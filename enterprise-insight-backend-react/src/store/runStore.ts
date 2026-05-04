import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { compileDsl } from '../api/compiler'
import { runOrchestrator } from '../api/orchestrator'
import type {
  CompileResponse,
  OrchestratorRunRequest,
  OrchestratorRunResponse,
} from '../api/types'
import { useHistoryStore } from './historyStore'
import { useNotificationStore } from './notifications'
import type {
  Execution,
  ExecutionPhase,
  RepairAttempt,
  RunEvent,
  RunRecord,
  StepKey,
  StepState,
} from './types'

export type AsyncStatus = 'idle' | 'loading' | 'success' | 'error'

type RunDataState = {
  dslText: string
  prompt: string
  compileResult: CompileResponse | null
  compileStatus: AsyncStatus
  compileError: string
  execution: Execution
}

type RunState = RunDataState & {
  setDslText: (value: string) => void
  loadDsl: (value: string) => void
  dispatch: (event: RunEvent) => void
  onEvent: (event: RunEvent) => void
  compileCurrentDsl: () => Promise<void>
  runCurrentDsl: (params: Pick<OrchestratorRunRequest, 'model' | 'targetDirectory' | 'verifyCommands' | 'maxRepairRounds'>) => Promise<void>
}

const defaultDsl = [
  'project:',
  '  type: spring_boot',
  '  modules:',
  '    - user',
  '    - auth',
  'constraints:',
  '  db: mysql',
  '  language: Java 21',
].join('\n')

const notify = (type: 'error' | 'info' | 'success', message: string) => {
  useNotificationStore.getState().push({
    id: crypto.randomUUID(),
    type,
    message,
  })
}

const createStep = (key: StepKey, title = key): StepState => ({
  key,
  title,
  status: 'idle',
  detail: `Waiting for ${title}.`,
  attempts: key === 'repair' ? [] : undefined,
})

export const createInitialSteps = (): StepState[] => [
  createStep('compile'),
  createStep('generate'),
  createStep('verify'),
  {
    ...createStep('repair'),
    detail: 'Repair runs only when verification fails and repair rounds are available.',
  },
]

export const createInitialExecution = (): Execution => ({
  id: null,
  phase: 'idle',
  steps: createInitialSteps(),
})

const phaseByStep: Record<StepKey, ExecutionPhase> = {
  compile: 'compiling',
  generate: 'generating',
  verify: 'verifying',
  repair: 'repairing',
}

const updateStep = (
  steps: StepState[],
  key: StepKey,
  patch: Partial<StepState> | ((step: StepState) => StepState),
) =>
  steps.map((step) => {
    if (step.key !== key) {
      return step
    }
    return typeof patch === 'function' ? patch(step) : { ...step, ...patch }
  })

const upsertRepairAttempt = (
  attempts: RepairAttempt[] | undefined,
  round: number,
  patch: Omit<RepairAttempt, 'round'>,
) => {
  const current = attempts ?? []
  const existing = current.find((attempt) => attempt.round === round)

  if (!existing) {
    return [...current, { round, ...patch }]
  }

  return current.map((attempt) =>
    attempt.round === round ? { ...attempt, ...patch } : attempt,
  )
}

const runReducer = (execution: Execution, event: RunEvent): Execution => {
  switch (event.type) {
    case 'RUN_STARTED':
      return {
        id: event.runId,
        phase: 'compiling',
        steps: createInitialSteps(),
      }
    case 'STEP_STARTED':
      return {
        ...execution,
        phase: phaseByStep[event.step],
        error: undefined,
        steps: updateStep(execution.steps, event.step, {
          status: 'running',
          detail: event.detail ?? `Running ${event.step}.`,
        }),
      }
    case 'STEP_SUCCESS':
      return {
        ...execution,
        steps: updateStep(execution.steps, event.step, {
          status: 'success',
          detail: event.detail ?? `${event.step} completed.`,
        }),
      }
    case 'STEP_FAIL':
      return {
        ...execution,
        steps: updateStep(execution.steps, event.step, {
          status: 'fail',
          detail: event.detail ?? event.error ?? `${event.step} failed.`,
        }),
      }
    case 'REPAIR_ROUND_STARTED':
      return {
        ...execution,
        phase: 'repairing',
        error: undefined,
        steps: updateStep(execution.steps, 'repair', (step) => ({
          ...step,
          status: 'running',
          detail: event.message ?? `Repair round ${event.round} started.`,
          attempts: upsertRepairAttempt(step.attempts, event.round, {
            status: 'running',
            message: event.message,
          }),
        })),
      }
    case 'REPAIR_ROUND_SUCCESS':
      return {
        ...execution,
        steps: updateStep(execution.steps, 'repair', (step) => ({
          ...step,
          status: 'success',
          detail: event.message ?? `Repair round ${event.round} succeeded.`,
          attempts: upsertRepairAttempt(step.attempts, event.round, {
            status: 'success',
            message: event.message,
          }),
        })),
      }
    case 'REPAIR_ROUND_FAIL':
      return {
        ...execution,
        steps: updateStep(execution.steps, 'repair', (step) => ({
          ...step,
          status: 'fail',
          detail: event.message ?? `Repair round ${event.round} failed.`,
          attempts: upsertRepairAttempt(step.attempts, event.round, {
            status: 'fail',
            message: event.message,
          }),
        })),
      }
    case 'RUN_COMPLETED':
      return {
        ...execution,
        phase: 'completed',
        result: event.result,
        error: undefined,
      }
    case 'RUN_FAILED':
      return {
        ...execution,
        phase: 'failed',
        result: event.result,
        error: event.error,
      }
    case 'RUN_CANCELLED':
      return {
        ...execution,
        phase: 'cancelled',
        error: event.error,
      }
    case 'RUN_RESET':
      return createInitialExecution()
    case 'DSL_TEXT_CHANGED':
    case 'DSL_LOADED':
    case 'DSL_COMPILE_STARTED':
    case 'DSL_COMPILE_SUCCESS':
    case 'DSL_COMPILE_FAIL':
      return execution
    default:
      return execution
  }
}

const runStoreReducer = (state: RunDataState, event: RunEvent): RunDataState => {
  switch (event.type) {
    case 'DSL_TEXT_CHANGED':
      return {
        ...state,
        dslText: event.value,
      }
    case 'DSL_LOADED':
      return {
        ...state,
        dslText: event.value,
        compileError: '',
      }
    case 'DSL_COMPILE_STARTED':
      return {
        ...state,
        compileStatus: 'loading',
        compileError: '',
        prompt: '',
      }
    case 'DSL_COMPILE_SUCCESS':
      return {
        ...state,
        compileResult: event.result,
        prompt: event.result.prompt,
        compileStatus: 'success',
      }
    case 'DSL_COMPILE_FAIL':
      return {
        ...state,
        compileError: event.error,
        compileStatus: 'error',
      }
    case 'RUN_COMPLETED':
      return {
        ...state,
        prompt: event.result.harnessPrompt,
        execution: runReducer(state.execution, event),
      }
    default:
      return {
        ...state,
        execution: runReducer(state.execution, event),
      }
  }
}

const getEventRunId = (event: RunEvent) => {
  if (event.type === 'RUN_STARTED') {
    return event.runId
  }
  if ('runId' in event) {
    return event.runId
  }
  return undefined
}

const shouldAcceptRuntimeEvent = (execution: Execution, event: RunEvent) => {
  const eventRunId = getEventRunId(event)

  if (!eventRunId) {
    return true
  }

  if (event.type === 'RUN_STARTED') {
    return true
  }

  return execution.id === eventRunId
}

const isActivePhase = (phase: ExecutionPhase) =>
  phase === 'compiling' || phase === 'generating' || phase === 'verifying' || phase === 'repairing'

const hasRepairAttempts = (response: OrchestratorRunResponse) =>
  response.generation.attempts.length > 1

const replayResponseEvents = (dispatch: (event: RunEvent) => void, response: OrchestratorRunResponse) => {
  if (response.harnessPrompt) {
    dispatch({
      type: 'STEP_SUCCESS',
      step: 'compile',
      detail: response.harnessPrompt,
    })
  } else {
    dispatch({
      type: 'STEP_FAIL',
      step: 'compile',
      error: 'Compiler returned no prompt.',
    })
  }

  dispatch({
    type: 'STEP_STARTED',
    step: 'generate',
    detail: 'Agent generation started.',
  })

  if (response.generation.finalOutput) {
    dispatch({
      type: 'STEP_SUCCESS',
      step: 'generate',
      detail: response.generation.finalOutput,
    })
  } else {
    dispatch({
      type: 'STEP_FAIL',
      step: 'generate',
      error: response.generation.status,
    })
  }

  dispatch({
    type: 'STEP_STARTED',
    step: 'verify',
    detail: 'Verification started.',
  })

  const verification = response.generation.finalVerificationResult
  if (response.generation.successful) {
    dispatch({
      type: 'STEP_SUCCESS',
      step: 'verify',
      detail: verification?.summary || response.generation.status,
    })
  } else {
    dispatch({
      type: 'STEP_FAIL',
      step: 'verify',
      error: verification?.summary || response.generation.status,
    })
  }

  if (hasRepairAttempts(response)) {
    response.generation.attempts.slice(1).forEach((attempt, index) => {
      const round = index + 1
      dispatch({
        type: 'REPAIR_ROUND_STARTED',
        round,
        message: `Repair round ${round} started.`,
      })
      dispatch({
        type: attempt.successful ? 'REPAIR_ROUND_SUCCESS' : 'REPAIR_ROUND_FAIL',
        round,
        message: attempt.verificationResult?.summary || response.generation.status,
      })
    })
  }

  if (response.generation.successful) {
    dispatch({ type: 'RUN_COMPLETED', result: response })
  } else {
    dispatch({
      type: 'RUN_FAILED',
      error: `Run finished with status: ${response.generation.status}`,
      result: response,
    })
  }
}

export const useRunStore = create<RunState>()(
  persist(
    (set, get) => {
      const dispatch = (event: RunEvent) => {
        set((state) => runStoreReducer(state, event))
      }
      const onEvent = (event: RunEvent) => {
        if (!shouldAcceptRuntimeEvent(get().execution, event)) {
          return
        }
        dispatch(event)
      }

      return {
        dslText: defaultDsl,
        prompt: '',
        compileResult: null,
        compileStatus: 'idle',
        compileError: '',
        execution: createInitialExecution(),
        setDslText: (value) => dispatch({ type: 'DSL_TEXT_CHANGED', value }),
        loadDsl: (value) => dispatch({ type: 'DSL_LOADED', value }),
        dispatch,
        onEvent,
        compileCurrentDsl: async () => {
          const dsl = get().dslText
          if (!dsl.trim()) {
            notify('error', 'DSL is empty. Add YAML before compiling.')
            dispatch({ type: 'DSL_COMPILE_FAIL', error: 'DSL is empty.' })
            return
          }
          notify('info', 'Compiling DSL...')
          dispatch({ type: 'DSL_COMPILE_STARTED' })
          try {
            const result = await compileDsl(dsl)
            dispatch({ type: 'DSL_COMPILE_SUCCESS', result })
            notify('success', 'DSL compiled successfully.')
          } catch (err) {
            const message = err instanceof Error ? err.message : 'Compile failed'
            dispatch({ type: 'DSL_COMPILE_FAIL', error: message })
            notify('error', message)
          }
        },
        runCurrentDsl: async (params) => {
          const dsl = get().dslText
          if (!dsl.trim()) {
            notify('error', 'DSL is empty. Add YAML before running.')
            dispatch({ type: 'RUN_RESET' })
            dispatch({ type: 'RUN_FAILED', error: 'DSL is empty.' })
            return
          }
          if (!params.targetDirectory.trim()) {
            notify('error', 'Target directory is required.')
            dispatch({ type: 'RUN_RESET' })
            dispatch({ type: 'RUN_FAILED', error: 'Target directory is required.' })
            return
          }

          const runId = crypto.randomUUID()
          notify('info', 'Run started.')
          dispatch({ type: 'RUN_STARTED', runId })
          dispatch({ type: 'STEP_STARTED', step: 'compile', detail: 'Sending DSL to orchestrator.' })

          try {
            const response = await runOrchestrator({
              requirement: dsl,
              model: params.model,
              targetDirectory: params.targetDirectory,
              verifyCommands: params.verifyCommands,
              maxRepairRounds: params.maxRepairRounds,
            })

            if (get().execution.id !== runId) {
              return
            }

            replayResponseEvents(dispatch, response)

            const execution = get().execution
            const record: RunRecord = {
              id: response.runId || runId,
              dsl,
              targetDirectory: params.targetDirectory,
              model: params.model ?? '',
              createdAt: response.createdAt,
              response,
              phase: execution.phase,
              steps: execution.steps,
            }
            useHistoryStore.getState().addRun(record)

            notify(
              response.generation.successful ? 'success' : 'error',
              response.generation.successful
                ? 'Run completed and verified.'
                : `Run finished with status: ${response.generation.status}`,
            )
          } catch (err) {
            if (get().execution.id !== runId) {
              return
            }

            const message = err instanceof Error ? err.message : 'Run failed'
            dispatch({ type: 'STEP_FAIL', step: 'compile', error: message })
            dispatch({ type: 'RUN_FAILED', error: message })
            notify('error', message)
          }
        },
      }
    },
    {
      name: 'orchestrator-run',
      partialize: (state) => ({
        dslText: state.dslText,
        prompt: state.prompt,
      }),
    },
  ),
)

export const isExecutionActive = (execution: Execution) => isActivePhase(execution.phase)

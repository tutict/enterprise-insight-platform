import { create } from 'zustand'
import type {
  Execution,
  ExecutionPhase,
  RepairAttempt,
  RunConnectionState,
  RunEvent,
  RunStatus,
  StepKey,
  StepState,
} from '../model/runEvent'

export type RunState = {
  runId: string | null
  status: RunStatus
  connectionState: RunConnectionState
  lastEventId?: string
}

type RunStoreState = RunState & {
  execution: Execution
  dispatch: (event: RunEvent) => void
  onEvent: (event: RunEvent) => void
}

const createStep = (key: StepKey, title = key): StepState => ({
  key,
  title,
  status: 'idle',
  detailKey: 'detail.waiting',
  detailParams: { step: title },
  attempts: key === 'repair' ? [] : undefined,
})

export const createInitialSteps = (): StepState[] => [
  createStep('compile'),
  createStep('generate'),
  createStep('verify'),
  {
    ...createStep('repair'),
    detailKey: 'detail.repairWaiting',
    detailParams: undefined,
  },
]

export const createInitialExecution = (): Execution => ({
  id: null,
  phase: 'idle',
  steps: createInitialSteps(),
  eventLog: [],
})

const createInitialRunState = (): RunState => ({
  runId: null,
  status: 'idle',
  connectionState: 'disconnected',
})

const phaseByStep: Record<StepKey, ExecutionPhase> = {
  compile: 'compiling',
  generate: 'generating',
  verify: 'verifying',
  repair: 'repairing',
}

const getEventRunId = (event: RunEvent) => {
  if ('runId' in event) {
    return event.runId
  }
  return undefined
}

const shouldAcceptRuntimeEvent = (execution: Execution, event: RunEvent) => {
  if (event.type === 'RUN_RESET') {
    return true
  }

  const eventRunId = getEventRunId(event)

  if (event.type === 'RUN_REQUESTED') {
    return true
  }

  if (!eventRunId) {
    return true
  }

  return !execution.id || execution.id === eventRunId
}

const appendEvent = (execution: Execution, event: RunEvent): Execution => ({
  ...execution,
  eventLog: [...execution.eventLog, event],
})

const getPayloadDetail = (payload: unknown) => {
  if (!payload) {
    return undefined
  }
  if (typeof payload === 'string') {
    return payload
  }
  if (typeof payload === 'object' && 'summary' in payload) {
    return String(payload.summary)
  }
  if (typeof payload === 'object' && 'status' in payload) {
    return String(payload.status)
  }
  if (typeof payload === 'object' && 'output' in payload) {
    return String(payload.output)
  }
  if (typeof payload === 'object' && 'error' in payload) {
    return String(payload.error)
  }
  return undefined
}

const getPayloadResult = (payload: unknown): Execution['result'] => {
  if (!payload || typeof payload !== 'object') {
    return undefined
  }
  if ('result' in payload) {
    return payload.result as Execution['result']
  }
  return payload as Execution['result']
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

const startRepairAttempt = (attempts: RepairAttempt[] | undefined, detail?: string) => {
  const current = attempts ?? []
  return [
    ...current,
    {
      round: current.length + 1,
      status: 'running' as const,
      message: detail,
    },
  ]
}

const finishLatestRepairAttempt = (
  attempts: RepairAttempt[] | undefined,
  status: RepairAttempt['status'],
  message?: string,
) => {
  const current = attempts ?? []
  if (!current.length) {
    return [
      {
        round: 1,
        status,
        message,
      },
    ]
  }

  return current.map((attempt, index) =>
    index === current.length - 1
      ? {
          ...attempt,
          status,
          message: message ?? attempt.message,
        }
      : attempt,
  )
}

const currentRunningPhase = (execution: Execution): ExecutionPhase => {
  const runningStep = execution.steps.find((step) => step.status === 'running')
  return runningStep ? phaseByStep[runningStep.key] : 'requested'
}

export const runReducer = (execution: Execution, event: RunEvent): Execution => {
  switch (event.type) {
    case 'RUN_REQUESTED':
      return appendEvent(
        {
          id: event.runId ?? execution.id,
          phase: 'requested',
          steps: createInitialSteps(),
          result: undefined,
          error: undefined,
          config: event.config,
          eventLog: [],
        },
        event,
      )
    case 'STEP_STARTED': {
      const hasBackendDetail = Boolean(event.detail)
      return appendEvent(
        {
          ...execution,
          phase: phaseByStep[event.step],
          error: undefined,
          steps: updateStep(execution.steps, event.step, (step) => ({
            ...step,
            status: 'running',
            detail: event.detail,
            detailKey: hasBackendDetail ? undefined : 'detail.running',
            detailParams: hasBackendDetail ? undefined : { step: event.step },
            attempts: event.step === 'repair' ? startRepairAttempt(step.attempts, event.detail) : step.attempts,
          })),
        },
        event,
      )
    }
    case 'STEP_SUCCEEDED': {
      const detail = event.detail ?? getPayloadDetail(event.payload)
      const hasBackendDetail = Boolean(detail)
      return appendEvent(
        {
          ...execution,
          steps: updateStep(execution.steps, event.step, (step) => ({
            ...step,
            status: 'success',
            detail,
            detailKey: hasBackendDetail ? undefined : 'detail.completed',
            detailParams: hasBackendDetail ? undefined : { step: event.step },
            attempts:
              event.step === 'repair'
                ? finishLatestRepairAttempt(step.attempts, 'success', detail)
                : step.attempts,
          })),
        },
        event,
      )
    }
    case 'STEP_FAILED': {
      const detail = event.detail ?? event.error
      return appendEvent(
        {
          ...execution,
          steps: updateStep(execution.steps, event.step, (step) => ({
            ...step,
            status: 'fail',
            detail,
            detailKey: undefined,
            detailParams: undefined,
            attempts:
              event.step === 'repair'
                ? finishLatestRepairAttempt(step.attempts, 'fail', detail)
                : step.attempts,
          })),
        },
        event,
      )
    }
    case 'STEP_RETRY_REQUESTED':
      return appendEvent(
        {
          ...execution,
          phase: phaseByStep[event.step],
          error: undefined,
          steps: updateStep(execution.steps, event.step, {
            status: 'running',
            detail: event.reason,
            detailKey: event.reason ? undefined : 'detail.retrying',
            detailParams: event.reason ? undefined : { step: event.step },
          }),
        },
        event,
      )
    case 'RUN_COMPLETED':
      return appendEvent(
        {
          ...execution,
          id: event.runId ?? execution.id,
          phase: 'completed',
          result: event.result ?? getPayloadResult(event.payload),
          error: undefined,
        },
        event,
      )
    case 'RUN_FAILED':
      return appendEvent(
        {
          ...execution,
          id: event.runId ?? execution.id,
          phase: 'failed',
          result: event.result ?? getPayloadResult(event.payload),
          error: event.error,
        },
        event,
      )
    case 'RUN_CANCELLED':
      return appendEvent(
        {
          ...execution,
          id: event.runId ?? execution.id,
          phase: 'cancelled',
          error: event.error,
        },
        event,
      )
    case 'RUN_PAUSED':
      return appendEvent(
        {
          ...execution,
          phase: 'paused',
          error: event.reason,
        },
        event,
      )
    case 'RUN_RESUMED':
      return appendEvent(
        {
          ...execution,
          phase: currentRunningPhase(execution),
          error: undefined,
        },
        event,
      )
    case 'RUN_RESET':
      return createInitialExecution()
    default:
      return execution
  }
}

const reduceRuntimeState = (
  state: RunStoreState,
  event: RunEvent,
  execution: Execution,
): RunState => {
  const eventRunId = getEventRunId(event) ?? state.runId
  const lastEventId = event.eventId ?? state.lastEventId

  switch (event.type) {
    case 'RUN_RESET':
      return createInitialRunState()
    case 'STREAM_CONNECTING':
      return { ...state, runId: eventRunId, connectionState: 'connecting', lastEventId }
    case 'STREAM_CONNECTED':
      return { ...state, runId: eventRunId, connectionState: 'connected', lastEventId }
    case 'STREAM_RECONNECTING':
      return { ...state, runId: eventRunId, connectionState: 'reconnecting', lastEventId }
    case 'STREAM_DISCONNECTED':
      return { ...state, runId: eventRunId, connectionState: 'disconnected', lastEventId }
    case 'RUN_REQUESTED':
      return { ...state, runId: eventRunId, status: 'running', lastEventId }
    case 'RUN_PAUSED':
      return { ...state, runId: eventRunId, status: 'paused', lastEventId }
    case 'RUN_RESUMED':
      return { ...state, runId: eventRunId, status: 'running', lastEventId }
    case 'RUN_COMPLETED':
      return {
        ...state,
        runId: eventRunId,
        status: 'completed',
        connectionState: 'disconnected',
        lastEventId,
      }
    case 'RUN_FAILED':
    case 'RUN_CANCELLED':
      return {
        ...state,
        runId: eventRunId,
        status: 'failed',
        connectionState: 'disconnected',
        lastEventId,
      }
    case 'STEP_STARTED':
    case 'STEP_SUCCEEDED':
    case 'STEP_FAILED':
    case 'STEP_RETRY_REQUESTED':
      return {
        ...state,
        runId: eventRunId,
        status: execution.phase === 'paused' ? 'paused' : 'running',
        lastEventId,
      }
    default:
      return { ...state, runId: eventRunId, lastEventId }
  }
}

export const useRunStore = create<RunStoreState>((set, get) => {
  const dispatch = (event: RunEvent) => {
    if (!shouldAcceptRuntimeEvent(get().execution, event)) {
      return
    }

    set((state) => {
      const execution = runReducer(state.execution, event)
      return {
        ...reduceRuntimeState(state, event, execution),
        execution,
      }
    })
  }

  return {
    ...createInitialRunState(),
    execution: createInitialExecution(),
    dispatch,
    onEvent: dispatch,
  }
})

export const isExecutionActive = (execution: Execution) =>
  execution.phase === 'requested' ||
  execution.phase === 'compiling' ||
  execution.phase === 'generating' ||
  execution.phase === 'verifying' ||
  execution.phase === 'repairing'

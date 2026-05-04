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
import type { RunRecord, TimelineStep } from './types'

export type AsyncStatus = 'idle' | 'loading' | 'success' | 'error'
export type RunStatus = 'idle' | 'running' | 'success' | 'error'

type RunState = {
  dslText: string
  prompt: string
  compileResult: CompileResponse | null
  compileStatus: AsyncStatus
  compileError: string
  status: RunStatus
  error: string
  result: OrchestratorRunResponse | null
  steps: TimelineStep[]
  setDslText: (value: string) => void
  loadDsl: (value: string) => void
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

export const createInitialTimeline = (): TimelineStep[] => [
  {
    key: 'compile',
    title: 'compile',
    status: 'pending',
    detail: 'Waiting for DSL compilation.',
  },
  {
    key: 'generate',
    title: 'generate',
    status: 'pending',
    detail: 'Waiting for agent generation.',
  },
  {
    key: 'verify',
    title: 'verify',
    status: 'pending',
    detail: 'Waiting for project verification.',
  },
]

const stepsFromResponse = (response: OrchestratorRunResponse): TimelineStep[] => {
  const generation = response.generation
  const verification = generation.finalVerificationResult
  return [
    {
      key: 'compile',
      title: 'compile',
      status: response.harnessPrompt ? 'success' : 'fail',
      detail: response.harnessPrompt || 'Compiler returned no prompt.',
    },
    {
      key: 'generate',
      title: 'generate',
      status: generation.finalOutput ? 'success' : 'fail',
      detail: generation.finalOutput || generation.status,
    },
    {
      key: 'verify',
      title: 'verify',
      status: generation.successful ? 'success' : 'fail',
      detail: verification?.summary || generation.status,
    },
  ]
}

export const useRunStore = create<RunState>()(
  persist(
    (set, get) => ({
      dslText: defaultDsl,
      prompt: '',
      compileResult: null,
      compileStatus: 'idle',
      compileError: '',
      status: 'idle',
      error: '',
      result: null,
      steps: createInitialTimeline(),
      setDslText: (value) => set({ dslText: value }),
      loadDsl: (value) =>
        set({
          dslText: value,
          compileError: '',
          error: '',
        }),
      compileCurrentDsl: async () => {
        const dsl = get().dslText
        if (!dsl.trim()) {
          notify('error', 'DSL is empty. Add YAML before compiling.')
          set({ compileStatus: 'error', compileError: 'DSL is empty.' })
          return
        }
        notify('info', 'Compiling DSL...')
        set({ compileStatus: 'loading', compileError: '', prompt: '' })
        try {
          const result = await compileDsl(dsl)
          set({
            compileResult: result,
            prompt: result.prompt,
            compileStatus: 'success',
          })
          notify('success', 'DSL compiled successfully.')
        } catch (err) {
          const message = err instanceof Error ? err.message : 'Compile failed'
          set({
            compileError: message,
            compileStatus: 'error',
          })
          notify('error', message)
        }
      },
      runCurrentDsl: async (params) => {
        const dsl = get().dslText
        if (!dsl.trim()) {
          notify('error', 'DSL is empty. Add YAML before running.')
          set({ status: 'error', error: 'DSL is empty.' })
          return
        }
        if (!params.targetDirectory.trim()) {
          notify('error', 'Target directory is required.')
          set({ status: 'error', error: 'Target directory is required.' })
          return
        }
        notify('info', 'Run started.')
        set({
          status: 'running',
          error: '',
          result: null,
          steps: [
            { key: 'compile', title: 'compile', status: 'running', detail: 'Sending DSL to compiler.' },
            { key: 'generate', title: 'generate', status: 'pending', detail: 'Waiting for compiled prompt.' },
            { key: 'verify', title: 'verify', status: 'pending', detail: 'Verification runs after generation.' },
          ],
        })
        try {
          const response = await runOrchestrator({
            requirement: dsl,
            model: params.model,
            targetDirectory: params.targetDirectory,
            verifyCommands: params.verifyCommands,
            maxRepairRounds: params.maxRepairRounds,
          })
          const steps = stepsFromResponse(response)
          const record: RunRecord = {
            id: response.runId,
            dsl,
            targetDirectory: params.targetDirectory,
            model: params.model ?? '',
            createdAt: response.createdAt,
            response,
            steps,
          }
          useHistoryStore.getState().addRun(record)
          set({
            status: response.generation.successful ? 'success' : 'error',
            result: response,
            prompt: response.harnessPrompt,
            steps,
          })
          notify(
            response.generation.successful ? 'success' : 'error',
            response.generation.successful
              ? 'Run completed and verified.'
              : `Run finished with status: ${response.generation.status}`,
          )
        } catch (err) {
          const message = err instanceof Error ? err.message : 'Run failed'
          set({
            status: 'error',
            error: message,
            steps: [
              { key: 'compile', title: 'compile', status: 'fail', detail: message },
              { key: 'generate', title: 'generate', status: 'pending', detail: 'Generation did not start.' },
              { key: 'verify', title: 'verify', status: 'pending', detail: 'Verification did not start.' },
            ],
          })
          notify('error', message)
        }
      },
    }),
    {
      name: 'orchestrator-run',
      partialize: (state) => ({
        dslText: state.dslText,
        prompt: state.prompt,
      }),
    },
  ),
)

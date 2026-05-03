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

type RunState = {
  dslText: string
  prompt: string
  compileResult: CompileResponse | null
  compileLoading: boolean
  compileError: string
  runLoading: boolean
  runError: string
  runResult: OrchestratorRunResponse | null
  timeline: TimelineStep[]
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

const timelineFromResponse = (response: OrchestratorRunResponse): TimelineStep[] => {
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
      compileLoading: false,
      compileError: '',
      runLoading: false,
      runError: '',
      runResult: null,
      timeline: createInitialTimeline(),
      setDslText: (value) => set({ dslText: value }),
      loadDsl: (value) =>
        set({
          dslText: value,
          compileError: '',
          runError: '',
        }),
      compileCurrentDsl: async () => {
        const dsl = get().dslText
        if (!dsl.trim()) {
          notify('error', 'DSL is empty. Add YAML before compiling.')
          set({ compileError: 'DSL is empty.' })
          return
        }
        notify('info', 'Compiling DSL...')
        set({ compileLoading: true, compileError: '', prompt: '' })
        try {
          const result = await compileDsl(dsl)
          set({
            compileResult: result,
            prompt: result.prompt,
            compileLoading: false,
          })
          notify('success', 'DSL compiled successfully.')
        } catch (err) {
          const message = err instanceof Error ? err.message : 'Compile failed'
          set({
            compileError: message,
            compileLoading: false,
          })
          notify('error', message)
        }
      },
      runCurrentDsl: async (params) => {
        const dsl = get().dslText
        if (!dsl.trim()) {
          notify('error', 'DSL is empty. Add YAML before running.')
          set({ runError: 'DSL is empty.' })
          return
        }
        if (!params.targetDirectory.trim()) {
          notify('error', 'Target directory is required.')
          set({ runError: 'Target directory is required.' })
          return
        }
        notify('info', 'Run started.')
        set({
          runLoading: true,
          runError: '',
          runResult: null,
          timeline: [
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
          const timeline = timelineFromResponse(response)
          const record: RunRecord = {
            id: response.runId,
            dsl,
            targetDirectory: params.targetDirectory,
            model: params.model ?? '',
            createdAt: response.createdAt,
            response,
            timeline,
          }
          useHistoryStore.getState().addRun(record)
          set({
            runLoading: false,
            runResult: response,
            prompt: response.harnessPrompt,
            timeline,
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
            runLoading: false,
            runError: message,
            timeline: [
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

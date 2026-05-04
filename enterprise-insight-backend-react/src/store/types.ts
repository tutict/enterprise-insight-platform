import type { CompileResponse, OrchestratorRunResponse } from '../api/types'

export type ExecutionPhase =
  | 'idle'
  | 'compiling'
  | 'generating'
  | 'verifying'
  | 'repairing'
  | 'completed'
  | 'failed'
  | 'cancelled'

export type StepKey = 'compile' | 'generate' | 'verify' | 'repair'

export type StepStatus = 'idle' | 'running' | 'success' | 'fail'

export type RepairAttempt = {
  round: number
  status: 'running' | 'success' | 'fail'
  message?: string
}

export type StepState = {
  key: StepKey
  title: string
  status: StepStatus
  detail?: string
  attempts?: RepairAttempt[]
}

export type Execution = {
  id: string | null
  phase: ExecutionPhase
  steps: StepState[]
  result?: OrchestratorRunResponse
  error?: string
}

export type RunEvent =
  | { type: 'DSL_TEXT_CHANGED'; value: string }
  | { type: 'DSL_LOADED'; value: string }
  | { type: 'DSL_COMPILE_STARTED' }
  | { type: 'DSL_COMPILE_SUCCESS'; result: CompileResponse }
  | { type: 'DSL_COMPILE_FAIL'; error: string }
  | { type: 'RUN_STARTED'; runId: string }
  | { type: 'STEP_STARTED'; step: StepKey; detail?: string; runId?: string }
  | { type: 'STEP_SUCCESS'; step: StepKey; detail?: string; runId?: string }
  | { type: 'STEP_FAIL'; step: StepKey; error?: string; detail?: string; runId?: string }
  | { type: 'REPAIR_ROUND_STARTED'; round: number; message?: string; runId?: string }
  | { type: 'REPAIR_ROUND_SUCCESS'; round: number; message?: string; runId?: string }
  | { type: 'REPAIR_ROUND_FAIL'; round: number; message?: string; runId?: string }
  | { type: 'RUN_COMPLETED'; result: OrchestratorRunResponse; runId?: string }
  | { type: 'RUN_FAILED'; error: string; result?: OrchestratorRunResponse; runId?: string }
  | { type: 'RUN_CANCELLED'; error?: string; runId?: string }
  | { type: 'RUN_RESET' }

export type TimelineStep = StepState

export type SavedDsl = {
  id: string
  name: string
  value: string
  updatedAt: string
}

export type RunRecord = {
  id: string
  dsl: string
  targetDirectory: string
  model: string
  createdAt: string
  response: OrchestratorRunResponse
  phase: ExecutionPhase
  steps: StepState[]
}

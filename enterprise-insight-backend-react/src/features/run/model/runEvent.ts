import type { OrchestratorRunResponse } from '../../../api/types/orchestrator.types'
import type { RunConfig } from './runConfig'

export type StepKey = 'compile' | 'generate' | 'verify' | 'repair'

export type StepStatus = 'idle' | 'running' | 'success' | 'fail'

export type ExecutionPhase =
  | 'idle'
  | 'requested'
  | 'compiling'
  | 'generating'
  | 'verifying'
  | 'repairing'
  | 'paused'
  | 'completed'
  | 'failed'
  | 'cancelled'

export type RepairAttempt = {
  round: number
  status: 'running' | 'success' | 'fail'
  message?: string
}

export type TranslationParams = Record<string, string | number>

export type StepState = {
  key: StepKey
  title: string
  status: StepStatus
  detail?: string
  detailKey?: string
  detailParams?: TranslationParams
  attempts?: RepairAttempt[]
}

export type RunEventBase = {
  runId?: string
  timestamp?: string | number
  eventId?: string
}

export type RunEvent =
  | ({ type: 'RUN_REQUESTED'; config?: RunConfig } & RunEventBase)
  | ({ type: 'STEP_STARTED'; step: StepKey; detail?: string } & RunEventBase)
  | ({ type: 'STEP_SUCCEEDED'; step: StepKey; payload?: unknown; detail?: string } & RunEventBase)
  | ({ type: 'STEP_FAILED'; step: StepKey; error: string; payload?: unknown; detail?: string } & RunEventBase)
  | ({ type: 'RUN_COMPLETED'; result?: OrchestratorRunResponse; payload?: unknown } & RunEventBase)
  | ({ type: 'RUN_FAILED'; error: string; result?: OrchestratorRunResponse; payload?: unknown } & RunEventBase)
  | ({ type: 'RUN_CANCELLED'; error?: string } & RunEventBase)
  | ({ type: 'RUN_PAUSED'; reason?: string } & RunEventBase)
  | ({ type: 'RUN_RESUMED' } & RunEventBase)
  | ({ type: 'STEP_RETRY_REQUESTED'; step: StepKey; attempt?: number; reason?: string } & RunEventBase)
  | ({ type: 'STREAM_CONNECTING'; lastEventId?: string } & RunEventBase)
  | ({ type: 'STREAM_CONNECTED'; lastEventId?: string } & RunEventBase)
  | ({ type: 'STREAM_RECONNECTING'; lastEventId?: string; error?: string } & RunEventBase)
  | ({ type: 'STREAM_DISCONNECTED'; lastEventId?: string; error?: string } & RunEventBase)
  | ({ type: 'RUN_RESET' } & RunEventBase)

export type RunConnectionState = 'disconnected' | 'connecting' | 'connected' | 'reconnecting'

export type RunStatus = 'idle' | 'running' | 'paused' | 'failed' | 'completed'

export type Execution = {
  id: string | null
  phase: ExecutionPhase
  steps: StepState[]
  result?: OrchestratorRunResponse
  error?: string
  config?: RunConfig
  eventLog: RunEvent[]
}

export type TimelineStep = StepState

export type RunRecord = {
  id: string
  dsl: string
  targetDirectory: string
  model: string
  createdAt: string
  response: OrchestratorRunResponse
  phase: ExecutionPhase
  steps: StepState[]
  eventLog: RunEvent[]
}

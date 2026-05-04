import type { RunEvent } from '../../features/run/model/runEvent'

export type RuntimeEventEnvelope = RunEvent & {
  runId?: string
}

export type RunStartResponse = {
  runId: string
}

export type ControlEvent =
  | { type: 'PAUSE'; runId: string }
  | { type: 'RESUME'; runId: string }
  | { type: 'CANCEL'; runId: string }
  | { type: 'RETRY_STEP'; runId: string; step: string }

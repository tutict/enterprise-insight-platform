import type { OrchestratorRunRequest, OrchestratorRunResponse } from './orchestrator.types'

export type DeliveryRunStatus = 'REQUESTED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

export type DeliveryRunRecord = {
  runId: string
  workspaceId: string
  playbookId: string
  playbookName: string
  status: DeliveryRunStatus
  request: OrchestratorRunRequest | null
  response: OrchestratorRunResponse | null
  events: Array<{
    eventId?: string
    runId: string
    type: string
    step?: string
    timestamp?: number | string
    payload?: Record<string, unknown>
  }>
  createdAt: string
  updatedAt: string
}

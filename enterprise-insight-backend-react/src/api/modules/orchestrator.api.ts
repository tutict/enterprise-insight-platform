import { apiRequest } from '../client'
import type { OrchestratorRunRequest, OrchestratorRunResponse } from '../types/orchestrator.types'

export function runOrchestrator(request: OrchestratorRunRequest) {
  return apiRequest<OrchestratorRunResponse>('/api/orchestrator/run', {
    method: 'POST',
    body: request,
  })
}

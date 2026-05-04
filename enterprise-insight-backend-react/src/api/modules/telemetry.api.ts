import { apiRequest } from '../client'
import type { UiErrorPayload } from '../types/telemetry.types'

export function logUiError(payload: UiErrorPayload) {
  return apiRequest<unknown>('/api/orchestrator/logs/ui-error', {
    method: 'POST',
    body: payload,
  })
}

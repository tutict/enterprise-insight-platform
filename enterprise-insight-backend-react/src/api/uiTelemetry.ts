import { apiRequest } from './client'

export type UiErrorPayload = {
  message: string
  stack: string
  componentStack: string
  url: string
  userAgent: string
  occurredAt: string
}

export function logUiError(payload: UiErrorPayload) {
  return apiRequest<unknown>('/api/orchestrator/logs/ui-error', {
    method: 'POST',
    body: payload,
  })
}

import { apiRequest } from '../client'
import type { DeliveryRunRecord } from '../types/delivery.types'

export function listDeliveryRuns() {
  return apiRequest<DeliveryRunRecord[]>('/api/orchestrator/delivery-runs')
}

export function getDeliveryRun(runId: string) {
  return apiRequest<DeliveryRunRecord>(`/api/orchestrator/delivery-runs/${encodeURIComponent(runId)}`)
}

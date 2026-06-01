import { apiRequest } from '../client'
import type { ProjectDeliveryBrief, ProjectInventory } from '../types/projectAnalysis.types'

export function getCurrentProjectInventory() {
  return apiRequest<ProjectInventory>('/api/project-analysis/current')
}

export function getCurrentProjectDeliveryBrief() {
  return apiRequest<ProjectDeliveryBrief>('/api/project-analysis/current/delivery-brief')
}

import type { OrchestratorRunResponse, StepStatus } from '../api/types'

export type TimelineStepKey = 'compile' | 'generate' | 'verify'

export type TimelineStep = {
  key: TimelineStepKey
  title: string
  status: StepStatus
  detail: string
}

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
  timeline: TimelineStep[]
}

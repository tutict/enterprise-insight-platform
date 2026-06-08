import type { DeliveryRunRecord } from './delivery.types'
import type { PatchProposal } from './patchProposal.types'

export type Workspace = {
  workspaceId: string
  customerName: string
  projectName: string
  repoRoot: string
  defaultBranch: string
  workingBranch?: string
  worktreePath?: string
  allowedPaths: string[]
  verifyCommands: string[][]
  modelPolicy: string
  secretPolicy: string
  createdAt: string
  updatedAt: string
}

export type EvidencePackage = {
  workspaceId: string
  runId: string
  markdownPath: string
  jsonPath: string
  markdown: string
  workspace: Workspace
  deliveryRun: DeliveryRunRecord
  patchProposal: PatchProposal
  exportedAt: string
}

import { apiRequest } from '../client'
import type { DeliveryRunRecord } from '../types/delivery.types'
import type { PatchProposal, PatchProposalDiff } from '../types/patchProposal.types'
import type { ProjectDeliveryBrief, ProjectInventory } from '../types/projectAnalysis.types'
import type { EvidencePackage, Workspace } from '../types/workspace.types'

const workspacePath = (workspaceId: string, suffix = '') =>
  `/api/workspaces/${encodeURIComponent(workspaceId)}${suffix}`

export function listWorkspaces() {
  return apiRequest<Workspace[]>('/api/workspaces')
}

export function getWorkspaceProjectInventory(workspaceId: string) {
  return apiRequest<ProjectInventory>(workspacePath(workspaceId, '/project-analysis/current'))
}

export function getWorkspaceProjectDeliveryBrief(workspaceId: string) {
  return apiRequest<ProjectDeliveryBrief>(workspacePath(workspaceId, '/project-analysis/current/delivery-brief'))
}

export function listWorkspaceDeliveryRuns(workspaceId: string) {
  return apiRequest<DeliveryRunRecord[]>(workspacePath(workspaceId, '/delivery-runs'))
}

export function exportWorkspaceEvidence(workspaceId: string, runId: string) {
  return apiRequest<EvidencePackage>(
    workspacePath(workspaceId, `/delivery-runs/${encodeURIComponent(runId)}/evidence`),
  )
}

export function getWorkspacePatchProposal(workspaceId: string, runId: string) {
  return apiRequest<PatchProposal>(
    workspacePath(workspaceId, `/delivery-runs/${encodeURIComponent(runId)}/patch-proposal`),
  )
}

export function regenerateWorkspacePatchProposal(workspaceId: string, runId: string) {
  return apiRequest<PatchProposal>(
    workspacePath(workspaceId, `/delivery-runs/${encodeURIComponent(runId)}/patch-proposal/regenerate`),
    { method: 'POST' },
  )
}

export function getWorkspacePatchProposalDiff(workspaceId: string, runId: string, fileId: string) {
  return apiRequest<PatchProposalDiff>(
    workspacePath(
      workspaceId,
      `/delivery-runs/${encodeURIComponent(runId)}/patch-proposal/files/${encodeURIComponent(fileId)}/diff`,
    ),
  )
}

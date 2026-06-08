export type PatchProposalStatus = 'READY' | 'HAS_REJECTED_FILES' | 'NO_CHANGES' | 'FAILED_TO_GENERATE'

export type PatchProposalChangeType = 'CREATE' | 'UPDATE' | 'NO_CHANGE' | 'REJECTED'

export type PatchProposalFile = {
  fileId: string
  targetPath: string
  generatedPath: string
  diffPath?: string | null
  changeType: PatchProposalChangeType
  bytesWritten: number
  oldSha256?: string | null
  newSha256?: string | null
  rejectedReason?: string | null
}

export type PatchProposal = {
  proposalId: string
  workspaceId: string
  runId: string
  status: PatchProposalStatus
  proposalPath: string
  verificationSourceRunId: string
  verificationScope: string
  verificationSuccessful: boolean | null
  verificationSummary?: string | null
  changeCount: number
  rejectedCount: number
  files: PatchProposalFile[]
  risks: string[]
  generatedAt: string
}

export type PatchProposalDiff = {
  workspaceId: string
  runId: string
  fileId: string
  targetPath: string
  changeType: PatchProposalChangeType
  diff: string
  rejectedReason?: string | null
}

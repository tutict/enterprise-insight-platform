import CodeBlock from '../../../shared/components/CodeBlock'
import type {
  PatchProposal,
  PatchProposalChangeType,
  PatchProposalDiff,
  PatchProposalStatus,
} from '../../../api/types/patchProposal.types'

type PatchProposalPanelProps = {
  proposal: PatchProposal | null
  selectedFileId: string | null
  selectedDiff: PatchProposalDiff | null
  isLoading: boolean
  onSelectFile: (fileId: string) => void
  onRegenerate: () => void
}

const statusLabel: Record<PatchProposalStatus, string> = {
  READY: 'Ready',
  HAS_REJECTED_FILES: 'Has rejected files',
  NO_CHANGES: 'No changes',
  FAILED_TO_GENERATE: 'Failed to generate',
}

const changeLabel: Record<PatchProposalChangeType, string> = {
  CREATE: 'Create',
  UPDATE: 'Update',
  NO_CHANGE: 'No change',
  REJECTED: 'Rejected',
}

const statusClass = (status: PatchProposalStatus) => {
  switch (status) {
    case 'READY':
      return 'border-emerald-500/40 bg-emerald-500/10 text-emerald-200'
    case 'HAS_REJECTED_FILES':
      return 'border-amber-500/40 bg-amber-500/10 text-amber-200'
    case 'NO_CHANGES':
      return 'border-slate-600 bg-slate-800 text-slate-300'
    case 'FAILED_TO_GENERATE':
      return 'border-rose-500/40 bg-rose-500/10 text-rose-200'
  }
}

const changeClass = (changeType: PatchProposalChangeType) => {
  switch (changeType) {
    case 'CREATE':
      return 'text-emerald-300'
    case 'UPDATE':
      return 'text-sky-300'
    case 'NO_CHANGE':
      return 'text-slate-400'
    case 'REJECTED':
      return 'text-rose-300'
  }
}

export default function PatchProposalPanel({
  proposal,
  selectedFileId,
  selectedDiff,
  isLoading,
  onSelectFile,
  onRegenerate,
}: PatchProposalPanelProps) {
  const selectedFile = proposal?.files.find((file) => file.fileId === selectedFileId) ?? proposal?.files[0] ?? null
  const diffValue = selectedFile?.changeType === 'REJECTED'
    ? selectedFile.rejectedReason ?? 'Rejected by path safety rules.'
    : selectedFile?.diffPath
      ? selectedDiff?.diff ?? 'Loading diff...'
      : 'No diff for this file.'

  return (
    <div className="panel p-5">
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-slate-100">Patch Proposal</h3>
          <p className="muted">Review-ready file changes generated from this delivery run.</p>
        </div>
        <button className="btn-secondary" type="button" disabled={isLoading} onClick={onRegenerate}>
          {isLoading ? 'Regenerating...' : 'Regenerate'}
        </button>
      </div>

      {proposal ? (
        <div className="space-y-4">
          <div className="flex flex-wrap items-center gap-3 text-xs">
            <span className={`rounded border px-2.5 py-1 font-medium ${statusClass(proposal.status)}`}>
              {statusLabel[proposal.status]}
            </span>
            <span className="text-slate-400">Changes: {proposal.changeCount}</span>
            <span className="text-slate-400">Rejected: {proposal.rejectedCount}</span>
            <span className="text-slate-400">Verification: {proposal.verificationScope}</span>
            <span className={proposal.verificationSuccessful ? 'text-emerald-300' : 'text-slate-400'}>
              {proposal.verificationSuccessful === null
                ? 'Unverified'
                : proposal.verificationSuccessful
                  ? 'Passed'
                  : 'Failed'}
            </span>
          </div>

          {proposal.risks.length ? (
            <div className="rounded-md border border-amber-500/30 bg-amber-500/10 p-3 text-xs text-amber-100">
              {proposal.risks.map((risk) => (
                <p key={risk}>{risk}</p>
              ))}
            </div>
          ) : null}

          <div className="grid gap-4 lg:grid-cols-[minmax(220px,0.75fr)_minmax(0,1.25fr)]">
            <div className="space-y-2">
              {proposal.files.length ? (
                proposal.files.map((file) => (
                  <button
                    key={file.fileId}
                    type="button"
                    className={`w-full rounded-md border px-3 py-2 text-left text-xs transition ${
                      file.fileId === selectedFile?.fileId
                        ? 'border-sky-400 bg-sky-400/10'
                        : 'border-slate-700 bg-slate-900/50 hover:border-slate-500'
                    }`}
                    onClick={() => onSelectFile(file.fileId)}
                  >
                    <span className={`block font-semibold ${changeClass(file.changeType)}`}>
                      {changeLabel[file.changeType]}
                    </span>
                    <span className="mt-1 block truncate text-slate-300">{file.targetPath}</span>
                  </button>
                ))
              ) : (
                <p className="rounded-md border border-slate-700 p-3 text-xs text-slate-500">No files recorded.</p>
              )}
            </div>

            <div className="min-w-0">
              <div className="mb-2 min-h-5 truncate text-xs font-medium text-slate-300">
                {selectedFile?.targetPath ?? 'No file selected'}
              </div>
              <CodeBlock value={diffValue} emptyLabel="No diff available." />
            </div>
          </div>
        </div>
      ) : (
        <div className="rounded-md border border-slate-700 p-4 text-sm text-slate-500">
          {isLoading ? 'Loading patch proposal...' : 'No patch proposal loaded.'}
        </div>
      )}
    </div>
  )
}

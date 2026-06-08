import type { Workspace } from '../../../api/types/workspace.types'
import type { RunRecord } from '../../run/model/runEvent'
import RunDetails from './RunDetails'
import RunList from './RunList'

type RunsWorkspaceProps = {
  runs: RunRecord[]
  selectedRun: RunRecord | null
  selectRun: (id: string) => void
  exportEvidence: (id: string) => void
  isLoading?: boolean
  workspaces: Workspace[]
  selectedWorkspaceId: string
  setSelectedWorkspaceId: (workspaceId: string) => void
}

export default function RunsWorkspace({
  runs,
  selectedRun,
  selectRun,
  exportEvidence,
  isLoading = false,
  workspaces,
  selectedWorkspaceId,
  setSelectedWorkspaceId,
}: RunsWorkspaceProps) {
  return (
    <div className="space-y-5">
      <section className="panel flex flex-wrap items-center justify-between gap-3 p-5">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">Workspace Evidence</h2>
          <p className="muted">Delivery runs are scoped to the selected workspace.</p>
        </div>
        <select
          className="field min-w-64"
          value={selectedWorkspaceId}
          onChange={(event) => setSelectedWorkspaceId(event.target.value)}
        >
          {workspaces.map((workspace) => (
            <option key={workspace.workspaceId} value={workspace.workspaceId}>
              {workspace.customerName} / {workspace.projectName}
            </option>
          ))}
        </select>
      </section>
      <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(380px,1fr)]">
        <RunList runs={runs} selectedRunId={selectedRun?.id ?? null} onSelectRun={selectRun} isLoading={isLoading} />
        <RunDetails run={selectedRun} exportEvidence={exportEvidence} />
      </div>
    </div>
  )
}

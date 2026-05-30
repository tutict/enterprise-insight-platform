import type { RunRecord } from '../../run/model/runEvent'
import RunDetails from './RunDetails'
import RunList from './RunList'

type RunsWorkspaceProps = {
  runs: RunRecord[]
  selectedRun: RunRecord | null
  selectRun: (id: string) => void
  isLoading?: boolean
}

export default function RunsWorkspace({ runs, selectedRun, selectRun, isLoading = false }: RunsWorkspaceProps) {
  return (
    <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(380px,1fr)]">
      <RunList runs={runs} selectedRunId={selectedRun?.id ?? null} onSelectRun={selectRun} isLoading={isLoading} />
      <RunDetails run={selectedRun} />
    </div>
  )
}

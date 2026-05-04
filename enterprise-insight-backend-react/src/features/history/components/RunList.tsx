import StatusBadge from '../../../components/StatusBadge'
import type { RunRecord } from '../../../store/types'

type RunListProps = {
  runs: RunRecord[]
  selectedRunId: string | null
  onSelectRun: (id: string) => void
}

export default function RunList({ runs, selectedRunId, onSelectRun }: RunListProps) {
  return (
    <section className="panel overflow-hidden">
      <div className="border-b border-white/10 p-5">
        <h2 className="text-lg font-semibold text-slate-100">Runs</h2>
        <p className="muted">Local browser history from real orchestrator responses.</p>
      </div>
      <div className="divide-y divide-white/10">
        {runs.length ? (
          runs.map((run) => (
            <button
              key={run.id}
              type="button"
              className={`grid w-full gap-3 px-5 py-4 text-left transition hover:bg-white/[0.04] ${
                selectedRunId === run.id ? 'bg-teal-400/10' : ''
              }`}
              onClick={() => onSelectRun(run.id)}
            >
              <div className="flex items-center justify-between gap-3">
                <span className="truncate text-sm font-medium text-slate-100">{run.id}</span>
                <StatusBadge status={run.response.generation.successful ? 'success' : 'fail'} />
              </div>
              <div className="grid gap-2 text-xs text-slate-500 sm:grid-cols-3">
                <span className="truncate">{run.targetDirectory}</span>
                <span>{run.model || 'default model'}</span>
                <span>{new Date(run.createdAt).toLocaleString()}</span>
              </div>
            </button>
          ))
        ) : (
          <div className="p-8 text-sm text-slate-500">No runs yet. Execute a run from the Run page.</div>
        )}
      </div>
    </section>
  )
}

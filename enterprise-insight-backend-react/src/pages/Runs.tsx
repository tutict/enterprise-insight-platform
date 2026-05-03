import CodeBlock from '../components/CodeBlock'
import CodeOutput from '../components/CodeOutput'
import ExecutionTimeline from '../components/ExecutionTimeline'
import StatusBadge from '../components/StatusBadge'
import { useHistoryStore } from '../store/historyStore'

function Runs() {
  const runs = useHistoryStore((state) => state.runs)
  const selectedRunId = useHistoryStore((state) => state.selectedRunId)
  const selectRun = useHistoryStore((state) => state.selectRun)
  const selectedRun = runs.find((run) => run.id === selectedRunId) ?? runs[0]

  return (
    <div className="grid gap-5 xl:grid-cols-[minmax(0,0.9fr)_minmax(380px,1fr)]">
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
                  selectedRun?.id === run.id ? 'bg-teal-400/10' : ''
                }`}
                onClick={() => selectRun(run.id)}
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

      <section className="space-y-5">
        {selectedRun ? (
          <>
            <ExecutionTimeline steps={selectedRun.timeline} />
            <div className="panel p-5">
              <h3 className="mb-3 text-sm font-semibold text-slate-100">Generated Output</h3>
              <CodeOutput value={selectedRun.response.generation.finalOutput} />
            </div>
            <div className="panel p-5">
              <h3 className="mb-3 text-sm font-semibold text-slate-100">DSL</h3>
              <CodeBlock value={selectedRun.dsl} collapsible />
            </div>
          </>
        ) : (
          <div className="panel p-8 text-sm text-slate-500">Select a run to inspect details.</div>
        )}
      </section>
    </div>
  )
}

export default Runs

import CodeOutput from '../../../components/CodeOutput'
import ExecutionTimeline from '../../../components/ExecutionTimeline'
import YamlEditor from '../../../components/YamlEditor'
import type { OrchestratorRunResponse } from '../../../api/types'
import type { SavedDsl, TimelineStep } from '../../../store/types'
import type { RunStatus } from '../../../store/runStore'

type RunWorkspaceProps = {
  dslText: string
  setDslText: (value: string) => void
  form: {
    model: string
    targetDirectory: string
    verifyCommand: string
    maxRepairRounds: number
  }
  setForm: {
    setModel: (value: string) => void
    setTargetDirectory: (value: string) => void
    setVerifyCommand: (value: string) => void
    setMaxRepairRounds: (value: number) => void
  }
  savedDsls: SavedDsl[]
  selectDsl: (id: string) => void
  run: () => void
  runState: {
    status: RunStatus
    isRunning: boolean
    error: string
    result: OrchestratorRunResponse | null
    steps: TimelineStep[]
  }
}

export default function RunWorkspace({
  dslText,
  setDslText,
  form,
  setForm,
  savedDsls,
  selectDsl,
  run,
  runState,
}: RunWorkspaceProps) {
  return (
    <div className="grid gap-5 2xl:grid-cols-[minmax(0,1fr)_430px]">
      <section className="space-y-5">
        <div className="panel p-5">
          <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold text-slate-100">Run</h2>
              <p className="muted">POST /api/orchestrator/run</p>
            </div>
            <button
              className="btn-primary"
              type="button"
              onClick={run}
              disabled={runState.isRunning}
            >
              {runState.isRunning ? 'Running...' : 'Run'}
            </button>
          </div>

          <div className="mb-4 grid gap-3 md:grid-cols-4">
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Saved DSL</span>
              <select className="field w-full" onChange={(event) => selectDsl(event.target.value)} defaultValue="">
                <option value="" disabled>
                  {savedDsls.length ? 'Select DSL' : 'No saved DSL'}
                </option>
                {savedDsls.map((dsl) => (
                  <option key={dsl.id} value={dsl.id}>
                    {dsl.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Model</span>
              <input className="field w-full" value={form.model} onChange={(event) => setForm.setModel(event.target.value)} />
            </label>
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Target directory</span>
              <input
                className="field w-full"
                value={form.targetDirectory}
                onChange={(event) => setForm.setTargetDirectory(event.target.value)}
              />
            </label>
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Repair rounds</span>
              <input
                className="field w-full"
                type="number"
                min={0}
                max={5}
                value={form.maxRepairRounds}
                onChange={(event) => setForm.setMaxRepairRounds(Number(event.target.value || 0))}
              />
            </label>
          </div>

          <label className="mb-4 block space-y-2">
            <span className="text-xs font-medium text-slate-400">Verify command</span>
            <input
              className="field w-full"
              value={form.verifyCommand}
              onChange={(event) => setForm.setVerifyCommand(event.target.value)}
            />
          </label>

          <YamlEditor value={dslText} onChange={setDslText} minHeight="360px" />

          {runState.isRunning ? (
            <div className="mt-4 rounded-lg border border-cyan-400/30 bg-cyan-950/60 p-3 text-sm text-cyan-100">
              Run request is in progress. Timeline status will update when the orchestrator returns.
            </div>
          ) : null}

          {runState.error ? (
            <div className="mt-4 rounded-lg border border-red-400/30 bg-red-950/60 p-3 text-sm text-red-100">
              <p className="font-medium">Run failed</p>
              <p className="mt-1 text-red-100/80">{runState.error}</p>
            </div>
          ) : null}
        </div>

        <div className="panel p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-100">Execution Result</h3>
            <span className="rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
              {runState.result?.generation.status ?? runState.status}
            </span>
          </div>
          <CodeOutput
            value={runState.result?.generation.finalOutput ?? ''}
            emptyLabel="Run the orchestrator to see generated code output."
          />
        </div>
      </section>

      <aside className="space-y-5">
        <ExecutionTimeline steps={runState.steps} />
        <section className="panel p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-100">Run Metadata</h3>
          <div className="space-y-3 text-sm text-slate-300">
            <div className="flex justify-between gap-4">
              <span className="text-slate-500">Run ID</span>
              <span className="truncate">{runState.result?.runId ?? '-'}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-slate-500">Attempts</span>
              <span>{runState.result?.generation.totalAttempts ?? '-'}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-slate-500">Project root</span>
              <span className="truncate">{runState.result?.generation.projectRoot ?? '-'}</span>
            </div>
          </div>
        </section>
      </aside>
    </div>
  )
}

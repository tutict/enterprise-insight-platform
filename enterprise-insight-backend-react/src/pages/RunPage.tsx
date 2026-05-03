import { useMemo, useState } from 'react'
import CodeOutput from '../components/CodeOutput'
import ExecutionTimeline from '../components/ExecutionTimeline'
import YamlEditor from '../components/YamlEditor'
import { useHistoryStore } from '../store/historyStore'
import { useRunStore } from '../store/runStore'

function RunPage() {
  const [model, setModel] = useState('llama3.1')
  const [targetDirectory, setTargetDirectory] = useState('generated-harness-app')
  const [verifyCommand, setVerifyCommand] = useState('mvn test')
  const [maxRepairRounds, setMaxRepairRounds] = useState(2)
  const dslText = useRunStore((state) => state.dslText)
  const setDslText = useRunStore((state) => state.setDslText)
  const loadDsl = useRunStore((state) => state.loadDsl)
  const runCurrentDsl = useRunStore((state) => state.runCurrentDsl)
  const runLoading = useRunStore((state) => state.runLoading)
  const runError = useRunStore((state) => state.runError)
  const runResult = useRunStore((state) => state.runResult)
  const timeline = useRunStore((state) => state.timeline)
  const savedDsls = useHistoryStore((state) => state.savedDsls)
  const selectSavedDsl = useHistoryStore((state) => state.selectSavedDsl)

  const command = useMemo(
    () => verifyCommand.split(' ').map((part) => part.trim()).filter(Boolean),
    [verifyCommand],
  )

  const handleRun = () => {
    void runCurrentDsl({
      model,
      targetDirectory,
      verifyCommands: [command.length ? command : ['mvn', 'test']],
      maxRepairRounds,
    })
  }

  const handleSelectDsl = (id: string) => {
    const selected = selectSavedDsl(id)
    if (selected) {
      loadDsl(selected.value)
    }
  }

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
              onClick={handleRun}
              disabled={runLoading}
            >
              {runLoading ? 'Running...' : 'Run'}
            </button>
          </div>

          <div className="mb-4 grid gap-3 md:grid-cols-4">
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Saved DSL</span>
              <select className="field w-full" onChange={(event) => handleSelectDsl(event.target.value)} defaultValue="">
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
              <input className="field w-full" value={model} onChange={(event) => setModel(event.target.value)} />
            </label>
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Target directory</span>
              <input
                className="field w-full"
                value={targetDirectory}
                onChange={(event) => setTargetDirectory(event.target.value)}
              />
            </label>
            <label className="space-y-2">
              <span className="text-xs font-medium text-slate-400">Repair rounds</span>
              <input
                className="field w-full"
                type="number"
                min={0}
                max={5}
                value={maxRepairRounds}
                onChange={(event) => setMaxRepairRounds(Number(event.target.value))}
              />
            </label>
          </div>

          <label className="mb-4 block space-y-2">
            <span className="text-xs font-medium text-slate-400">Verify command</span>
            <input className="field w-full" value={verifyCommand} onChange={(event) => setVerifyCommand(event.target.value)} />
          </label>

          <YamlEditor value={dslText} onChange={setDslText} minHeight="360px" />

          {runLoading ? (
            <div className="mt-4 rounded-lg border border-cyan-400/30 bg-cyan-950/60 p-3 text-sm text-cyan-100">
              Run request is in progress. Timeline status will update when the orchestrator returns.
            </div>
          ) : null}

          {runError ? (
            <div className="mt-4 rounded-lg border border-red-400/30 bg-red-950/60 p-3 text-sm text-red-100">
              <p className="font-medium">Run failed</p>
              <p className="mt-1 text-red-100/80">{runError}</p>
            </div>
          ) : null}
        </div>

        <div className="panel p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-100">Execution Result</h3>
            <span className="rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
              {runResult?.generation.status ?? 'idle'}
            </span>
          </div>
          <CodeOutput
            value={runResult?.generation.finalOutput ?? ''}
            emptyLabel="Run the orchestrator to see generated code output."
          />
        </div>
      </section>

      <aside className="space-y-5">
        <ExecutionTimeline steps={timeline} />
        <section className="panel p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-100">Run Metadata</h3>
          <div className="space-y-3 text-sm text-slate-300">
            <div className="flex justify-between gap-4">
              <span className="text-slate-500">Run ID</span>
              <span className="truncate">{runResult?.runId ?? '-'}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-slate-500">Attempts</span>
              <span>{runResult?.generation.totalAttempts ?? '-'}</span>
            </div>
            <div className="flex justify-between gap-4">
              <span className="text-slate-500">Project root</span>
              <span className="truncate">{runResult?.generation.projectRoot ?? '-'}</span>
            </div>
          </div>
        </section>
      </aside>
    </div>
  )
}

export default RunPage

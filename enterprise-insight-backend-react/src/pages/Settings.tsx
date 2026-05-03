import { getApiBaseUrl } from '../api/client'

function Settings() {
  return (
    <div className="grid gap-5 lg:grid-cols-2">
      <section className="panel p-5">
        <h2 className="text-lg font-semibold text-slate-100">Settings</h2>
        <p className="muted mt-1">Runtime values used by the console client.</p>

        <div className="mt-5 space-y-4">
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">API base URL</span>
            <input className="field w-full" readOnly value={getApiBaseUrl() || 'same-origin / Vite proxy'} />
          </label>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">Compiler endpoint</span>
            <input className="field w-full" readOnly value="/api/compiler/compile" />
          </label>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">Orchestrator endpoint</span>
            <input className="field w-full" readOnly value="/api/orchestrator/run" />
          </label>
        </div>
      </section>

      <section className="panel p-5">
        <h3 className="text-sm font-semibold text-slate-100">Execution Contract</h3>
        <div className="mt-4 space-y-3 text-sm text-slate-300">
          <p>DSL Editor sends YAML text through the compiler request body.</p>
          <p>Run sends the same DSL text to the orchestrator as the requirement field.</p>
          <p>Timeline status is derived from actual orchestrator lifecycle and response fields.</p>
        </div>
      </section>
    </div>
  )
}

export default Settings

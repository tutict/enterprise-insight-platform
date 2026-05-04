type SettingsViewProps = {
  runtimeValues: Array<{
    label: string
    value: string
  }>
  executionContract: string[]
}

export default function SettingsView({ runtimeValues, executionContract }: SettingsViewProps) {
  return (
    <div className="grid gap-5 lg:grid-cols-2">
      <section className="panel p-5">
        <h2 className="text-lg font-semibold text-slate-100">Settings</h2>
        <p className="muted mt-1">Runtime values used by the console client.</p>

        <div className="mt-5 space-y-4">
          {runtimeValues.map((item) => (
            <label key={item.label} className="block space-y-2">
              <span className="text-xs font-medium text-slate-400">{item.label}</span>
              <input className="field w-full" readOnly value={item.value} />
            </label>
          ))}
        </div>
      </section>

      <section className="panel p-5">
        <h3 className="text-sm font-semibold text-slate-100">Execution Contract</h3>
        <div className="mt-4 space-y-3 text-sm text-slate-300">
          {executionContract.map((line) => (
            <p key={line}>{line}</p>
          ))}
        </div>
      </section>
    </div>
  )
}

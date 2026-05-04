import { useMemo, useState } from 'react'
import type { StepStatus, TimelineStep } from '../store/types'
import CodeBlock from './CodeBlock'
import StatusBadge from './StatusBadge'

const stepRingClass: Record<StepStatus, string> = {
  idle: 'border-slate-700 bg-console-950 text-slate-500',
  running: 'border-cyan-300/60 bg-cyan-400/15 text-cyan-100 shadow-[0_0_0_4px_rgba(34,211,238,0.10)]',
  success: 'border-emerald-300/60 bg-emerald-400/15 text-emerald-100',
  fail: 'border-red-300/60 bg-red-400/15 text-red-100',
}

const connectorClass: Record<StepStatus, string> = {
  idle: 'bg-slate-800',
  running: 'bg-cyan-400',
  success: 'bg-emerald-400',
  fail: 'bg-red-400',
}

const getInitialStepKey = (steps: TimelineStep[]) =>
  steps.find((step) => step.status === 'running')?.key ?? steps[0]?.key ?? ''

export default function ExecutionTimeline({ steps }: { steps: TimelineStep[] }) {
  const [selectedStepKey, setSelectedStepKey] = useState<string>(() => getInitialStepKey(steps))
  const selectedStep = useMemo(
    () => steps.find((step) => step.key === selectedStepKey) ?? steps.find((step) => step.status === 'running') ?? steps[0],
    [selectedStepKey, steps],
  )

  if (!steps.length) {
    return (
      <section className="panel p-5">
        <h2 className="text-sm font-semibold text-slate-100">Execution Timeline</h2>
        <div className="mt-4 rounded-lg border border-white/10 bg-console-950 p-4 text-sm text-slate-500">
          No execution steps yet.
        </div>
      </section>
    )
  }

  return (
    <section className="panel p-5">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">Execution Timeline</h2>
          <p className="muted">Stepper status updates from runStore.</p>
        </div>
        <span className="rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
          {selectedStep?.title ?? 'idle'}
        </span>
      </div>

      <div className="grid gap-3 sm:grid-cols-3">
        {steps.map((step, index) => {
          const isSelected = selectedStep?.key === step.key
          const nextStep = steps[index + 1]

          return (
            <button
              key={step.key}
              type="button"
              className={`relative min-w-0 rounded-lg border px-3 py-3 text-left transition hover:bg-white/[0.05] ${
                isSelected ? 'border-teal-400/40 bg-teal-400/[0.07]' : 'border-white/10 bg-white/[0.03]'
              }`}
              aria-pressed={isSelected}
              onClick={() => setSelectedStepKey(step.key)}
            >
              {nextStep ? (
                <span
                  className={`pointer-events-none absolute left-[calc(50%+1rem)] top-7 hidden h-0.5 w-[calc(100%-2rem)] sm:block ${connectorClass[step.status]}`}
                />
              ) : null}
              <div className="relative z-10 flex items-center gap-3">
                <span
                  className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full border text-xs font-semibold ${
                    stepRingClass[step.status]
                  } ${step.status === 'running' ? 'animate-pulse' : ''}`}
                >
                  {index + 1}
                </span>
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-slate-100">{step.title}</p>
                  <div className="mt-1">
                    <StatusBadge status={step.status} />
                  </div>
                </div>
              </div>
            </button>
          )
        })}
      </div>

      <div className="mt-4 rounded-lg border border-white/10 bg-console-950 p-3">
        <div className="mb-3 flex items-center justify-between gap-3">
          <div className="min-w-0">
            <p className="truncate text-sm font-medium text-slate-100">{selectedStep?.title ?? 'Step detail'}</p>
            <p className="mt-1 text-xs text-slate-500">{selectedStep?.status ?? 'idle'}</p>
          </div>
          {selectedStep ? <StatusBadge status={selectedStep.status} /> : null}
        </div>
        <CodeBlock value={selectedStep?.detail ?? ''} emptyLabel="No step detail yet." collapsible />
      </div>
    </section>
  )
}

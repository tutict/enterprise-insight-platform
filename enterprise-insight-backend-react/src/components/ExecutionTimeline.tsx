import { useState } from 'react'
import type { StepStatus } from '../api/types'
import type { TimelineStep } from '../store/types'
import CodeBlock from './CodeBlock'
import StatusBadge from './StatusBadge'

const stepCircleClass: Record<StepStatus, string> = {
  pending: 'border-slate-700 bg-console-950 text-slate-500',
  running: 'border-cyan-300/50 bg-cyan-400/15 text-cyan-100 shadow-[0_0_0_4px_rgba(34,211,238,0.08)]',
  success: 'border-emerald-300/50 bg-emerald-400/15 text-emerald-100',
  fail: 'border-red-300/50 bg-red-400/15 text-red-100',
}

export default function ExecutionTimeline({ steps }: { steps: TimelineStep[] }) {
  const [openStep, setOpenStep] = useState<string>('compile')

  return (
    <section className="panel p-5">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">Execution Timeline</h2>
          <p className="muted">compile / generate / verify</p>
        </div>
      </div>
      <div className="relative">
        {steps.map((step, index) => (
          <div key={step.key} className="relative pb-4 last:pb-0">
            {index < steps.length - 1 ? (
              <div className="absolute left-[1.0625rem] top-9 h-[calc(100%-2.25rem)] w-px bg-white/10" />
            ) : null}
            <button
              type="button"
              className={`relative flex w-full items-start gap-3 rounded-lg border border-white/10 bg-white/[0.03] px-3 py-3 text-left transition hover:bg-white/[0.05] ${
                openStep === step.key ? 'border-teal-400/30 bg-teal-400/[0.06]' : ''
              }`}
              aria-expanded={openStep === step.key}
              onClick={() => setOpenStep(openStep === step.key ? '' : step.key)}
            >
              <span
                className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full border text-xs font-semibold ${
                  stepCircleClass[step.status]
                } ${step.status === 'running' ? 'animate-pulse' : ''}`}
              >
                {index + 1}
              </span>
              <div className="min-w-0 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <p className="text-sm font-medium text-slate-100">{step.title}</p>
                  <StatusBadge status={step.status} />
                </div>
                <p className="mt-1 truncate text-xs text-slate-500">{step.detail}</p>
              </div>
              <span className="shrink-0 rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
                {openStep === step.key ? 'Hide' : 'Details'}
              </span>
            </button>
            {openStep === step.key ? (
              <div className="ml-12 mt-3">
                <CodeBlock value={step.detail} collapsible />
              </div>
            ) : null}
          </div>
        ))}
      </div>
    </section>
  )
}

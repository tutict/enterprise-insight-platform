import { useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { StepStatus, TimelineStep } from '../model/runEvent'
import CodeBlock from '../../../shared/components/CodeBlock'
import StatusBadge from '../../../shared/components/StatusBadge'

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
  const { t } = useTranslation('run')
  const [selectedStepKey, setSelectedStepKey] = useState<string>(() => getInitialStepKey(steps))
  const selectedStep = useMemo(
    () => steps.find((step) => step.key === selectedStepKey) ?? steps.find((step) => step.status === 'running') ?? steps[0],
    [selectedStepKey, steps],
  )
  const getStepLabel = (step: TimelineStep) => t(`steps.${step.key}`)
  const getStepDetail = (step: TimelineStep | undefined) => {
    if (!step) {
      return ''
    }
    if (step.detailKey) {
      return t(step.detailKey, {
        ...step.detailParams,
        step: getStepLabel(step).toLocaleLowerCase(),
      })
    }
    return step.detail ?? ''
  }

  if (!steps.length) {
    return (
      <section className="panel p-5">
        <h2 className="text-sm font-semibold text-slate-100">{t('timeline.title')}</h2>
        <div className="mt-4 rounded-lg border border-white/10 bg-console-950 p-4 text-sm text-slate-500">
          {t('timeline.empty')}
        </div>
      </section>
    )
  }

  return (
    <section className="panel p-5">
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <h2 className="text-sm font-semibold text-slate-100">{t('timeline.title')}</h2>
          <p className="muted">{t('timeline.description')}</p>
        </div>
        <span className="rounded-md border border-white/10 px-2 py-1 text-xs text-slate-400">
          {selectedStep ? getStepLabel(selectedStep) : t('stepStatus.idle')}
        </span>
      </div>

      <div className="grid gap-3 sm:grid-cols-4">
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
                  <p className="truncate text-sm font-medium text-slate-100">{getStepLabel(step)}</p>
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
            <p className="truncate text-sm font-medium text-slate-100">
              {selectedStep ? getStepLabel(selectedStep) : t('timeline.stepDetail')}
            </p>
            <p className="mt-1 text-xs text-slate-500">
              {selectedStep ? t(`stepStatus.${selectedStep.status}`) : t('stepStatus.idle')}
            </p>
          </div>
          {selectedStep ? <StatusBadge status={selectedStep.status} /> : null}
        </div>
        <CodeBlock value={getStepDetail(selectedStep)} emptyLabel={t('timeline.emptyDetail')} collapsible />
        {selectedStep?.attempts?.length ? (
          <div className="mt-3 space-y-2 border-t border-white/10 pt-3">
            {selectedStep.attempts.map((attempt) => (
              <div key={attempt.round} className="flex items-start justify-between gap-3 text-xs text-slate-400">
                <span>{t('repair.round', { round: attempt.round })}</span>
                <StatusBadge status={attempt.status} />
              </div>
            ))}
          </div>
        ) : null}
      </div>
    </section>
  )
}

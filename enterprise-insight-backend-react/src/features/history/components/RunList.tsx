import { useTranslation } from 'react-i18next'
import StatusBadge from '../../../shared/components/StatusBadge'
import type { RunRecord, StepStatus } from '../../run/model/runEvent'

type RunListProps = {
  runs: RunRecord[]
  selectedRunId: string | null
  onSelectRun: (id: string) => void
  isLoading?: boolean
}

export default function RunList({ runs, selectedRunId, onSelectRun, isLoading = false }: RunListProps) {
  const { t } = useTranslation('run')
  const statusForRun = (run: RunRecord): StepStatus => {
    if (run.phase === 'completed' && run.response?.generation.successful) {
      return 'success'
    }
    if (
      run.phase === 'requested' ||
      run.phase === 'compiling' ||
      run.phase === 'generating' ||
      run.phase === 'verifying' ||
      run.phase === 'repairing'
    ) {
      return 'running'
    }
    if (run.phase === 'idle') {
      return 'idle'
    }
    return 'fail'
  }

  return (
    <section className="panel overflow-hidden">
      <div className="border-b border-white/10 p-5">
        <h2 className="text-lg font-semibold text-slate-100">{t('history.title')}</h2>
        <p className="muted">{t('history.description')}</p>
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
                <StatusBadge status={statusForRun(run)} />
              </div>
              <div className="grid gap-2 text-xs text-slate-500 sm:grid-cols-3">
                <span className="truncate">{run.targetDirectory}</span>
                <span>{run.model || t('history.defaultModel')}</span>
                <span>{new Date(run.createdAt).toLocaleString()}</span>
              </div>
            </button>
          ))
        ) : isLoading ? (
          <div className="p-8 text-sm text-slate-500">{t('history.loading')}</div>
        ) : (
          <div className="p-8 text-sm text-slate-500">{t('history.empty')}</div>
        )}
      </div>
    </section>
  )
}

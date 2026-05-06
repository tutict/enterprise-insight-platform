import { useTranslation } from 'react-i18next'
import GraphCanvas from './GraphCanvas'
import type { GraphConnectionState, GraphRunStatus } from '../../../api/types/graph.types'
import GraphEditor from './builder/GraphEditor'
import PromptPreviewPanel from './PromptPreviewPanel'

type GraphRuntimeWorkspaceProps = {
  status: GraphRunStatus
  connectionState: GraphConnectionState
  runId: string | null
  lastEventId?: string
  eventCount: number
  generatedPrompt: string
  isGeneratingPrompt: boolean
  compileGraph: () => void
  generatePrompt: () => void
  runGraph: () => void
  setGeneratedPrompt: (value: string) => void
  copyGeneratedPrompt: () => void
}

export default function GraphRuntimeWorkspace({
  status,
  connectionState,
  runId,
  lastEventId,
  eventCount,
  generatedPrompt,
  isGeneratingPrompt,
  compileGraph,
  generatePrompt,
  runGraph,
  setGeneratedPrompt,
  copyGeneratedPrompt,
}: GraphRuntimeWorkspaceProps) {
  const { t } = useTranslation(['common', 'run'])

  return (
    <div className="space-y-5">
      <section className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-slate-100">{t('graph.builder.title')}</h2>
            <p className="muted">{t('graph.builder.description')}</p>
          </div>
          <div className="flex items-center gap-2">
            <button className="btn-secondary" type="button" onClick={compileGraph}>
              {t('graph.builder.compile')}
            </button>
            <button className="btn-secondary" type="button" onClick={generatePrompt} disabled={isGeneratingPrompt}>
              {isGeneratingPrompt ? t('graph.builder.generatingPrompt') : t('graph.builder.generatePrompt')}
            </button>
            <button className="btn-primary" type="button" onClick={runGraph} disabled={status === 'running'}>
              {status === 'running' ? t('run:run.running') : t('graph.builder.runGraph')}
            </button>
          </div>
        </div>
        <GraphEditor />
      </section>

      <PromptPreviewPanel
        prompt={generatedPrompt}
        setPrompt={setGeneratedPrompt}
        copyPrompt={copyGeneratedPrompt}
      />

      <section className="panel p-5">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-slate-100">{t('graph.runtime.title')}</h2>
            <p className="muted">{t('graph.runtime.description')}</p>
          </div>
        </div>

        <div className="mb-4 grid gap-3 text-xs text-slate-400 md:grid-cols-4">
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            {t('graph.runtime.status', { status: t([`status.${status}`, 'status.unknown'], { status }) })}
          </span>
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            {t('graph.runtime.stream', {
              state: t([`status.${connectionState}`, 'status.unknown'], { status: connectionState }),
            })}
          </span>
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            {t('graph.runtime.event', { eventId: lastEventId ?? '-' })}
          </span>
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            {t('graph.runtime.events', { count: eventCount })}
          </span>
        </div>

        <GraphCanvas />
      </section>

      <section className="panel p-5">
        <h3 className="mb-3 text-sm font-semibold text-slate-100">{t('graph.metadata.title')}</h3>
        <div className="grid gap-3 text-sm text-slate-300 md:grid-cols-2">
          <div className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            <span className="text-slate-500">{t('run:run.runId')}</span>
            <p className="mt-1 truncate">{runId ?? '-'}</p>
          </div>
          <div className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            <span className="text-slate-500">{t('graph.metadata.defaultGraph')}</span>
            <p className="mt-1">{t('graph.metadata.defaultGraphPath')}</p>
          </div>
        </div>
      </section>
    </div>
  )
}

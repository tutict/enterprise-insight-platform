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
  return (
    <div className="space-y-5">
      <section className="space-y-3">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h2 className="text-lg font-semibold text-slate-100">Graph Builder</h2>
            <p className="muted">Build nodes and edges; execution stays in the backend graph runtime.</p>
          </div>
          <div className="flex items-center gap-2">
            <button className="btn-secondary" type="button" onClick={compileGraph}>
              Compile
            </button>
            <button className="btn-secondary" type="button" onClick={generatePrompt} disabled={isGeneratingPrompt}>
              {isGeneratingPrompt ? 'Generating...' : 'Generate Prompt'}
            </button>
            <button className="btn-primary" type="button" onClick={runGraph} disabled={status === 'running'}>
              {status === 'running' ? 'Running...' : 'Run graph'}
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
            <h2 className="text-lg font-semibold text-slate-100">Execution Runtime</h2>
            <p className="muted">DAG, conditional edge, and repair loop runtime.</p>
          </div>
        </div>

        <div className="mb-4 grid gap-3 text-xs text-slate-400 md:grid-cols-4">
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">status: {status}</span>
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">stream: {connectionState}</span>
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">event: {lastEventId ?? '-'}</span>
          <span className="rounded-md border border-white/10 bg-console-950 px-3 py-2">events: {eventCount}</span>
        </div>

        <GraphCanvas />
      </section>

      <section className="panel p-5">
        <h3 className="mb-3 text-sm font-semibold text-slate-100">Graph Metadata</h3>
        <div className="grid gap-3 text-sm text-slate-300 md:grid-cols-2">
          <div className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            <span className="text-slate-500">Run ID</span>
            <p className="mt-1 truncate">{runId ?? '-'}</p>
          </div>
          <div className="rounded-md border border-white/10 bg-console-950 px-3 py-2">
            <span className="text-slate-500">Default graph</span>
            <p className="mt-1">compile -&gt; generate -&gt; verify -&gt; repair loop</p>
          </div>
        </div>
      </section>
    </div>
  )
}

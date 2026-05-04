import type { BuilderEdge, BuilderNode } from '../../store/graphBuilderStore'

type ConfigPanelProps = {
  node: BuilderNode | null
  edge: BuilderEdge | null
  updateNode: (id: string, patch: Partial<BuilderNode['data']>) => void
  updateNodeConfig: (id: string, patch: Record<string, unknown>) => void
  updateEdge: (id: string, patch: Partial<NonNullable<BuilderEdge['data']>>) => void
  deleteSelected: () => void
}

export default function ConfigPanel({
  node,
  edge,
  updateNode,
  updateNodeConfig,
  updateEdge,
  deleteSelected,
}: ConfigPanelProps) {
  if (edge) {
    return (
      <aside className="space-y-4 border-t border-white/10 bg-console-900/80 p-4 lg:border-l lg:border-t-0">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h3 className="text-sm font-semibold text-slate-100">Edge</h3>
            <p className="text-xs text-slate-500">{edge.id}</p>
          </div>
          <button className="btn-secondary px-2 py-1 text-xs" type="button" onClick={deleteSelected}>
            Delete
          </button>
        </div>
        <label className="grid gap-2">
          <span className="text-xs text-slate-400">Condition</span>
          <select
            className="field"
            value={edge.data?.condition ?? 'success'}
            onChange={(event) => updateEdge(edge.id, { condition: event.target.value })}
          >
            <option value="always">always</option>
            <option value="success">success</option>
            <option value="failed">failed</option>
          </select>
        </label>
        <label className="grid gap-2">
          <span className="text-xs text-slate-400">Max iterations</span>
          <input
            className="field"
            type="number"
            min={0}
            value={edge.data?.maxIterations ?? ''}
            onChange={(event) =>
              updateEdge(edge.id, {
                maxIterations: event.target.value ? Number(event.target.value) : undefined,
              })
            }
          />
        </label>
      </aside>
    )
  }

  if (!node) {
    return (
      <aside className="border-t border-white/10 bg-console-900/80 p-4 text-sm text-slate-500 lg:border-l lg:border-t-0">
        Select a node or edge to configure it.
      </aside>
    )
  }

  const config = node.data.config

  return (
    <aside className="space-y-4 border-t border-white/10 bg-console-900/80 p-4 lg:border-l lg:border-t-0">
      <div className="flex items-center justify-between gap-3">
        <div>
          <h3 className="text-sm font-semibold text-slate-100">Node</h3>
          <p className="text-xs text-slate-500">{node.id}</p>
        </div>
        <button className="btn-secondary px-2 py-1 text-xs" type="button" onClick={deleteSelected}>
          Delete
        </button>
      </div>

      <label className="grid gap-2">
        <span className="text-xs text-slate-400">Label</span>
        <input className="field" value={node.data.label} onChange={(event) => updateNode(node.id, { label: event.target.value })} />
      </label>

      {node.data.type === 'llm' ? (
        <>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">Model</span>
            <input
              className="field"
              value={String(config.model ?? '')}
              onChange={(event) => updateNodeConfig(node.id, { model: event.target.value })}
            />
          </label>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">Prompt</span>
            <textarea
              className="field min-h-28"
              value={String(config.prompt ?? '')}
              onChange={(event) => updateNodeConfig(node.id, { prompt: event.target.value })}
            />
          </label>
        </>
      ) : null}

      {node.data.type === 'tool' ? (
        <>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">Method</span>
            <select
              className="field"
              value={String(config.method ?? 'POST')}
              onChange={(event) => updateNodeConfig(node.id, { method: event.target.value })}
            >
              <option>GET</option>
              <option>POST</option>
              <option>PUT</option>
              <option>DELETE</option>
            </select>
          </label>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">API URL</span>
            <input
              className="field"
              value={String(config.url ?? '')}
              onChange={(event) => updateNodeConfig(node.id, { url: event.target.value })}
            />
          </label>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">Runtime effect</span>
            <select
              className="field"
              value={String(config.effect ?? 'http')}
              onChange={(event) => updateNodeConfig(node.id, { effect: event.target.value })}
            >
              <option value="http">HTTP request</option>
              <option value="repair-loop">Repair loop</option>
            </select>
          </label>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">Body</span>
            <textarea
              className="field min-h-24"
              value={String(config.body ?? '')}
              onChange={(event) => updateNodeConfig(node.id, { body: event.target.value })}
            />
          </label>
        </>
      ) : null}

      {node.data.type === 'condition' ? (
        <>
          <label className="grid gap-2">
            <span className="text-xs text-slate-400">Expression</span>
            <textarea
              className="field min-h-24"
              value={String(config.expression ?? '')}
              onChange={(event) => updateNodeConfig(node.id, { expression: event.target.value })}
            />
          </label>
          <label className="flex items-center gap-2 text-sm text-slate-300">
            <input
              type="checkbox"
              checked={Boolean(config.pass ?? true)}
              onChange={(event) => updateNodeConfig(node.id, { pass: event.target.checked })}
            />
            Evaluate as success
          </label>
        </>
      ) : null}
    </aside>
  )
}

import type { BuilderNodeType } from '../../../../api/types/graph.types'

type NodePaletteProps = {
  addNode: (type: BuilderNodeType) => void
}

export const BUILDER_NODE_MIME_TYPE = 'application/eip-builder-node'

const nodeTypes: BuilderNodeType[] = ['start', 'llm', 'tool', 'condition', 'end']

export default function NodePalette({ addNode }: NodePaletteProps) {
  return (
    <div className="space-y-2">
      <p className="text-xs font-medium uppercase text-slate-500">Nodes</p>
      <div className="grid gap-2">
        {nodeTypes.map((type) => (
          <button
            key={type}
            className="btn-secondary justify-start gap-2 px-3 py-2"
            type="button"
            draggable
            onClick={() => addNode(type)}
            onDragStart={(event) => {
              event.dataTransfer.setData(BUILDER_NODE_MIME_TYPE, type)
              event.dataTransfer.effectAllowed = 'copy'
            }}
          >
            <span className="h-2 w-2 rounded-full bg-teal-300" aria-hidden="true" />
            {type}
          </button>
        ))}
      </div>
    </div>
  )
}

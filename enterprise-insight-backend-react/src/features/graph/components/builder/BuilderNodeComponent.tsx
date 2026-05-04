import { Handle, Position, type NodeProps } from '@xyflow/react'
import type { BuilderNode } from '../../store/graphBuilderStore'

const typeClass: Record<string, string> = {
  start: 'border-teal-300/60 bg-teal-950/70 text-teal-100',
  llm: 'border-cyan-300/60 bg-cyan-950/70 text-cyan-100',
  tool: 'border-amber-300/60 bg-amber-950/70 text-amber-100',
  condition: 'border-fuchsia-300/60 bg-fuchsia-950/70 text-fuchsia-100',
  end: 'border-emerald-300/60 bg-emerald-950/70 text-emerald-100',
}

export default function BuilderNodeComponent({ data, selected }: NodeProps<BuilderNode>) {
  return (
    <div
      className={`min-w-44 rounded-lg border px-4 py-3 shadow-sm ${
        typeClass[data.type] ?? 'border-slate-700 bg-console-950 text-slate-300'
      } ${selected ? 'ring-2 ring-teal-300/60' : ''}`}
    >
      <Handle type="target" position={Position.Left} isConnectable />
      <p className="truncate text-sm font-semibold text-slate-100">{data.label}</p>
      <p className="mt-1 text-xs uppercase text-current">{data.type}</p>
      <Handle type="source" position={Position.Right} isConnectable />
    </div>
  )
}

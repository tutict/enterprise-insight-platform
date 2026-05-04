import { Handle, Position, type Node, type NodeProps } from '@xyflow/react'
import type { GraphNodeStatus } from '../../../api/types/graph.types'

export type GraphFlowNodeData = {
  label: string
  type: string
  status: GraphNodeStatus
  detail?: string
}

export type GraphFlowNode = Node<GraphFlowNodeData, 'graphNode'>

const nodeClass: Record<GraphNodeStatus, string> = {
  idle: 'border-slate-700 bg-console-950 text-slate-400',
  running: 'border-cyan-300/60 bg-cyan-950/80 text-cyan-100 shadow-[0_0_0_4px_rgba(34,211,238,0.10)]',
  success: 'border-emerald-300/60 bg-emerald-950/70 text-emerald-100',
  fail: 'border-red-300/60 bg-red-950/70 text-red-100',
}

export default function GraphNodeComponent({ data }: NodeProps<GraphFlowNode>) {
  return (
    <div className={`min-w-44 rounded-lg border px-4 py-3 ${nodeClass[data.status]}`}>
      <Handle type="target" position={Position.Left} isConnectable={false} />
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold text-slate-100">{data.label}</p>
          <p className="mt-1 text-xs uppercase text-current">{data.status}</p>
        </div>
        {data.status === 'running' ? <span className="mt-1 h-3 w-3 animate-ping rounded-full bg-cyan-300" /> : null}
      </div>
      <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-400">{data.detail ?? data.type}</p>
      <Handle type="source" position={Position.Right} isConnectable={false} />
    </div>
  )
}

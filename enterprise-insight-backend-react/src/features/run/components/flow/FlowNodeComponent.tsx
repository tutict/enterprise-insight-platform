import { Handle, Position, type Node, type NodeProps } from '@xyflow/react'

export type FlowNodeStatus = 'idle' | 'running' | 'success' | 'fail'

export type FlowNodeData = {
  label: string
  status: FlowNodeStatus
  detail: string
}

export type WorkflowNode = Node<FlowNodeData, 'workflow'>

const nodeStateClass: Record<FlowNodeStatus, string> = {
  idle: 'border-slate-700 bg-console-950 text-slate-400',
  running: 'border-cyan-300/60 bg-cyan-950/80 text-cyan-100 shadow-[0_0_0_4px_rgba(34,211,238,0.10)]',
  success: 'border-emerald-300/60 bg-emerald-950/70 text-emerald-100',
  fail: 'border-red-300/60 bg-red-950/70 text-red-100',
}

const indicatorClass: Record<FlowNodeStatus, string> = {
  idle: 'bg-slate-500',
  running: 'bg-cyan-300',
  success: 'bg-emerald-300',
  fail: 'bg-red-300',
}

export default function FlowNodeComponent({ data }: NodeProps<WorkflowNode>) {
  return (
    <div className={`min-w-44 rounded-lg border px-4 py-3 ${nodeStateClass[data.status]}`}>
      <Handle
        type="target"
        position={Position.Left}
        className="!h-2 !w-2 !border-slate-900 !bg-slate-500"
        isConnectable={false}
      />
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="truncate text-sm font-semibold text-slate-100">{data.label}</p>
          <p className="mt-1 text-xs uppercase tracking-wide text-current">{data.status}</p>
        </div>
        <span className="relative mt-1 flex h-3 w-3 shrink-0">
          {data.status === 'running' ? (
            <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-cyan-300 opacity-70" />
          ) : null}
          <span className={`relative inline-flex h-3 w-3 rounded-full ${indicatorClass[data.status]}`} />
        </span>
      </div>
      <p className="mt-3 line-clamp-2 text-xs leading-5 text-slate-400">{data.detail}</p>
      <Handle
        type="source"
        position={Position.Right}
        className="!h-2 !w-2 !border-slate-900 !bg-slate-500"
        isConnectable={false}
      />
    </div>
  )
}

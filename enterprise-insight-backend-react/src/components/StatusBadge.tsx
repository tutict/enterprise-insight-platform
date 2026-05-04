import type { StepStatus } from '../store/types'

const statusClass: Record<StepStatus, string> = {
  idle: 'border-slate-600 bg-slate-800 text-slate-300',
  running: 'border-cyan-400/30 bg-cyan-950/70 text-cyan-200',
  success: 'border-emerald-400/30 bg-emerald-950/70 text-emerald-200',
  fail: 'border-red-400/30 bg-red-950/70 text-red-200',
}

export default function StatusBadge({ status }: { status: StepStatus }) {
  return (
    <span className={`inline-flex items-center rounded-md border px-2 py-1 text-xs font-medium ${statusClass[status]}`}>
      {status}
    </span>
  )
}

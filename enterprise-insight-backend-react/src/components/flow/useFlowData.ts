import { useMemo } from 'react'
import { MarkerType, type Edge } from '@xyflow/react'
import { useRunStore } from '../../store/runStore'
import type { ExecutionPhase, StepStatus, TimelineStep } from '../../store/types'
import type { FlowNodeStatus, WorkflowNode } from './FlowNodeComponent'

const NODE_WIDTH = 176
const NODE_HEIGHT = 96
const HORIZONTAL_GAP = 260
const CANVAS_Y = 80

const toFlowStatus = (status: StepStatus): FlowNodeStatus => {
  return status
}

const edgeStrokeForStatus = (status: FlowNodeStatus) => {
  if (status === 'success') {
    return '#34d399'
  }
  if (status === 'fail') {
    return '#f87171'
  }
  if (status === 'running') {
    return '#22d3ee'
  }
  return '#334155'
}

const isActiveGraphPhase = (phase: ExecutionPhase) =>
  phase === 'compiling' || phase === 'generating' || phase === 'verifying' || phase === 'repairing'

export function createWorkflowGraph(steps: TimelineStep[], phase: ExecutionPhase) {
  const nodes: WorkflowNode[] = steps.map((step, index) => {
    const flowStatus = toFlowStatus(step.status)

    return {
      id: step.key,
      type: 'workflow',
      position: {
        x: index * HORIZONTAL_GAP,
        y: CANVAS_Y,
      },
      data: {
        label: step.title,
        status: flowStatus,
        detail: step.detail ?? '',
      },
      width: NODE_WIDTH,
      height: NODE_HEIGHT,
      draggable: false,
      selectable: false,
    }
  })

  const edges: Edge[] = steps.slice(0, -1).map((step, index) => {
    const next = steps[index + 1]
    const sourceStatus = toFlowStatus(step.status)
    const targetStatus = toFlowStatus(next.status)
    const stroke = edgeStrokeForStatus(targetStatus === 'idle' ? sourceStatus : targetStatus)

    return {
      id: `${step.key}-${next.key}`,
      source: step.key,
      target: next.key,
      type: 'smoothstep',
      animated: isActiveGraphPhase(phase) && (sourceStatus === 'running' || targetStatus === 'running'),
      markerEnd: {
        type: MarkerType.ArrowClosed,
        color: stroke,
      },
      style: {
        stroke,
        strokeWidth: 2,
      },
    }
  })

  return { nodes, edges }
}

export function useFlowData() {
  const execution = useRunStore((state) => state.execution)

  return useMemo(
    () => createWorkflowGraph(execution.steps, execution.phase),
    [execution.phase, execution.steps],
  )
}

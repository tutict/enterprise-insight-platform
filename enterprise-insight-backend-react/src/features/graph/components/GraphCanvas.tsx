import { useMemo } from 'react'
import {
  Background,
  BackgroundVariant,
  Controls,
  MarkerType,
  ReactFlow,
  type Edge,
  type NodeTypes,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { useGraphStore } from '../store/graphStore'
import GraphNodeComponent, { type GraphFlowNode } from './GraphNodeComponent'

const NODE_X_GAP = 260
const NODE_Y_GAP = 150

const nodeTypes: NodeTypes = {
  graphNode: GraphNodeComponent,
}

const edgeColor = (status: 'idle' | 'traversed') => (status === 'traversed' ? '#22d3ee' : '#334155')

const computeLevels = (startNodeId: string | undefined, edges: { source: string; target: string }[]) => {
  const levels: Record<string, number> = {}
  const queue: string[] = []

  if (startNodeId) {
    levels[startNodeId] = 0
    queue.push(startNodeId)
  }

  while (queue.length) {
    const source = queue.shift()
    if (!source) {
      continue
    }
    const level = levels[source] ?? 0

    edges
      .filter((edge) => edge.source === source)
      .forEach((edge) => {
        const nextLevel = level + 1
        if (levels[edge.target] === undefined || levels[edge.target] < nextLevel) {
          levels[edge.target] = nextLevel
          if (nextLevel < 8) {
            queue.push(edge.target)
          }
        }
      })
  }

  return levels
}

export default function GraphCanvas() {
  const definition = useGraphStore((state) => state.definition)
  const runtimeNodes = useGraphStore((state) => state.nodes)
  const runtimeEdges = useGraphStore((state) => state.edges)

  const { nodes, edges } = useMemo(() => {
    if (!definition) {
      return { nodes: [] as GraphFlowNode[], edges: [] as Edge[] }
    }

    const levels = computeLevels(definition.startNodeId, definition.edges)
    const levelRows: Record<number, number> = {}

    const nodes: GraphFlowNode[] = definition.nodes.map((node, index) => {
      const runtimeNode = runtimeNodes[node.id]
      const configuredPosition = node.config?.position as { x?: number; y?: number } | undefined
      const level = levels[node.id] ?? index
      const row = levelRows[level] ?? 0
      levelRows[level] = row + 1

      return {
        id: node.id,
        type: 'graphNode',
        position: {
          x: typeof configuredPosition?.x === 'number' ? configuredPosition.x : level * NODE_X_GAP,
          y: typeof configuredPosition?.y === 'number' ? configuredPosition.y : row * NODE_Y_GAP,
        },
        data: {
          label: node.label,
          type: node.type,
          status: runtimeNode?.status ?? 'idle',
          detail: runtimeNode?.detail,
        },
        draggable: false,
        selectable: false,
      }
    })

    const edges: Edge[] = definition.edges.map((edge) => {
      const runtimeEdge = runtimeEdges[edge.id]
      const stroke = edgeColor(runtimeEdge?.status ?? 'idle')

      return {
        id: edge.id,
        source: edge.source,
        target: edge.target,
        type: edge.source === edge.target ? 'smoothstep' : 'smoothstep',
        animated: runtimeEdge?.status === 'traversed',
        label: edge.label ?? edge.condition,
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
  }, [definition, runtimeEdges, runtimeNodes])

  return (
    <div className="h-96 overflow-hidden rounded-lg border border-white/10 bg-console-950">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        fitView
        fitViewOptions={{ padding: 0.25 }}
        minZoom={0.5}
        maxZoom={1.4}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={false}
        proOptions={{ hideAttribution: true }}
      >
        <Background color="#334155" gap={24} size={1} variant={BackgroundVariant.Dots} />
        <Controls showInteractive={false} position="bottom-right" />
      </ReactFlow>
    </div>
  )
}

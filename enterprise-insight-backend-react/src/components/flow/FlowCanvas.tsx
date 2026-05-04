import {
  Background,
  BackgroundVariant,
  Controls,
  ReactFlow,
  type Edge,
  type NodeTypes,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import FlowNodeComponent, { type WorkflowNode } from './FlowNodeComponent'

type FlowCanvasProps = {
  nodes: WorkflowNode[]
  edges: Edge[]
  className?: string
}

const nodeTypes: NodeTypes = {
  workflow: FlowNodeComponent,
}

export default function FlowCanvas({ nodes, edges, className = '' }: FlowCanvasProps) {
  return (
    <div className={`h-72 overflow-hidden rounded-lg border border-white/10 bg-console-950 ${className}`}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        fitView
        fitViewOptions={{ padding: 0.25 }}
        minZoom={0.6}
        maxZoom={1.4}
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={false}
        panOnScroll
        proOptions={{ hideAttribution: true }}
      >
        <Background color="#334155" gap={24} size={1} variant={BackgroundVariant.Dots} />
        <Controls showInteractive={false} position="bottom-right" />
      </ReactFlow>
    </div>
  )
}

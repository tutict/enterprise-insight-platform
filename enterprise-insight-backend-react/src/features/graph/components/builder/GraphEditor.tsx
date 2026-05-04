import {
  Background,
  BackgroundVariant,
  Controls,
  ReactFlow,
  ReactFlowProvider,
  useReactFlow,
  type NodeTypes,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { useCallback, useRef } from 'react'
import type { DragEvent } from 'react'
import type { BuilderNodeType } from '../../../../api/types/graph.types'
import { useGraphBuilderStore } from '../../store/graphBuilderStore'
import BuilderNodeComponent from './BuilderNodeComponent'
import ConfigPanel from './ConfigPanel'
import NodePalette, { BUILDER_NODE_MIME_TYPE } from './NodePalette'

const nodeTypes: NodeTypes = {
  builderNode: BuilderNodeComponent,
}

const builderNodeTypes = new Set<BuilderNodeType>(['start', 'llm', 'tool', 'condition', 'end'])

const isBuilderNodeType = (value: string): value is BuilderNodeType =>
  builderNodeTypes.has(value as BuilderNodeType)

function GraphEditorContent() {
  const flowWrapperRef = useRef<HTMLDivElement>(null)
  const { screenToFlowPosition } = useReactFlow()
  const nodes = useGraphBuilderStore((state) => state.nodes)
  const edges = useGraphBuilderStore((state) => state.edges)
  const selectedNodeId = useGraphBuilderStore((state) => state.selectedNodeId)
  const selectedEdgeId = useGraphBuilderStore((state) => state.selectedEdgeId)
  const validation = useGraphBuilderStore((state) => state.validation)
  const onNodesChange = useGraphBuilderStore((state) => state.onNodesChange)
  const onEdgesChange = useGraphBuilderStore((state) => state.onEdgesChange)
  const connect = useGraphBuilderStore((state) => state.connect)
  const addNode = useGraphBuilderStore((state) => state.addNode)
  const selectNode = useGraphBuilderStore((state) => state.selectNode)
  const selectEdge = useGraphBuilderStore((state) => state.selectEdge)
  const updateNode = useGraphBuilderStore((state) => state.updateNode)
  const updateNodeConfig = useGraphBuilderStore((state) => state.updateNodeConfig)
  const updateEdge = useGraphBuilderStore((state) => state.updateEdge)
  const deleteSelected = useGraphBuilderStore((state) => state.deleteSelected)

  const selectedNode = nodes.find((node) => node.id === selectedNodeId) ?? null
  const selectedEdge = edges.find((edge) => edge.id === selectedEdgeId) ?? null

  const handleDragOver = useCallback((event: DragEvent<HTMLDivElement>) => {
    event.preventDefault()
    event.dataTransfer.dropEffect = 'copy'
  }, [])

  const handleDrop = useCallback(
    (event: DragEvent<HTMLDivElement>) => {
      event.preventDefault()

      const type = event.dataTransfer.getData(BUILDER_NODE_MIME_TYPE)
      if (!isBuilderNodeType(type) || !flowWrapperRef.current) {
        return
      }

      addNode(
        type,
        screenToFlowPosition({
          x: event.clientX,
          y: event.clientY,
        }),
      )
    },
    [addNode, screenToFlowPosition],
  )

  return (
    <section className="overflow-hidden rounded-lg border border-white/10 bg-console-900/85">
      <div className="grid min-h-[620px] grid-cols-1 lg:grid-cols-[180px_minmax(0,1fr)_320px]">
        <aside className="border-b border-white/10 p-4 lg:border-b-0 lg:border-r">
          <NodePalette addNode={addNode} />
          <div className="mt-6 space-y-2">
            <p className="text-xs font-medium uppercase text-slate-500">Validation</p>
            <div className="rounded-md border border-white/10 bg-console-950 p-3 text-xs">
              <p className={validation.valid ? 'text-emerald-300' : 'text-red-300'}>
                {validation.valid ? 'valid' : 'invalid'}
              </p>
              {[...validation.errors, ...validation.warnings].slice(0, 5).map((message) => (
                <p key={message} className="mt-2 text-slate-400">
                  {message}
                </p>
              ))}
            </div>
          </div>
        </aside>

        <div
          ref={flowWrapperRef}
          className="min-h-[520px] min-w-0"
          onDragOver={handleDragOver}
          onDrop={handleDrop}
        >
          <ReactFlow
            nodes={nodes}
            edges={edges}
            nodeTypes={nodeTypes}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onConnect={connect}
            onNodeClick={(_, node) => selectNode(node.id)}
            onEdgeClick={(_, edge) => selectEdge(edge.id)}
            onPaneClick={() => {
              selectNode(null)
              selectEdge(null)
            }}
            deleteKeyCode={['Backspace', 'Delete']}
            fitView
            proOptions={{ hideAttribution: true }}
          >
            <Background color="#334155" gap={24} size={1} variant={BackgroundVariant.Dots} />
            <Controls showInteractive={false} position="bottom-right" />
          </ReactFlow>
        </div>

        <ConfigPanel
          node={selectedNode}
          edge={selectedEdge}
          updateNode={updateNode}
          updateNodeConfig={updateNodeConfig}
          updateEdge={updateEdge}
          deleteSelected={deleteSelected}
        />
      </div>
    </section>
  )
}

export default function GraphEditor() {
  return (
    <ReactFlowProvider>
      <GraphEditorContent />
    </ReactFlowProvider>
  )
}

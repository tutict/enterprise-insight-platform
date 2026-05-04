import { create } from 'zustand'
import {
  addEdge,
  applyEdgeChanges,
  applyNodeChanges,
  type Connection,
  type Edge,
  type EdgeChange,
  type Node,
  type NodeChange,
  type XYPosition,
} from '@xyflow/react'
import type { BuilderNodeType, GraphDefinition } from '../../../api/types/graph.types'

const GRAPH_MAX_ITERATIONS = 3

export type BuilderNodeData = {
  label: string
  type: BuilderNodeType
  config: Record<string, unknown>
}

export type BuilderNode = Node<BuilderNodeData, 'builderNode'>

export type BuilderEdgeData = {
  condition: string
  maxIterations?: number
}

export type BuilderEdge = Edge<BuilderEdgeData>

type ValidationResult = {
  valid: boolean
  errors: string[]
  warnings: string[]
}

type GraphBuilderState = {
  nodes: BuilderNode[]
  edges: BuilderEdge[]
  selectedNodeId: string | null
  selectedEdgeId: string | null
  validation: ValidationResult
  onNodesChange: (changes: NodeChange<BuilderNode>[]) => void
  onEdgesChange: (changes: EdgeChange<BuilderEdge>[]) => void
  connect: (connection: Connection) => void
  addNode: (type: BuilderNodeType, position?: XYPosition) => void
  deleteSelected: () => void
  selectNode: (id: string | null) => void
  selectEdge: (id: string | null) => void
  updateNode: (id: string, patch: Partial<BuilderNodeData>) => void
  updateNodeConfig: (id: string, patch: Record<string, unknown>) => void
  updateEdge: (id: string, patch: Partial<BuilderEdgeData>) => void
  loadGraph: (graph: GraphDefinition) => void
  toGraphDefinition: () => GraphDefinition
}

const defaultNodes: BuilderNode[] = [
  createNode('start', { x: 0, y: 80 }, 'start'),
  createNode('llm', { x: 260, y: 80 }, 'generate'),
  createNode('condition', { x: 520, y: 80 }, 'verify', {
    expression: 'repairIterations >= requiredRepairIterations',
  }),
  createNode('tool', { x: 780, y: 190 }, 'repair', {
    effect: 'repair-loop',
    url: '/api/tool/repair',
  }),
  createNode('end', { x: 780, y: 20 }, 'end'),
]

const defaultEdges: BuilderEdge[] = [
  createEdge('start-generate', 'start', 'generate', 'success'),
  createEdge('generate-verify', 'generate', 'verify', 'success'),
  createEdge('verify-end', 'verify', 'end', 'success'),
  createEdge('verify-repair', 'verify', 'repair', 'failed', 2),
  createEdge('repair-generate', 'repair', 'generate', 'success', 2),
]

function createNode(
  type: BuilderNodeType,
  position: XYPosition,
  id = `${type}-${crypto.randomUUID().slice(0, 8)}`,
  config: Record<string, unknown> = {},
): BuilderNode {
  return {
    id,
    type: 'builderNode',
    position,
    data: {
      label: id,
      type,
      config: {
        ...defaultConfig(type),
        ...config,
      },
    },
  }
}

function createEdge(
  id: string,
  source: string,
  target: string,
  condition = 'success',
  maxIterations?: number,
): BuilderEdge {
  return {
    id,
    source,
    target,
    type: 'smoothstep',
    animated: false,
    label: condition,
    data: {
      condition,
      maxIterations,
    },
  }
}

function defaultConfig(type: BuilderNodeType) {
  if (type === 'llm') {
    return { model: 'llama3.1', prompt: 'Generate implementation from upstream context.' }
  }
  if (type === 'tool') {
    return { method: 'POST', url: '/api/tool', body: '{}', effect: 'http' }
  }
  if (type === 'condition') {
    return { expression: 'true' }
  }
  return {}
}

const isBoundedLoopEdge = (edge: BuilderEdge) =>
  typeof edge.data?.maxIterations === 'number' && edge.data.maxIterations > 0

const hasCycle = (nodes: BuilderNode[], edges: BuilderEdge[]) => {
  const visiting = new Set<string>()
  const visited = new Set<string>()
  const outgoing = new Map<string, BuilderEdge[]>()

  edges.forEach((edge) => {
    outgoing.set(edge.source, [...(outgoing.get(edge.source) ?? []), edge])
  })

  const visit = (nodeId: string): boolean => {
    if (visited.has(nodeId)) {
      return false
    }
    if (visiting.has(nodeId)) {
      return true
    }

    visiting.add(nodeId)
    const cycle = (outgoing.get(nodeId) ?? []).some((edge) => visit(edge.target))
    visiting.delete(nodeId)
    visited.add(nodeId)
    return cycle
  }

  return nodes.some((node) => visit(node.id))
}

const reachableFrom = (startNodeId: string | undefined, edges: BuilderEdge[]) => {
  const reachable = new Set<string>()
  const outgoing = new Map<string, BuilderEdge[]>()

  edges.forEach((edge) => {
    outgoing.set(edge.source, [...(outgoing.get(edge.source) ?? []), edge])
  })

  const visit = (nodeId: string) => {
    if (reachable.has(nodeId)) {
      return
    }
    reachable.add(nodeId)
    ;(outgoing.get(nodeId) ?? []).forEach((edge) => visit(edge.target))
  }

  if (startNodeId) {
    visit(startNodeId)
  }

  return reachable
}

const validate = (nodes: BuilderNode[], edges: BuilderEdge[]): ValidationResult => {
  const errors: string[] = []
  const warnings: string[] = []
  const nodeIds = new Set(nodes.map((node) => node.id))
  const startNodes = nodes.filter((node) => node.data.type === 'start')
  const endNodes = nodes.filter((node) => node.data.type === 'end')

  if (startNodes.length !== 1) {
    errors.push('Graph must contain exactly one start node.')
  }
  if (!endNodes.length) {
    errors.push('Graph must contain at least one end node.')
  }

  edges.forEach((edge) => {
    if (!nodeIds.has(edge.source)) {
      errors.push(`Edge ${edge.id} has missing source ${edge.source}.`)
    }
    if (!nodeIds.has(edge.target)) {
      errors.push(`Edge ${edge.id} has missing target ${edge.target}.`)
    }
    if (edge.data?.maxIterations !== undefined) {
      if (edge.data.maxIterations <= 0) {
        errors.push(`Edge ${edge.id} maxIterations must be greater than zero.`)
      }
      if (edge.data.maxIterations > GRAPH_MAX_ITERATIONS) {
        warnings.push(`Edge ${edge.id} maxIterations exceeds graph maxIterations.`)
      }
    }
    if (edge.source === edge.target && !isBoundedLoopEdge(edge)) {
      errors.push(`Loop edge ${edge.id} needs maxIterations.`)
    }
  })

  nodes
    .filter((node) => node.data.type === 'condition')
    .forEach((node) => {
      const outgoing = edges.filter((edge) => edge.source === node.id)
      if (!outgoing.some((edge) => edge.data?.condition === 'success')) {
        errors.push(`Condition node ${node.id} must define a success branch.`)
      }
      if (!outgoing.some((edge) => edge.data?.condition === 'failed' || edge.data?.condition === 'failure')) {
        errors.push(`Condition node ${node.id} must define a failed branch.`)
      }
      if (!String(node.data.config.expression ?? '').trim()) {
        warnings.push(`Condition node ${node.id} has no expression.`)
      }
    })

  if (hasCycle(nodes, edges.filter((edge) => !isBoundedLoopEdge(edge)))) {
    errors.push('Graph must be a DAG after removing edges guarded by maxIterations.')
  }

  if (startNodes[0]) {
    const reachable = reachableFrom(startNodes[0].id, edges)
    nodes
      .filter((node) => !reachable.has(node.id))
      .forEach((node) => warnings.push(`Node ${node.id} is not reachable from start.`))
  }

  return {
    valid: errors.length === 0,
    errors,
    warnings,
  }
}

const normalizeState = (state: Pick<GraphBuilderState, 'nodes' | 'edges'>) => ({
  validation: validate(state.nodes, state.edges),
})

export const useGraphBuilderStore = create<GraphBuilderState>((set, get) => ({
  nodes: defaultNodes,
  edges: defaultEdges,
  selectedNodeId: null,
  selectedEdgeId: null,
  validation: validate(defaultNodes, defaultEdges),
  onNodesChange: (changes) =>
    set((state) => {
      const nodes = applyNodeChanges(changes, state.nodes)
      const nodeIds = new Set(nodes.map((node) => node.id))
      const edges = state.edges.filter((edge) => nodeIds.has(edge.source) && nodeIds.has(edge.target))
      const selectedNodeId = state.selectedNodeId && nodeIds.has(state.selectedNodeId) ? state.selectedNodeId : null
      const selectedEdgeId =
        state.selectedEdgeId && edges.some((edge) => edge.id === state.selectedEdgeId)
          ? state.selectedEdgeId
          : null
      return { nodes, edges, selectedNodeId, selectedEdgeId, ...normalizeState({ nodes, edges }) }
    }),
  onEdgesChange: (changes) =>
    set((state) => {
      const edges = applyEdgeChanges(changes, state.edges)
      const selectedEdgeId =
        state.selectedEdgeId && edges.some((edge) => edge.id === state.selectedEdgeId)
          ? state.selectedEdgeId
          : null
      return { edges, selectedEdgeId, ...normalizeState({ nodes: state.nodes, edges }) }
    }),
  connect: (connection) =>
    set((state) => {
      const sourceNode = state.nodes.find((node) => node.id === connection.source)
      const sourceConditions = new Set(
        state.edges
          .filter((edge) => edge.source === connection.source)
          .map((edge) => edge.data?.condition),
      )
      const condition = sourceNode?.data.type === 'condition' && sourceConditions.has('success') ? 'failed' : 'success'
      const edge: BuilderEdge = {
        ...connection,
        id: `${connection.source}-${connection.target}-${crypto.randomUUID().slice(0, 6)}`,
        type: 'smoothstep',
        label: condition,
        data: { condition },
      } as BuilderEdge
      const edges = addEdge(edge, state.edges)
      return { edges, ...normalizeState({ nodes: state.nodes, edges }) }
    }),
  addNode: (type, position = { x: 120, y: 120 }) =>
    set((state) => {
      const node = createNode(type, position)
      const nodes = [...state.nodes, node]
      return { nodes, selectedNodeId: node.id, ...normalizeState({ nodes, edges: state.edges }) }
    }),
  deleteSelected: () =>
    set((state) => {
      const selectedNodeId = state.selectedNodeId
      const selectedEdgeId = state.selectedEdgeId
      const nodes = selectedNodeId ? state.nodes.filter((node) => node.id !== selectedNodeId) : state.nodes
      const edges = state.edges.filter(
        (edge) =>
          edge.id !== selectedEdgeId &&
          edge.source !== selectedNodeId &&
          edge.target !== selectedNodeId,
      )
      return {
        nodes,
        edges,
        selectedNodeId: null,
        selectedEdgeId: null,
        ...normalizeState({ nodes, edges }),
      }
    }),
  selectNode: (id) => set({ selectedNodeId: id, selectedEdgeId: null }),
  selectEdge: (id) => set({ selectedEdgeId: id, selectedNodeId: null }),
  updateNode: (id, patch) =>
    set((state) => {
      const nodes = state.nodes.map((node) =>
        node.id === id ? { ...node, data: { ...node.data, ...patch } } : node,
      )
      return { nodes, ...normalizeState({ nodes, edges: state.edges }) }
    }),
  updateNodeConfig: (id, patch) =>
    set((state) => {
      const nodes = state.nodes.map((node) =>
        node.id === id
          ? {
              ...node,
              data: {
                ...node.data,
                config: { ...node.data.config, ...patch },
              },
            }
          : node,
      )
      return { nodes, ...normalizeState({ nodes, edges: state.edges }) }
    }),
  updateEdge: (id, patch) =>
    set((state) => {
      const edges = state.edges.map((edge) =>
        edge.id === id
          ? {
              ...edge,
              label: patch.condition ?? edge.label,
              data: {
                ...edge.data,
                ...patch,
                condition: patch.condition ?? edge.data?.condition ?? 'success',
              },
            }
          : edge,
      )
      return { edges, ...normalizeState({ nodes: state.nodes, edges }) }
    }),
  loadGraph: (graph) =>
    set(() => {
      const nodes: BuilderNode[] = graph.nodes.map((node, index) => {
        const position = node.config?.position as XYPosition | undefined
        return {
          id: node.id,
          type: 'builderNode',
          position: position ?? { x: index * 220, y: 100 },
          data: {
            label: node.label,
            type: node.type as BuilderNodeType,
            config: node.config ?? defaultConfig(node.type as BuilderNodeType),
          },
        }
      })
      const edges: BuilderEdge[] = graph.edges.map((edge) =>
        createEdge(edge.id, edge.source, edge.target, edge.condition, edge.maxIterations),
      )
      return { nodes, edges, selectedNodeId: null, selectedEdgeId: null, ...normalizeState({ nodes, edges }) }
    }),
  toGraphDefinition: () => {
    const { nodes, edges } = get()
    const startNode = nodes.find((node) => node.data.type === 'start') ?? nodes[0]
    return {
      id: 'visual-builder-graph',
      name: 'Visual Builder Graph',
      startNodeId: startNode?.id ?? '',
      maxIterations: Math.max(
        GRAPH_MAX_ITERATIONS,
        ...edges.map((edge) => edge.data?.maxIterations ?? 0),
      ),
      nodes: nodes.map((node) => ({
        id: node.id,
        label: node.data.label,
        type: node.data.type,
        config: {
          ...node.data.config,
          position: node.position,
        },
      })),
      edges: edges.map((edge) => ({
        id: edge.id,
        source: edge.source,
        target: edge.target,
        condition: edge.data?.condition ?? 'success',
        maxIterations: edge.data?.maxIterations,
        label: String(edge.label ?? edge.data?.condition ?? ''),
      })),
      metadata: {
        source: 'visual-builder',
        requiredRepairIterations: 1,
      },
    }
  },
}))

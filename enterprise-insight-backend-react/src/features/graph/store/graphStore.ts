import { create } from 'zustand'
import type {
  GraphConnectionState,
  GraphDefinition,
  GraphEdgeDefinition,
  GraphEvent,
  GraphNodeDefinition,
  GraphNodeStatus,
  GraphRunStatus,
} from '../../../api/types/graph.types'

export type GraphRuntimeNode = GraphNodeDefinition & {
  status: GraphNodeStatus
  detail?: string
}

export type GraphRuntimeEdge = GraphEdgeDefinition & {
  status: 'idle' | 'traversed'
  iterations: number
}

type GraphState = {
  runId: string | null
  status: GraphRunStatus
  connectionState: GraphConnectionState
  lastEventId?: string
  definition: GraphDefinition | null
  nodes: Record<string, GraphRuntimeNode>
  edges: Record<string, GraphRuntimeEdge>
  eventLog: GraphEvent[]
  dispatch: (event: GraphEvent) => void
  reset: () => void
}

const createInitialState = (): Omit<GraphState, 'dispatch' | 'reset'> => ({
  runId: null,
  status: 'idle',
  connectionState: 'disconnected',
  definition: null,
  nodes: {},
  edges: {},
  eventLog: [],
})

const getPayloadString = (event: GraphEvent, key: string) => {
  const value = event.payload?.[key]
  if (typeof value === 'string') {
    return value
  }
  if (value && typeof value === 'object' && 'detail' in value) {
    return String(value.detail)
  }
  return undefined
}

const createNodes = (definition: GraphDefinition) =>
  Object.fromEntries(
    definition.nodes.map((node) => [
      node.id,
      {
        ...node,
        status: 'idle' as const,
        detail: 'Waiting for graph event.',
      },
    ]),
  )

const createEdges = (definition: GraphDefinition) =>
  Object.fromEntries(
    definition.edges.map((edge) => [
      edge.id,
      {
        ...edge,
        status: 'idle' as const,
        iterations: 0,
      },
    ]),
  )

const reducer = (state: GraphState, event: GraphEvent): Omit<GraphState, 'dispatch' | 'reset'> => {
  const lastEventId = event.eventId ?? state.lastEventId

  switch (event.type) {
    case 'STREAM_CONNECTING':
      return { ...state, runId: event.runId, connectionState: 'connecting', lastEventId }
    case 'STREAM_CONNECTED':
      return { ...state, runId: event.runId, connectionState: 'connected', lastEventId }
    case 'STREAM_RECONNECTING':
      return { ...state, runId: event.runId, connectionState: 'reconnecting', lastEventId }
    case 'STREAM_DISCONNECTED':
      return { ...state, runId: event.runId, connectionState: 'disconnected', lastEventId }
    case 'GRAPH_RUN_STARTED': {
      const definition = event.payload?.definition as GraphDefinition | undefined
      return {
        ...state,
        runId: event.runId,
        status: 'running',
        connectionState: 'connected',
        lastEventId,
        definition: definition ?? state.definition,
        nodes: definition ? createNodes(definition) : state.nodes,
        edges: definition ? createEdges(definition) : state.edges,
        eventLog: [...state.eventLog, event],
      }
    }
    case 'NODE_STARTED':
      return {
        ...state,
        runId: event.runId,
        status: 'running',
        lastEventId,
        nodes: {
          ...state.nodes,
          ...(event.nodeId
            ? {
                [event.nodeId]: {
                  ...state.nodes[event.nodeId],
                  status: 'running' as const,
                  detail: 'Node is running.',
                },
              }
            : {}),
        },
        eventLog: [...state.eventLog, event],
      }
    case 'NODE_SUCCEEDED':
    case 'NODE_FAILED':
      return {
        ...state,
        runId: event.runId,
        lastEventId,
        nodes: {
          ...state.nodes,
          ...(event.nodeId
            ? {
                [event.nodeId]: {
                  ...state.nodes[event.nodeId],
                  status: event.type === 'NODE_SUCCEEDED' ? 'success' : 'fail',
                  detail:
                    getPayloadString(event, 'detail') ??
                    getPayloadString(event, 'status') ??
                    getPayloadString(event, 'result'),
                },
              }
            : {}),
        },
        eventLog: [...state.eventLog, event],
      }
    case 'EDGE_TRAVERSED':
      return {
        ...state,
        runId: event.runId,
        lastEventId,
        edges: {
          ...state.edges,
          ...(event.edgeId
            ? {
                [event.edgeId]: {
                  ...state.edges[event.edgeId],
                  status: 'traversed' as const,
                  iterations: Number(event.payload?.iteration ?? state.edges[event.edgeId]?.iterations ?? 0),
                },
              }
            : {}),
        },
        eventLog: [...state.eventLog, event],
      }
    case 'GRAPH_RUN_COMPLETED':
    case 'GRAPH_RUN_FAILED':
      return {
        ...state,
        runId: event.runId,
        status: event.type === 'GRAPH_RUN_COMPLETED' ? 'completed' : 'failed',
        connectionState: 'disconnected',
        lastEventId,
        eventLog: [...state.eventLog, event],
      }
    default:
      return { ...state, runId: event.runId, lastEventId, eventLog: [...state.eventLog, event] }
  }
}

export const useGraphStore = create<GraphState>((set) => ({
  ...createInitialState(),
  dispatch: (event) => set((state) => reducer(state, event)),
  reset: () => set(createInitialState()),
}))

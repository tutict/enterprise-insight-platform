import { getGraphStreamUrl } from '../../../api/modules/graph.api'
import type { GraphEvent } from '../../../api/types/graph.types'

const GRAPH_EVENT_TYPES = [
  'GRAPH_RUN_STARTED',
  'NODE_STARTED',
  'NODE_SUCCEEDED',
  'NODE_FAILED',
  'EDGE_TRAVERSED',
  'GRAPH_RUN_COMPLETED',
  'GRAPH_RUN_FAILED',
]

const TERMINAL_EVENTS = new Set(['GRAPH_RUN_COMPLETED', 'GRAPH_RUN_FAILED'])

const createDeferred = () => {
  let resolve!: () => void
  const promise = new Promise<void>((next) => {
    resolve = next
  })
  return { promise, resolve }
}

export async function* graphStream(
  runId: string,
  resumeFromLastEventId?: string,
): AsyncGenerator<GraphEvent> {
  const queue: GraphEvent[] = []
  let closed = false
  let lastEventId = resumeFromLastEventId
  let deferred = createDeferred()

  const push = (event: GraphEvent) => {
    if (event.eventId) {
      lastEventId = event.eventId
    }
    queue.push(event)
    deferred.resolve()
  }

  push({ type: 'STREAM_CONNECTING', runId, eventId: lastEventId })

  const source = new EventSource(getGraphStreamUrl(runId, lastEventId))

  source.onopen = () => {
    push({ type: 'STREAM_CONNECTED', runId, eventId: lastEventId })
  }

  const handleMessage = (message: MessageEvent<string>) => {
    const event = {
      ...(JSON.parse(message.data) as GraphEvent),
      eventId: message.lastEventId || undefined,
    }
    push(event)

    if (TERMINAL_EVENTS.has(event.type)) {
      closed = true
      source.close()
      deferred.resolve()
    }
  }

  source.onmessage = handleMessage
  GRAPH_EVENT_TYPES.forEach((type) => {
    source.addEventListener(type, (event) => handleMessage(event as MessageEvent<string>))
  })

  source.onerror = () => {
    if (closed) {
      return
    }

    push({
      type: source.readyState === EventSource.CLOSED ? 'STREAM_DISCONNECTED' : 'STREAM_RECONNECTING',
      runId,
      eventId: lastEventId,
      payload: {
        error: 'Graph stream connection interrupted.',
      },
    })
  }

  try {
    while (!closed || queue.length) {
      if (!queue.length) {
        await deferred.promise
        deferred = createDeferred()
      }

      while (queue.length) {
        const event = queue.shift()
        if (event) {
          yield event
        }
      }
    }
  } finally {
    closed = true
    source.close()
  }
}

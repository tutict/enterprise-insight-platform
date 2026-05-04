import type { RunEvent } from '../model/runEvent'

type EventSource = AsyncIterable<RunEvent> | Iterable<RunEvent>

const stampEvent = (event: RunEvent): RunEvent =>
  event.timestamp
    ? event
    : {
        ...event,
        timestamp: new Date().toISOString(),
      }

const isAsyncIterable = (source: EventSource): source is AsyncIterable<RunEvent> =>
  Symbol.asyncIterator in source

export async function* runEventStream(source: EventSource): AsyncGenerator<RunEvent> {
  if (isAsyncIterable(source)) {
    for await (const event of source) {
      yield stampEvent(event)
    }
    return
  }

  for (const event of source) {
    yield stampEvent(event)
  }
}

export function createCallbackEventStream(
  subscribe: (emit: (event: RunEvent) => void) => () => void,
): AsyncGenerator<RunEvent> {
  const queue: RunEvent[] = []
  let notify: (() => void) | null = null
  let closed = false
  const unsubscribe = subscribe((event) => {
    queue.push(event)
    notify?.()
  })

  async function* stream() {
    try {
      while (!closed) {
        if (!queue.length) {
          await new Promise<void>((resolve) => {
            notify = resolve
          })
          notify = null
        }

        while (queue.length) {
          const event = queue.shift()
          if (event) {
            yield stampEvent(event)
          }
        }
      }
    } finally {
      closed = true
      unsubscribe()
    }
  }

  return stream()
}

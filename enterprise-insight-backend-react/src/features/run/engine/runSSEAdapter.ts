import { getRunStreamUrl } from '../../../api/modules/runtimeEvents.api'
import type { OrchestratorRunResponse } from '../../../api/types/orchestrator.types'
import type { RunConfig } from '../model/runConfig'
import type { RunEvent, StepKey } from '../model/runEvent'

type BackendRunEvent = {
  runId: string
  type: string
  step?: StepKey
  timestamp?: number | string
  payload?: Record<string, unknown>
}

const TERMINAL_EVENTS = new Set(['RUN_COMPLETED', 'RUN_FAILED', 'RUN_CANCELLED'])
const RUN_EVENT_TYPES = [
  'RUN_REQUESTED',
  'STEP_STARTED',
  'STEP_SUCCEEDED',
  'STEP_FAILED',
  'RUN_COMPLETED',
  'RUN_FAILED',
  'RUN_CANCELLED',
  'RUN_PAUSED',
  'RUN_RESUMED',
  'STEP_RETRY_REQUESTED',
]

const getPayloadString = (payload: Record<string, unknown> | undefined, key: string) => {
  const value = payload?.[key]
  return typeof value === 'string' ? value : undefined
}

const normalizeRunEvent = (event: BackendRunEvent, eventId?: string): RunEvent => {
  const payload = event.payload ?? {}
  const base = {
    runId: event.runId,
    timestamp: event.timestamp,
    eventId,
  }

  switch (event.type) {
    case 'RUN_REQUESTED':
      return {
        ...base,
        type: 'RUN_REQUESTED',
        config: payload.config as RunConfig | undefined,
      }
    case 'STEP_STARTED':
      return {
        ...base,
        type: 'STEP_STARTED',
        step: event.step ?? 'compile',
        detail: getPayloadString(payload, 'summary'),
      }
    case 'STEP_SUCCEEDED':
      return {
        ...base,
        type: 'STEP_SUCCEEDED',
        step: event.step ?? 'compile',
        payload,
        detail: getPayloadString(payload, 'summary') ?? getPayloadString(payload, 'output'),
      }
    case 'STEP_FAILED':
      return {
        ...base,
        type: 'STEP_FAILED',
        step: event.step ?? 'compile',
        payload,
        error: getPayloadString(payload, 'error') ?? getPayloadString(payload, 'summary') ?? `${event.step} failed.`,
      }
    case 'RUN_COMPLETED':
      return {
        ...base,
        type: 'RUN_COMPLETED',
        result: payload.result as OrchestratorRunResponse | undefined,
        payload,
      }
    case 'RUN_FAILED':
      return {
        ...base,
        type: 'RUN_FAILED',
        error: getPayloadString(payload, 'error') ?? 'Run failed.',
        result: payload.result as OrchestratorRunResponse | undefined,
        payload,
      }
    case 'RUN_PAUSED':
      return {
        ...base,
        type: 'RUN_PAUSED',
        reason: getPayloadString(payload, 'reason'),
      }
    case 'RUN_RESUMED':
      return {
        ...base,
        type: 'RUN_RESUMED',
      }
    case 'RUN_CANCELLED':
      return {
        ...base,
        type: 'RUN_CANCELLED',
        error: getPayloadString(payload, 'error') ?? 'Run cancelled.',
      }
    case 'STEP_RETRY_REQUESTED':
      return {
        ...base,
        type: 'STEP_RETRY_REQUESTED',
        step: event.step ?? 'verify',
        reason: getPayloadString(payload, 'reason'),
      }
    default:
      return {
        ...base,
        type: 'STREAM_RECONNECTING',
        error: `Unsupported event: ${event.type}`,
      }
  }
}

const createDeferred = () => {
  let resolve!: () => void
  const promise = new Promise<void>((next) => {
    resolve = next
  })
  return { promise, resolve }
}

export async function* createRunStream(
  runId: string,
  resumeFromLastEventId?: string,
): AsyncGenerator<RunEvent> {
  const queue: RunEvent[] = []
  let closed = false
  let lastEventId = resumeFromLastEventId
  let deferred = createDeferred()

  const push = (event: RunEvent) => {
    if (event.eventId) {
      lastEventId = event.eventId
    }
    queue.push(event)
    deferred.resolve()
  }

  push({ type: 'STREAM_CONNECTING', runId, lastEventId })

  const source = new EventSource(getRunStreamUrl(runId, lastEventId))

  source.onopen = () => {
    push({ type: 'STREAM_CONNECTED', runId, lastEventId })
  }

  const handleMessage = (message: MessageEvent<string>) => {
    const event = normalizeRunEvent(
      JSON.parse(message.data) as BackendRunEvent,
      message.lastEventId || undefined,
    )
    push(event)

    if (TERMINAL_EVENTS.has(event.type)) {
      closed = true
      source.close()
      deferred.resolve()
    }
  }

  source.onmessage = handleMessage
  RUN_EVENT_TYPES.forEach((type) => {
    source.addEventListener(type, (event) => handleMessage(event as MessageEvent<string>))
  })

  source.onerror = () => {
    if (closed) {
      return
    }

    push({
      type: source.readyState === EventSource.CLOSED ? 'STREAM_DISCONNECTED' : 'STREAM_RECONNECTING',
      runId,
      lastEventId,
      error: 'Execution stream connection interrupted.',
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

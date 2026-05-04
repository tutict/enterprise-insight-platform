import { apiRequest, getApiBaseUrl } from '../client'
import type { OrchestratorRunRequest } from '../types/orchestrator.types'
import type { ControlEvent, RunStartResponse, RuntimeEventEnvelope } from '../types/runtime.types'

type RuntimeEventHandler = (event: RuntimeEventEnvelope) => void

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

export function getRunStreamUrl(runId: string, lastEventId?: string) {
  const baseUrl = getApiBaseUrl()
  const path = `/api/orchestrator/run/stream/${encodeURIComponent(runId)}`
  const url = new URL(`${baseUrl}${path}`, window.location.origin)

  if (lastEventId) {
    url.searchParams.set('lastEventId', lastEventId)
  }

  return url.toString()
}

export function createRunEventSource(
  runId: string,
  onEvent: RuntimeEventHandler,
  lastEventId?: string,
) {
  const source = new EventSource(getRunStreamUrl(runId, lastEventId))

  const handleMessage = (message: MessageEvent<string>) => {
    onEvent({
      ...(JSON.parse(message.data) as RuntimeEventEnvelope),
      eventId: message.lastEventId || undefined,
    })
  }

  source.onmessage = handleMessage
  RUN_EVENT_TYPES.forEach((type) => {
    source.addEventListener(type, (event) => handleMessage(event as MessageEvent<string>))
  })

  return () => source.close()
}

export function startRunStream(request: OrchestratorRunRequest) {
  return apiRequest<RunStartResponse>('/api/orchestrator/run/start', {
    method: 'POST',
    body: request,
  })
}

export function sendRunControlEvent(event: ControlEvent) {
  return apiRequest<void>('/api/orchestrator/run/control', {
    method: 'POST',
    body: event,
  })
}

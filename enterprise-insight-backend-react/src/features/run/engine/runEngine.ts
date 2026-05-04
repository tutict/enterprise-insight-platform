import { createRunStream } from './runSSEAdapter'
import { runEventStream } from './runEventStream'
import type { RunEngineConfig } from '../model/runConfig'
import type { RunEvent, StepKey } from '../model/runEvent'

export async function* runEngine(runId: string): AsyncGenerator<RunEvent> {
  yield* createRunStream(runId)
}

export async function* runEngineReplay(eventLog: RunEvent[]): AsyncGenerator<RunEvent> {
  yield* runEventStream(eventLog)
}

export async function* retryStep(
  config: RunEngineConfig,
  step: StepKey,
  attempt: number,
): AsyncGenerator<RunEvent> {
  yield* runEventStream([
    {
      type: 'STEP_RETRY_REQUESTED',
      step,
      attempt,
      runId: config.runId,
      reason: `Retrying ${step}.`,
    },
  ])
  if (config.runId) {
    yield* runEngine(config.runId)
  }
}

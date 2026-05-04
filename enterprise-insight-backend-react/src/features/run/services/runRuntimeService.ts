import { sendRunControlEvent, startRunStream } from '../../../api/modules/runtimeEvents.api'
import type { ControlEvent } from '../../../api/types/runtime.types'
import { splitVerifyCommand, type RunEngineConfig } from '../model/runConfig'

export function startRunExecutionStream(config: RunEngineConfig) {
  const verifyCommand = splitVerifyCommand(config.verifyCommand)

  return startRunStream({
    runId: config.runId,
    requirement: config.dsl ?? '',
    model: config.model || undefined,
    targetDirectory: config.targetDirectory,
    verifyCommands: [verifyCommand],
    maxRepairRounds: config.maxRepairRounds,
  })
}

export function sendRunControl(event: ControlEvent) {
  return sendRunControlEvent(event)
}

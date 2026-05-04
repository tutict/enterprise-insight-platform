import { useMemo, useState } from 'react'
import { useHistoryStore } from '../store/historyStore'
import { useRunStore } from '../store/runStore'

const DEFAULT_VERIFY_COMMAND = ['mvn', 'test']

const splitVerifyCommand = (value: string) =>
  value
    .split(' ')
    .map((part) => part.trim())
    .filter(Boolean)

export function useRunPage() {
  const [model, setModel] = useState('llama3.1')
  const [targetDirectory, setTargetDirectory] = useState('generated-harness-app')
  const [verifyCommand, setVerifyCommand] = useState(DEFAULT_VERIFY_COMMAND.join(' '))
  const [maxRepairRounds, setMaxRepairRounds] = useState(2)

  const dslText = useRunStore((state) => state.dslText)
  const setDslText = useRunStore((state) => state.setDslText)
  const loadDsl = useRunStore((state) => state.loadDsl)
  const runCurrentDsl = useRunStore((state) => state.runCurrentDsl)
  const status = useRunStore((state) => state.status)
  const error = useRunStore((state) => state.error)
  const result = useRunStore((state) => state.result)
  const steps = useRunStore((state) => state.steps)

  const savedDsls = useHistoryStore((state) => state.savedDsls)
  const selectSavedDsl = useHistoryStore((state) => state.selectSavedDsl)

  const verifyCommandParts = useMemo(() => splitVerifyCommand(verifyCommand), [verifyCommand])

  const run = () => {
    void runCurrentDsl({
      model: model.trim() || undefined,
      targetDirectory,
      verifyCommands: [verifyCommandParts.length ? verifyCommandParts : DEFAULT_VERIFY_COMMAND],
      maxRepairRounds,
    })
  }

  const selectDsl = (id: string) => {
    const selected = selectSavedDsl(id)
    if (selected) {
      loadDsl(selected.value)
    }
  }

  return {
    dslText,
    setDslText,
    form: {
      model,
      targetDirectory,
      verifyCommand,
      maxRepairRounds,
    },
    setForm: {
      setModel,
      setTargetDirectory,
      setVerifyCommand,
      setMaxRepairRounds,
    },
    savedDsls,
    selectDsl,
    run,
    runState: {
      status,
      isRunning: status === 'running',
      error,
      result,
      steps,
    },
  }
}

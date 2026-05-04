import { useState } from 'react'
import { useHistoryStore } from '../store/historyStore'
import { useRunStore } from '../store/runStore'

export function useDslEditorPage() {
  const [name, setName] = useState('')
  const dslText = useRunStore((state) => state.dslText)
  const setDslText = useRunStore((state) => state.setDslText)
  const compileCurrentDsl = useRunStore((state) => state.compileCurrentDsl)
  const prompt = useRunStore((state) => state.prompt)
  const compileStatus = useRunStore((state) => state.compileStatus)
  const compileError = useRunStore((state) => state.compileError)
  const compileResult = useRunStore((state) => state.compileResult)
  const saveDsl = useHistoryStore((state) => state.saveDsl)

  const save = () => {
    saveDsl(name, dslText)
    if (dslText.trim()) {
      setName('')
    }
  }

  const compile = () => {
    void compileCurrentDsl()
  }

  return {
    name,
    setName,
    dslText,
    setDslText,
    compile,
    save,
    compilerState: {
      prompt,
      status: compileStatus,
      isCompiling: compileStatus === 'loading',
      error: compileError,
      result: compileResult,
    },
  }
}

import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { compileDsl } from '../../../api/modules/compiler.api'
import { useHistoryStore } from '../../history/store/historyStore'
import { useNotificationStore } from '../../../store/uiStore'
import { useDslStore } from '../store/dslStore'

export function useDslEditorPage() {
  const { t } = useTranslation('dsl')
  const [name, setName] = useState('')
  const dslText = useDslStore((state) => state.dslText)
  const setDslText = useDslStore((state) => state.setDslText)
  const prompt = useDslStore((state) => state.prompt)
  const compileStatus = useDslStore((state) => state.compileStatus)
  const compileError = useDslStore((state) => state.compileError)
  const compileResult = useDslStore((state) => state.compileResult)
  const compileStarted = useDslStore((state) => state.compileStarted)
  const compileSucceeded = useDslStore((state) => state.compileSucceeded)
  const compileFailed = useDslStore((state) => state.compileFailed)
  const saveDsl = useHistoryStore((state) => state.saveDsl)
  const pushNotification = useNotificationStore((state) => state.push)

  const save = () => {
    if (!dslText.trim()) {
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: t('editor.emptySave'),
      })
      return
    }

    saveDsl(name, dslText)
    pushNotification({
      id: crypto.randomUUID(),
      type: 'success',
      message: t('editor.saved', { name: name.trim() || t('editor.defaultName') }),
    })
    setName('')
  }

  const compile = async () => {
    if (!dslText.trim()) {
      compileFailed(t('editor.emptyEditor'))
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message: t('editor.emptyCompile'),
      })
      return
    }

    compileStarted()
    pushNotification({
      id: crypto.randomUUID(),
      type: 'info',
      message: t('editor.compiling'),
    })

    try {
      const result = await compileDsl(dslText)
      compileSucceeded(result)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: t('editor.compiledSuccessfully'),
      })
    } catch (err) {
      const message = err instanceof Error ? err.message : t('editor.failed')
      compileFailed(message)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message,
      })
    }
  }

  return {
    name,
    setName,
    dslText,
    setDslText,
    compile: () => void compile(),
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

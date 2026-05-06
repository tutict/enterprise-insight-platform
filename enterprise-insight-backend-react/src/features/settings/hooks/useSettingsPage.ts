import { getApiBaseUrl } from '../../../api/client'
import { useNotificationStore } from '../../../store/uiStore'

export function useSettingsPage() {
  const language = useNotificationStore((state) => state.language)
  const setLanguage = useNotificationStore((state) => state.setLanguage)

  return {
    language,
    setLanguage,
    runtimeValues: [
      {
        labelKey: 'settings.runtimeLabels.apiBaseUrl',
        value: getApiBaseUrl(),
        valueKey: 'settings.values.sameOriginProxy',
      },
      {
        labelKey: 'settings.runtimeLabels.compilerEndpoint',
        value: '/api/compiler/compile',
      },
      {
        labelKey: 'settings.runtimeLabels.orchestratorEndpoint',
        value: '/api/orchestrator/run',
      },
    ],
    executionContract: [
      'settings.contract.compiler',
      'settings.contract.orchestrator',
      'settings.contract.timeline',
    ],
  }
}

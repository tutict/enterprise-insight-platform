import { useEffect, useState } from 'react'
import { getApiBaseUrl } from '../api/client'

type ApiState = 'checking' | 'ok' | 'down'

type ApiStatus = {
  state: ApiState
  latencyMs?: number
  message?: string
}

export default function useApiStatus() {
  const [status, setStatus] = useState<ApiStatus>({ state: 'checking' })

  useEffect(() => {
    const controller = new AbortController()
    const baseUrl = getApiBaseUrl()
    const healthUrl = `${baseUrl}/actuator/health`
    const startedAt = Date.now()
    const timeoutId = window.setTimeout(() => controller.abort(), 4000)

    const check = async () => {
      try {
        const response = await fetch(healthUrl, { signal: controller.signal })
        const latencyMs = Date.now() - startedAt
        if (!response.ok) {
          setStatus({
            state: 'down',
            latencyMs,
            message: `HTTP ${response.status}`,
          })
          return
        }

        let message = 'OK'
        try {
          const data = (await response.json()) as { status?: string }
          if (data?.status) {
            message = data.status
          }
        } catch {
          message = 'Online'
        }

        setStatus({ state: 'ok', latencyMs, message })
      } catch (error) {
        if ((error as Error).name === 'AbortError') {
          setStatus({ state: 'down', message: 'Timeout' })
          return
        }
        setStatus({ state: 'down', message: 'Unreachable' })
      }
    }

    check()

    return () => {
      controller.abort()
      window.clearTimeout(timeoutId)
    }
  }, [])

  return status
}

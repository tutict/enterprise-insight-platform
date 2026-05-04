import { type FormEvent, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { useNotificationStore } from '../store/notifications'

type LocationState = {
  from?: { pathname?: string }
}

export function useLoginPage() {
  const { login, isAuthenticated } = useAuth()
  const pushNotification = useNotificationStore((state) => state.push)
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as LocationState | null
  const redirectPath = state?.from?.pathname ?? '/'
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setIsLoading(true)
    try {
      await login(username.trim(), password.trim())
      pushNotification({
        id: crypto.randomUUID(),
        type: 'success',
        message: 'Signed in successfully.',
      })
      navigate(redirectPath, { replace: true })
    } catch (err) {
      const message = (err as Error).message || 'Login failed'
      setError(message)
      pushNotification({
        id: crypto.randomUUID(),
        type: 'error',
        message,
      })
    } finally {
      setIsLoading(false)
    }
  }

  return {
    isAuthenticated,
    redirectPath,
    form: {
      username,
      password,
      error,
      isLoading,
    },
    setForm: {
      setUsername,
      setPassword,
    },
    submit,
  }
}

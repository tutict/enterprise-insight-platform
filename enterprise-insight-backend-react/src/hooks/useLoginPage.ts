import { type FormEvent, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

type LocationState = {
  from?: { pathname?: string }
}

export function useLoginPage() {
  const { login, isAuthenticated } = useAuth()
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
      navigate(redirectPath, { replace: true })
    } catch (err) {
      setError((err as Error).message || 'Login failed')
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

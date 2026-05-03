import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

type LocationState = {
  from?: { pathname?: string }
}

function Login() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const state = location.state as LocationState | null
  const redirectPath = state?.from?.pathname ?? '/'
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={redirectPath} replace />
  }

  const onSubmit = async (event: React.FormEvent) => {
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

  return (
    <div className="grid min-h-screen place-items-center bg-console-950 px-5 text-slate-100">
      <section className="panel w-full max-w-md p-6">
        <div className="mb-6">
          <p className="text-xs uppercase tracking-wide text-teal-300">AI Orchestrator</p>
          <h1 className="mt-2 text-2xl font-semibold">Sign in</h1>
          <p className="muted mt-2">Access the compiler, orchestrator, and run history.</p>
        </div>

        <form className="space-y-4" onSubmit={onSubmit}>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">Username</span>
            <input
              className="field w-full"
              type="text"
              placeholder="admin"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </label>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">Password</span>
            <input
              className="field w-full"
              type="password"
              placeholder="******"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          {error ? <p className="text-sm text-red-300">{error}</p> : null}
          <button className="btn-primary w-full" type="submit" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="mt-5 text-xs text-slate-500">Demo backend accepts any password. Use admin for admin role.</p>
      </section>
    </div>
  )
}

export default Login

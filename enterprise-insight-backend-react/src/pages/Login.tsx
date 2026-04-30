import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

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
    <div className="login-shell">
      <div className="login-panel">
        <div className="login-header">
          <p className="eyebrow">Welcome back</p>
          <h1>AI Harness Compiler</h1>
          <p className="muted">
            Sign in to compile DSL into prompts and run agent code generation.
          </p>
        </div>

        <form className="login-form" onSubmit={onSubmit}>
          <label>
            Username
            <input
              type="text"
              placeholder="admin"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              placeholder="******"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          {error ? <p className="form-error">{error}</p> : null}
          <button className="primary-btn" type="submit" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <div className="login-hint">
          <p>
            Demo: any username works. Use <strong>admin</strong> for admin role.
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login

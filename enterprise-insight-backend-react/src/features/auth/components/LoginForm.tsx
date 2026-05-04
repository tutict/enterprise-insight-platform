import type { FormEvent } from 'react'

type LoginFormProps = {
  form: {
    username: string
    password: string
    error: string
    isLoading: boolean
  }
  setForm: {
    setUsername: (value: string) => void
    setPassword: (value: string) => void
  }
  submit: (event: FormEvent<HTMLFormElement>) => void
}

export default function LoginForm({ form, setForm, submit }: LoginFormProps) {
  return (
    <div className="grid min-h-screen place-items-center bg-console-950 px-5 text-slate-100">
      <section className="panel w-full max-w-md p-6">
        <div className="mb-6">
          <p className="text-xs uppercase tracking-wide text-teal-300">AI Orchestrator</p>
          <h1 className="mt-2 text-2xl font-semibold">Sign in</h1>
          <p className="muted mt-2">Access the compiler, orchestrator, and run history.</p>
        </div>

        <form className="space-y-4" onSubmit={submit}>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">Username</span>
            <input
              className="field w-full"
              type="text"
              placeholder="admin"
              value={form.username}
              onChange={(event) => setForm.setUsername(event.target.value)}
              required
            />
          </label>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">Password</span>
            <input
              className="field w-full"
              type="password"
              placeholder="******"
              value={form.password}
              onChange={(event) => setForm.setPassword(event.target.value)}
              required
            />
          </label>
          {form.error ? <p className="text-sm text-red-300">{form.error}</p> : null}
          <button className="btn-primary w-full" type="submit" disabled={form.isLoading}>
            {form.isLoading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="mt-5 text-xs text-slate-500">Demo backend accepts any password. Use admin for admin role.</p>
      </section>
    </div>
  )
}

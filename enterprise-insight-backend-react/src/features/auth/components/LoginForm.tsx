import type { FormEvent } from 'react'
import { useTranslation } from 'react-i18next'

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
  const { t } = useTranslation('common')

  return (
    <div className="grid min-h-screen place-items-center bg-console-950 px-5 text-slate-100">
      <section className="panel w-full max-w-md p-6">
        <div className="mb-6">
          <p className="text-xs uppercase tracking-wide text-teal-300">{t('app.brand')}</p>
          <h1 className="mt-2 text-2xl font-semibold">{t('auth.signIn')}</h1>
          <p className="muted mt-2">{t('auth.description')}</p>
        </div>

        <form className="space-y-4" onSubmit={submit}>
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">{t('auth.username')}</span>
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
            <span className="text-xs font-medium text-slate-400">{t('auth.password')}</span>
            <input
              className="field w-full"
              type="password"
              placeholder="******"
              value={form.password}
              onChange={(event) => setForm.setPassword(event.target.value)}
              required
            />
          </label>
          {form.error ? (
            <div className="rounded-lg border border-red-400/30 bg-red-950/60 p-3 text-sm text-red-100">
              <p className="font-medium">{t('auth.failed')}</p>
              <p className="mt-1 text-red-100/80">{form.error}</p>
            </div>
          ) : null}
          <button className="btn-primary w-full" type="submit" disabled={form.isLoading}>
            {form.isLoading ? t('auth.signingIn') : t('auth.signIn')}
          </button>
        </form>

        <p className="mt-5 text-xs text-slate-500">{t('auth.demoHint')}</p>
      </section>
    </div>
  )
}

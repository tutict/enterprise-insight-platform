import { useTranslation } from 'react-i18next'
import { useAuth } from '../../features/auth/context/useAuth'
import useApiStatus from '../hooks/useApiStatus'

function TopBar() {
  const { t } = useTranslation('common')
  const { auth, logout } = useAuth()
  const apiStatus = useApiStatus()
  const apiStateLabel = apiStatus.state === 'ok' ? t('api.online') : t(`api.state.${apiStatus.state}`)

  return (
    <header className="flex min-h-16 flex-col gap-3 border-b border-slate-800 bg-console-900/80 px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:px-6 lg:px-8">
      <div className="min-w-0">
        <p className="text-xs uppercase text-slate-500">{t('app.eyebrow')}</p>
        <h1 className="text-base font-semibold text-slate-100">{t('app.heading')}</h1>
      </div>
      <div className="flex flex-wrap items-center gap-3">
        <div
          className="inline-flex min-h-10 items-center gap-2 rounded-md border border-slate-700/90 bg-console-950 px-3 py-2 text-xs text-slate-400"
          aria-label={`${t('api.label')} ${apiStateLabel}`}
        >
          <span
            className={`h-2.5 w-2.5 rounded-full ${
              apiStatus.state === 'ok'
                ? 'bg-emerald-400'
                : apiStatus.state === 'checking'
                  ? 'bg-amber-400'
                  : 'bg-red-400'
            }`}
          />
          {t('api.label')} {apiStateLabel}
          {apiStatus.latencyMs ? ` ${t('api.latency', { latency: apiStatus.latencyMs })}` : ''}
        </div>
        {auth ? (
          <div className="flex min-w-0 items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium text-slate-100">{auth.username}</p>
              <p className="text-xs text-slate-500">
                {auth.roles.join(', ')} {t('user.tenantSeparator')} {auth.tenant ?? 'demo-tenant'}
              </p>
            </div>
            <button className="btn-secondary min-h-10 px-3 py-1.5" type="button" onClick={logout}>
              {t('auth.signOut')}
            </button>
          </div>
        ) : null}
      </div>
    </header>
  )
}

export default TopBar

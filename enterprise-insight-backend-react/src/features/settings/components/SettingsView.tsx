import { useTranslation } from 'react-i18next'
import { SUPPORTED_LANGUAGES, type SupportedLanguage } from '../../../i18n'

type SettingsViewProps = {
  language: SupportedLanguage
  setLanguage: (lang: SupportedLanguage) => void
  runtimeValues: Array<{
    labelKey: string
    value: string
    valueKey?: string
  }>
  executionContract: string[]
}

export default function SettingsView({
  language,
  setLanguage,
  runtimeValues,
  executionContract,
}: SettingsViewProps) {
  const { t } = useTranslation('common')

  return (
    <div className="grid gap-5 lg:grid-cols-2">
      <section className="panel p-5">
        <h2 className="text-lg font-semibold text-slate-100">{t('settings.title')}</h2>
        <p className="muted mt-1">{t('settings.description')}</p>

        <div className="mt-5 space-y-4">
          <label className="block space-y-2">
            <span className="text-xs font-medium text-slate-400">{t('settings.language')}</span>
            <select
              className="field w-full"
              value={language}
              onChange={(event) => setLanguage(event.target.value as SupportedLanguage)}
            >
              {SUPPORTED_LANGUAGES.map((lang) => (
                <option key={lang} value={lang}>
                  {t(`settings.languageOption.${lang}`)}
                </option>
              ))}
            </select>
          </label>
          {runtimeValues.map((item) => (
            <label key={item.labelKey} className="block space-y-2">
              <span className="text-xs font-medium text-slate-400">{t(item.labelKey)}</span>
              <input className="field w-full" readOnly value={item.value || (item.valueKey ? t(item.valueKey) : '')} />
            </label>
          ))}
        </div>
      </section>

      <section className="panel p-5">
        <h3 className="text-sm font-semibold text-slate-100">{t('settings.executionContract')}</h3>
        <div className="mt-4 space-y-3 text-sm text-slate-300">
          {executionContract.map((line) => (
            <p key={line}>{t(line)}</p>
          ))}
        </div>
      </section>
    </div>
  )
}

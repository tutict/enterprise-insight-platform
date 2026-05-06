import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'

import enCommon from './locales/en/common.json'
import enDsl from './locales/en/dsl.json'
import enRun from './locales/en/run.json'
import zhCnCommon from './locales/zh-CN/common.json'
import zhCnDsl from './locales/zh-CN/dsl.json'
import zhCnRun from './locales/zh-CN/run.json'

export const SUPPORTED_LANGUAGES = ['en', 'zh-CN'] as const
export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number]

export const DEFAULT_LANGUAGE: SupportedLanguage = 'en'
export const LANGUAGE_STORAGE_KEY = 'eip.language'

export const isSupportedLanguage = (value: string | null | undefined): value is SupportedLanguage =>
  Boolean(value && (SUPPORTED_LANGUAGES as readonly string[]).includes(value))

export const getStoredLanguage = (): SupportedLanguage => {
  if (typeof window === 'undefined') {
    return DEFAULT_LANGUAGE
  }

  const stored = window.localStorage.getItem(LANGUAGE_STORAGE_KEY)
  return isSupportedLanguage(stored) ? stored : DEFAULT_LANGUAGE
}

i18n.use(initReactI18next).init({
  resources: {
    en: {
      common: enCommon,
      run: enRun,
      dsl: enDsl,
    },
    'zh-CN': {
      common: zhCnCommon,
      run: zhCnRun,
      dsl: zhCnDsl,
    },
  },
  lng: getStoredLanguage(),
  fallbackLng: DEFAULT_LANGUAGE,
  supportedLngs: SUPPORTED_LANGUAGES,
  defaultNS: 'common',
  ns: ['common', 'run', 'dsl'],
  interpolation: {
    escapeValue: false,
  },
  returnNull: false,
})

export default i18n

import { create } from 'zustand'
import i18n, { getStoredLanguage, LANGUAGE_STORAGE_KEY, type SupportedLanguage } from '../i18n'

export type NotificationType = 'error' | 'info' | 'success'

export type Notification = {
  id: string
  type: NotificationType
  message: string
}

type NotificationState = {
  notifications: Notification[]
  push: (notification: Notification) => void
  remove: (id: string) => void
}

export type UIState = NotificationState & {
  language: SupportedLanguage
  setLanguage: (lang: SupportedLanguage) => void
}

export const useNotificationStore = create<UIState>((set) => ({
  language: getStoredLanguage(),
  notifications: [],
  setLanguage: (language) => {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, language)
    void i18n.changeLanguage(language)
    set({ language })
  },
  push: (notification) =>
    set((state) => ({
      notifications: [...state.notifications, notification],
    })),
  remove: (id) =>
    set((state) => ({
      notifications: state.notifications.filter((item) => item.id !== id),
    })),
}))

import { create } from 'zustand'

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

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  push: (notification) =>
    set((state) => ({
      notifications: [...state.notifications, notification],
    })),
  remove: (id) =>
    set((state) => ({
      notifications: state.notifications.filter((item) => item.id !== id),
    })),
}))

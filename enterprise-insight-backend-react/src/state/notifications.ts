export type NotificationType = 'error' | 'info' | 'success'

export type NotificationAction = {
  label: string
  onClick: () => void
}

export type Notification = {
  id: string
  type: NotificationType
  message: string
  action?: NotificationAction
}

type Listener = (notification: Notification) => void

const listeners = new Set<Listener>()

export const emitNotification = (notification: Notification) => {
  listeners.forEach((listener) => listener(notification))
}

export const subscribeNotifications = (listener: Listener) => {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

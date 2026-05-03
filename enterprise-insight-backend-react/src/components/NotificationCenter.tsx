import { useEffect } from 'react'
import { useNotificationStore } from '../store/notifications'

const AUTO_DISMISS_MS = 4500

function NotificationCenter() {
  const notifications = useNotificationStore((state) => state.notifications)
  const removeNotification = useNotificationStore((state) => state.remove)

  useEffect(() => {
    if (!notifications.length) {
      return
    }
    const timers = notifications.map((notification) =>
      window.setTimeout(() => removeNotification(notification.id), AUTO_DISMISS_MS),
    )
    return () => timers.forEach(window.clearTimeout)
  }, [notifications, removeNotification])

  if (!notifications.length) {
    return null
  }

  return (
    <div className="fixed right-5 top-5 z-50 flex w-96 max-w-[calc(100vw-2rem)] flex-col gap-3">
      {notifications.map((note) => (
        <div
          key={note.id}
          className={`rounded-lg border px-4 py-3 text-sm shadow-panel ${
            note.type === 'error'
              ? 'border-red-400/30 bg-red-950/80 text-red-100'
              : note.type === 'success'
                ? 'border-emerald-400/30 bg-emerald-950/80 text-emerald-100'
                : 'border-sky-400/30 bg-sky-950/80 text-sky-100'
          }`}
        >
          <p>{note.message}</p>
        </div>
      ))}
    </div>
  )
}

export default NotificationCenter

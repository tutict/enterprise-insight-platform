import { useEffect, useState } from 'react'
import {
  type Notification,
  subscribeNotifications,
} from '../state/notifications'

const AUTO_DISMISS_MS = 4500

function NotificationCenter() {
  const [notifications, setNotifications] = useState<Notification[]>([])

  const removeNotification = (id: string) => {
    setNotifications((prev) => prev.filter((item) => item.id !== id))
  }

  useEffect(() => {
    const unsubscribe = subscribeNotifications((notification) => {
      setNotifications((prev) => [...prev, notification])
      window.setTimeout(() => {
        removeNotification(notification.id)
      }, AUTO_DISMISS_MS)
    })

    return () => {
      unsubscribe()
    }
  }, [])

  if (!notifications.length) {
    return null
  }

  return (
    <div className="toast-stack">
      {notifications.map((note) => (
        <div key={note.id} className={`toast toast--${note.type}`}>
          <p>{note.message}</p>
          {note.action ? (
            <button
              className="toast-action"
              type="button"
              onClick={() => {
                note.action?.onClick()
                removeNotification(note.id)
              }}
            >
              {note.action.label}
            </button>
          ) : null}
        </div>
      ))}
    </div>
  )
}

export default NotificationCenter

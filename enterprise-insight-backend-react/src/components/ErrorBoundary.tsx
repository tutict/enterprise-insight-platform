import type { ReactNode } from 'react'
import { Component } from 'react'

type ErrorBoundaryProps = {
  children: ReactNode
}

type ErrorBoundaryState = {
  hasError: boolean
  message: string
}

class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = {
    hasError: false,
    message: '',
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, message: error.message || 'Unexpected error' }
  }

  componentDidCatch(error: Error, info: { componentStack: string }) {
    const payload = {
      message: error.message || 'Unexpected error',
      stack: error.stack ?? '',
      componentStack: info.componentStack ?? '',
      url: window.location.href,
      userAgent: navigator.userAgent,
      occurredAt: new Date().toISOString(),
    }

    fetch('/api/harness/logs/ui-error', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    }).catch(() => undefined)
  }

  handleReload = () => {
    window.location.reload()
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <div className="error-card">
            <p className="eyebrow">Something went wrong</p>
            <h1>We hit an unexpected error.</h1>
            <p className="muted">{this.state.message}</p>
            <button
              className="primary-btn"
              type="button"
              onClick={this.handleReload}
            >
              Reload page
            </button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}

export default ErrorBoundary

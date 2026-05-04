import type { ReactNode } from 'react'
import { Component } from 'react'
import { logUiError } from '../../api/modules/telemetry.api'

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

    logUiError(payload).catch(() => undefined)
  }

  handleReload = () => {
    window.location.reload()
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="grid min-h-screen place-items-center bg-console-950 px-5 text-slate-100">
          <div className="panel w-full max-w-lg p-6">
            <p className="text-xs uppercase tracking-wide text-red-300">Something went wrong</p>
            <h1 className="mt-2 text-2xl font-semibold">Unexpected error</h1>
            <p className="muted mt-2">{this.state.message}</p>
            <button className="btn-primary mt-5" type="button" onClick={this.handleReload}>
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

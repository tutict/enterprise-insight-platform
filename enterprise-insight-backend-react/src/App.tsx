import { Suspense, lazy } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import AppLayout from './shared/components/AppLayout'
import ErrorBoundary from './shared/components/ErrorBoundary'
import NotificationCenter from './shared/components/NotificationCenter'
import RequireAuth from './shared/components/RequireAuth'

const DslEditor = lazy(() => import('./pages/DslEditor'))
const GraphRuntime = lazy(() => import('./pages/GraphRuntime'))
const Login = lazy(() => import('./pages/Login'))
const RunPage = lazy(() => import('./pages/RunPage'))
const Runs = lazy(() => import('./pages/Runs'))
const Settings = lazy(() => import('./pages/Settings'))

function App() {
  return (
    <ErrorBoundary>
      <NotificationCenter />
      <Suspense fallback={<div className="grid min-h-screen place-items-center bg-console-950 text-slate-400">Loading...</div>}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<RequireAuth />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/run" replace />} />
              <Route path="/dsl" element={<DslEditor />} />
              <Route path="/graph" element={<GraphRuntime />} />
              <Route path="/run" element={<RunPage />} />
              <Route path="/runs" element={<Runs />} />
              <Route path="/settings" element={<Settings />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
    </ErrorBoundary>
  )
}

export default App

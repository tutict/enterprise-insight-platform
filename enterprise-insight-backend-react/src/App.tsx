import { Suspense, lazy } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import './App.css'
import AppLayout from './components/AppLayout'
import ErrorBoundary from './components/ErrorBoundary'
import NotificationCenter from './components/NotificationCenter'
import RequireAuth from './components/RequireAuth'

const HarnessCompiler = lazy(() => import('./pages/HarnessCompiler'))
const Insights = lazy(() => import('./pages/Insights'))
const Login = lazy(() => import('./pages/Login'))
const Runs = lazy(() => import('./pages/Runs'))
const Settings = lazy(() => import('./pages/Settings'))
const Systems = lazy(() => import('./pages/Systems'))

function App() {
  return (
    <ErrorBoundary>
      <NotificationCenter />
      <Suspense fallback={<div className="page-loading">Loading...</div>}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route element={<RequireAuth />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/harness" replace />} />
              <Route path="/harness" element={<HarnessCompiler />} />
              <Route path="/runs" element={<Runs />} />
              <Route path="/templates" element={<Insights />} />
              <Route path="/agents" element={<Systems />} />
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

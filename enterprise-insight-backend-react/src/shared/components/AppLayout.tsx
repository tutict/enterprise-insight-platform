import { Outlet } from 'react-router-dom'
import SideNav from './SideNav'
import TopBar from './TopBar'

function AppLayout() {
  return (
    <div className="flex min-h-screen bg-console-950 text-slate-100">
      <SideNav />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopBar />
        <main className="min-w-0 flex-1 overflow-auto px-6 py-5">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AppLayout

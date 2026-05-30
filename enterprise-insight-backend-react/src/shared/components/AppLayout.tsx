import { Outlet } from 'react-router-dom'
import SideNav from './SideNav'
import TopBar from './TopBar'

function AppLayout() {
  return (
    <div className="min-h-screen bg-console-950 text-slate-100 lg:flex">
      <SideNav />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopBar />
        <main className="min-w-0 flex-1 overflow-auto px-4 py-4 sm:px-6 lg:px-8 lg:py-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AppLayout

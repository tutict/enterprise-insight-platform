import { Outlet } from 'react-router-dom'
import SideNav from './SideNav'
import TopBar from './TopBar'

function AppLayout() {
  return (
    <div className="app-shell">
      <SideNav />
      <div className="app-main">
        <TopBar />
        <div className="app-content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}

export default AppLayout

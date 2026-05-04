import { Navigate } from 'react-router-dom'
import { useLoginPage } from '../../../hooks/useLoginPage'
import LoginForm from '../components/LoginForm'

export default function LoginContainer() {
  const loginPage = useLoginPage()

  if (loginPage.isAuthenticated) {
    return <Navigate to={loginPage.redirectPath} replace />
  }

  return <LoginForm {...loginPage} />
}

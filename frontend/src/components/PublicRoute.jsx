import { Navigate, Outlet, useLocation } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

export default function PublicRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (isAuthenticated) {
    return <Navigate to={location.state?.from?.pathname || '/catalogo'} replace />
  }

  return <Outlet />
}

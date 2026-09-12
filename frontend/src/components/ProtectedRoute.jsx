import { Navigate, Outlet, useLocation } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    // Guarda a rota pedida para voltar a ela depois do login.
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}

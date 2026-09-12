import { Navigate, Outlet, useLocation } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

/**
 * Rotas de visitante (login, cadastro). Quem já tem sessão válida não volta
 * para o formulário de login — só cai aqui se não estiver logado ou se o
 * token tiver expirado.
 */
export default function PublicRoute() {
  const { isAuthenticated } = useAuth()
  const location = useLocation()

  if (isAuthenticated) {
    return <Navigate to={location.state?.from?.pathname || '/catalogo'} replace />
  }

  return <Outlet />
}

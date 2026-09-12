import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import CadastroPage from './pages/CadastroPage'
import CatalogoPage from './pages/CatalogoPage'
import LeilaoDetalhePage from './pages/LeilaoDetalhePage'
import PregaoPage from './pages/PregaoPage'
import RecusasPage from './pages/RecusasPage'
import AdminPage from './pages/AdminPage'
import AdminGruposPage from './pages/AdminGruposPage'
import AdminUsuariosPage from './pages/AdminUsuariosPage'
import NovoLeilaoPage from './pages/NovoLeilaoPage'
import ProtectedRoute from './components/ProtectedRoute'
import PublicRoute from './components/PublicRoute'
import useAuth from './hooks/useAuth'

function HomeRedirect() {
  const { isAuthenticated } = useAuth()
  return <Navigate to={isAuthenticated ? '/catalogo' : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />

      <Route element={<PublicRoute />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<CadastroPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route path="/catalogo" element={<CatalogoPage />} />
        <Route path="/leiloes/:uuid" element={<LeilaoDetalhePage />} />
        <Route path="/leiloes/:uuid/pregao" element={<PregaoPage />} />
        <Route path="/leiloes/:uuid/recusas" element={<RecusasPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/admin/grupos" element={<AdminGruposPage />} />
        <Route path="/admin/usuarios" element={<AdminUsuariosPage />} />
        <Route path="/admin/leiloes/novo" element={<NovoLeilaoPage />} />
      </Route>

      <Route path="*" element={<HomeRedirect />} />
    </Routes>
  )
}

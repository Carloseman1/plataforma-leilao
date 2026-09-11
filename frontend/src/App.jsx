/**
 * App = mapa de rotas.
 */

import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import CadastroPage from './pages/CadastroPage'
import CatalogoPage from './pages/CatalogoPage'
import LeilaoDetalhePage from './pages/LeilaoDetalhePage'
import AdminPage from './pages/AdminPage'
import AdminGruposPage from './pages/AdminGruposPage'
import AdminUsuariosPage from './pages/AdminUsuariosPage'
import NovoLeilaoPage from './pages/NovoLeilaoPage'
import ProtectedRoute from './components/ProtectedRoute'
import { getSessao } from './utils/session'

function HomeRedirect() {
  const sessao = getSessao()
  return <Navigate to={sessao?.token ? '/catalogo' : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cadastro" element={<CadastroPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/catalogo" element={<CatalogoPage />} />
        <Route path="/leiloes/:id" element={<LeilaoDetalhePage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="/admin/grupos" element={<AdminGruposPage />} />
        <Route path="/admin/usuarios" element={<AdminUsuariosPage />} />
        <Route path="/admin/leiloes/novo" element={<NovoLeilaoPage />} />
      </Route>
    </Routes>
  )
}

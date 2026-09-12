import { Link, useNavigate } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import { PERMISSOES, podeAdministrar, temPermissao } from '../../utils/permissions'

export default function SiteHeader() {
  const navigate = useNavigate()
  const { usuario, logout } = useAuth()

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <header className="site-header">
      <Link to="/catalogo" className="site-brand">
        <span className="site-brand-mark">Haras Real</span>
        <span className="site-brand-sub">Leilões</span>
      </Link>

      <nav className="site-nav">
        {temPermissao(usuario, PERMISSOES.CRIAR_LEILAO) && (
          <Link to="/admin/leiloes/novo">Novo leilão</Link>
        )}
        {podeAdministrar(usuario) && (
          <Link to="/admin">Administração</Link>
        )}
      </nav>

      <div className="site-header-actions">
        {usuario?.email && (
          <span className="site-user">
            {usuario.email}
            {usuario.grupo ? ` · ${usuario.grupo}` : ''}
          </span>
        )}
        <button type="button" className="site-logout" onClick={handleLogout}>
          Sair
        </button>
      </div>
    </header>
  )
}

import { Link } from 'react-router-dom'
import SiteHeader from '../components/catalog/SiteHeader'
import useAuth from '../hooks/useAuth'
import { PERMISSOES, temPermissao } from '../utils/permissions'
import '../styles/catalog.css'
import '../styles/admin.css'

export default function AdminPage() {
  const { usuario } = useAuth()

  return (
    <div className="catalog-page">
      <SiteHeader />
      <main className="catalog-main">
        <header className="catalog-intro">
          <h1>Administração</h1>
          <p>Gerencie grupos de permissão e usuários da plataforma.</p>
        </header>

        <div className="admin-links">
          {temPermissao(usuario, PERMISSOES.GERENCIAR_GRUPOS) && (
            <Link className="admin-card-link" to="/admin/grupos">
              <strong>Grupos de permissão</strong>
              <span>Criar e editar o que cada grupo pode fazer</span>
            </Link>
          )}
          {temPermissao(usuario, PERMISSOES.GERENCIAR_USUARIOS) && (
            <Link className="admin-card-link" to="/admin/usuarios">
              <strong>Usuários</strong>
              <span>Atribuir um grupo a cada usuário</span>
            </Link>
          )}
          {temPermissao(usuario, PERMISSOES.CRIAR_LEILAO) && (
            <Link className="admin-card-link" to="/admin/leiloes/novo">
              <strong>Novo leilão</strong>
              <span>Publicar um leilão no catálogo</span>
            </Link>
          )}
        </div>
      </main>
    </div>
  )
}

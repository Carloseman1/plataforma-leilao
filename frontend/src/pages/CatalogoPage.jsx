/**
 * Catálogo de leilões — tela inicial após login.
 */

import SiteHeader from '../components/catalog/SiteHeader'
import LeilaoListItem from '../components/catalog/LeilaoListItem'
import useLeiloes from '../hooks/useLeiloes'
import '../styles/catalog.css'

export default function CatalogoPage() {
  const { leiloes, loading, erro } = useLeiloes()

  return (
    <div className="catalog-page">
      <SiteHeader />

      <main className="catalog-main">
        <header className="catalog-intro">
          <h1>Leilões</h1>
          <p>Escolha um leilão para ver os lotes disponíveis.</p>
        </header>

        {loading && <p className="catalog-state">Carregando catálogo…</p>}
        {erro && <p className="catalog-state catalog-state--erro">{erro}</p>}

        {!loading && !erro && leiloes.length === 0 && (
          <p className="catalog-state">Nenhum leilão publicado no momento.</p>
        )}

        <div className="leilao-list">
          {leiloes.map((leilao) => (
            <LeilaoListItem key={leilao.id} leilao={leilao} />
          ))}
        </div>
      </main>
    </div>
  )
}

/**
 * Detalhe do leilão — lista de lotes (sem lance nesta fase).
 */

import { Link, useParams } from 'react-router-dom'
import SiteHeader from '../components/catalog/SiteHeader'
import LoteRow from '../components/catalog/LoteRow'
import useLeilaoDetalhe from '../hooks/useLeilaoDetalhe'
import { formatarDataHora, rotuloStatus } from '../utils/format'
import '../styles/catalog.css'

export default function LeilaoDetalhePage() {
  const { id } = useParams()
  const { leilao, loading, erro } = useLeilaoDetalhe(id)

  return (
    <div className="catalog-page">
      <SiteHeader />

      <main className="catalog-main">
        <p className="back-link">
          <Link to="/catalogo">← Voltar ao catálogo</Link>
        </p>

        {loading && <p className="catalog-state">Carregando leilão…</p>}
        {erro && <p className="catalog-state catalog-state--erro">{erro}</p>}

        {leilao && (
          <>
            <header className="detalhe-intro">
              <p className="detalhe-status">
                <span className={`status status--${leilao.status?.toLowerCase()}`}>
                  {rotuloStatus(leilao.status)}
                </span>
              </p>
              <h1>{leilao.titulo}</h1>
              {leilao.subtitulo && <p className="detalhe-sub">{leilao.subtitulo}</p>}
              <p className="detalhe-info">
                {formatarDataHora(leilao.dataInicio)}
                {leilao.local ? ` · ${leilao.local}` : ''}
              </p>
            </header>

            <section className="lotes-section">
              <h2>Lotes</h2>
              {(!leilao.lotes || leilao.lotes.length === 0) ? (
                <p className="catalog-state">Este leilão ainda não tem lotes.</p>
              ) : (
                <div className="lotes-table-wrap">
                  <table className="lotes-table">
                    <thead>
                      <tr>
                        <th>Nº</th>
                        <th>Animal</th>
                        <th>Raça</th>
                        <th>Sexo</th>
                        <th>Idade</th>
                        <th>Lance inicial</th>
                      </tr>
                    </thead>
                    <tbody>
                      {leilao.lotes.map((lote) => (
                        <LoteRow key={lote.id} lote={lote} />
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  )
}

import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import SiteHeader from '../components/catalog/SiteHeader'
import LoteCard from '../components/catalog/LoteCard'
import AdicionarLoteCard from '../components/catalog/AdicionarLoteCard'
import FormularioNovoLote from '../components/catalog/FormularioNovoLote'
import useAuth from '../hooks/useAuth'
import useLeilaoDetalhe from '../hooks/useLeilaoDetalhe'
import { formatarDataHora, rotuloStatus } from '../utils/format'
import { PERMISSOES, temPermissao } from '../utils/permissions'
import '../styles/catalog.css'
import '../styles/admin.css'

export default function LeilaoDetalhePage() {
  const { uuid } = useParams()
  const { usuario } = useAuth()
  const { leilao, loading, erro, recarregar } = useLeilaoDetalhe(uuid)
  const [formAberto, setFormAberto] = useState(false)

  const lotes = leilao?.lotes || []
  const podeAdicionarLote = temPermissao(usuario, PERMISSOES.CRIAR_LEILAO)

  function aoCriarLote() {
    setFormAberto(false)
    recarregar()
  }

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
              <p className="detalhe-acoes">
                <Link className="leilao-item-link" to={`/leiloes/${uuid}/pregao`}>
                  Entrar no pregão ao vivo
                </Link>
              </p>
            </header>

            <section className="lotes-section">
              <h2>Lotes</h2>

              {lotes.length === 0 && !podeAdicionarLote && (
                <p className="catalog-state">Este leilão ainda não tem lotes.</p>
              )}

              {formAberto && (
                <FormularioNovoLote
                  uuidLeilao={uuid}
                  onCancelar={() => setFormAberto(false)}
                  onCriado={aoCriarLote}
                />
              )}

              <div className="lotes-grid">
                {lotes.map((lote) => (
                  <LoteCard key={lote.uuid} lote={lote} />
                ))}

                {podeAdicionarLote && !formAberto && (
                  <AdicionarLoteCard onClick={() => setFormAberto(true)} />
                )}
              </div>
            </section>
          </>
        )}
      </main>
    </div>
  )
}

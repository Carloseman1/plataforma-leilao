import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import SiteHeader from '../components/catalog/SiteHeader'
import FeedbackMessage from '../components/FeedbackMessage'
import Button from '../components/Button'
import LoteEmPregao from '../components/pregao/LoteEmPregao'
import FeedDeLances from '../components/pregao/FeedDeLances'
import ProvocarCasos from '../components/pregao/ProvocarCasos'
import useAuth from '../hooks/useAuth'
import usePregao from '../hooks/usePregao'
import useDarLance from '../hooks/useDarLance'
import { avancarPregao, encerrarPregao, iniciarPregao, reabrirPregao } from '../api/pregaoService'
import { formatarMoeda, rotuloStatus } from '../utils/format'
import { PERMISSOES, temPermissao } from '../utils/permissions'
import '../styles/catalog.css'
import '../styles/admin.css'
import '../styles/pregao.css'

const DESFECHO = {
  VENDIDO: 'Vendido',
  ENCERRADO: 'Sem venda',
  RESERVA_NAO_ATINGIDA: 'Reserva não atingida',
}

export default function PregaoPage() {
  const { uuid } = useParams()
  const { usuario } = useAuth()
  const { pregao, lances, erro, carregando, conectado, recarregar } = usePregao(uuid)
  const { enviar, reenviarUltimo, temUltimoLance, enviando, erro: erroLance } = useDarLance()
  const [erroComando, setErroComando] = useState('')

  const ehLeiloeiro = temPermissao(usuario, PERMISSOES.CRIAR_LEILAO)
  const loteAtual = pregao?.loteAtual

  async function comandar(acao) {
    setErroComando('')
    try {
      await acao(uuid)
      await recarregar()
    } catch (err) {
      setErroComando(err.message)
    }
  }

  return (
    <div className="catalog-page">
      <SiteHeader />

      <main className="catalog-main">
        <p className="back-link">
          <Link to={`/leiloes/${uuid}`}>← Voltar ao leilão</Link>
        </p>

        {carregando && <p className="catalog-state">Carregando pregão…</p>}
        {erro && <p className="catalog-state catalog-state--erro">{erro}</p>}

        {pregao && (
          <>
            <header className="pregao-topo">
              <div>
                <p className="detalhe-status">
                  <span className={`status status--${pregao.status?.toLowerCase()}`}>
                    {rotuloStatus(pregao.status)}
                  </span>
                  <span className={`sinal ${conectado ? 'sinal--vivo' : 'sinal--mudo'}`}>
                    {conectado ? 'Ao vivo' : 'Reconectando…'}
                  </span>
                </p>
                <h1>{pregao.titulo}</h1>
              </div>

              {ehLeiloeiro && (
                <div className="pregao-comandos">
                  {pregao.status !== 'EM_ANDAMENTO' && pregao.status !== 'ENCERRADO' && (
                    <Button onClick={() => comandar(iniciarPregao)}>Abrir pregão</Button>
                  )}
                  {pregao.status === 'ENCERRADO' && (
                    <Button onClick={() => comandar(reabrirPregao)}>Reabrir último lote</Button>
                  )}
                  {pregao.status === 'EM_ANDAMENTO' && (
                    <>
                      <Button onClick={() => comandar(avancarPregao)}>Bater o martelo</Button>
                      <button
                        type="button"
                        className="link-btn"
                        onClick={() => comandar(encerrarPregao)}
                      >
                        Encerrar pregão
                      </button>
                    </>
                  )}
                </div>
              )}
            </header>

            <FeedbackMessage type="error">{erroComando || erroLance}</FeedbackMessage>

            <div className="pregao-palco">
              <section>
                {loteAtual ? (
                  <LoteEmPregao lote={loteAtual}>
                    <Button
                      disabled={enviando}
                      onClick={() => enviar(loteAtual.uuid, Number(loteAtual.proximoLance))}
                    >
                      Dar {formatarMoeda(loteAtual.proximoLance)}
                    </Button>
                  </LoteEmPregao>
                ) : (
                  <p className="catalog-state">
                    {pregao.status === 'ENCERRADO'
                      ? 'Pregão encerrado.'
                      : 'Nenhum lote aberto. O leiloeiro ainda não começou.'}
                  </p>
                )}

                {loteAtual && ehLeiloeiro && (
                  <ProvocarCasos
                    lote={loteAtual}
                    aoEnviar={enviar}
                    aoReenviar={reenviarUltimo}
                    temUltimoLance={temUltimoLance()}
                  />
                )}
              </section>

              <aside className="pregao-lateral">
                <section>
                  <h3 className="admin-subtitle">Lances</h3>
                  <FeedDeLances lances={lances} />
                  {ehLeiloeiro && (
                    <p className="pregao-dica">
                      <Link to={`/leiloes/${uuid}/recusas`}>Ver todas as recusas →</Link>
                    </p>
                  )}
                </section>

                <section>
                  <h3 className="admin-subtitle">Na fila ({pregao.naFila.length})</h3>
                  {pregao.naFila.length === 0 ? (
                    <p className="catalog-state">Nenhum lote esperando.</p>
                  ) : (
                    <ol className="pregao-fila">
                      {pregao.naFila.map((lote) => (
                        <li key={lote.uuid}>
                          <span>{String(lote.numero).padStart(2, '0')}</span>
                          {lote.nomeAnimal}
                        </li>
                      ))}
                    </ol>
                  )}
                </section>

                <section>
                  <h3 className="admin-subtitle">Encerrados ({pregao.encerrados.length})</h3>
                  {pregao.encerrados.length === 0 ? (
                    <p className="catalog-state">Nenhum lote fechado ainda.</p>
                  ) : (
                    <ul className="pregao-encerrados">
                      {pregao.encerrados.map((lote) => (
                        <li key={lote.uuid}>
                          <span className="pregao-encerrado-nome">
                            {String(lote.numero).padStart(2, '0')} {lote.nomeAnimal}
                          </span>
                          <span className="pregao-encerrado-valor">
                            {lote.lanceAtual ? formatarMoeda(lote.lanceAtual) : '—'}
                          </span>
                          <span className={`desfecho desfecho--${lote.status.toLowerCase()}`}>
                            {DESFECHO[lote.status] || lote.status}
                            {lote.lanceDaCasa && ' · fechou no lance da casa'}
                          </span>
                        </li>
                      ))}
                    </ul>
                  )}
                </section>
              </aside>
            </div>
          </>
        )}
      </main>
    </div>
  )
}

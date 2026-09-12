import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import SiteHeader from '../components/catalog/SiteHeader'
import { listarFalhas, listarRecusas } from '../api/pregaoService'
import { formatarDataHora, formatarMoeda } from '../utils/format'
import '../styles/catalog.css'
import '../styles/admin.css'
import '../styles/pregao.css'

export default function RecusasPage() {
  const { uuid } = useParams()
  const [recusas, setRecusas] = useState([])
  const [falhas, setFalhas] = useState([])
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)

  useEffect(() => {
    let ativo = true

    async function carregar() {
      try {
        const [listaRecusas, listaFalhas] = await Promise.all([
          listarRecusas(uuid),
          listarFalhas(),
        ])
        if (!ativo) return
        setRecusas(listaRecusas)
        setFalhas(listaFalhas)
      } catch (err) {
        if (ativo) setErro(err.message)
      } finally {
        if (ativo) setCarregando(false)
      }
    }

    carregar()
    return () => {
      ativo = false
    }
  }, [uuid])

  const porMotivo = agruparPorMotivo(recusas)

  return (
    <div className="catalog-page">
      <SiteHeader />

      <main className="catalog-main">
        <p className="back-link">
          <Link to={`/leiloes/${uuid}/pregao`}>← Voltar ao pregão</Link>
        </p>

        <header className="catalog-intro">
          <h1>Lances recusados</h1>
          <p>Toda tentativa que não virou lance, com o motivo de cada uma.</p>
        </header>

        {carregando && <p className="catalog-state">Carregando…</p>}
        {erro && <p className="catalog-state catalog-state--erro">{erro}</p>}

        {!carregando && recusas.length === 0 && (
          <p className="catalog-state">Nenhum lance recusado neste leilão.</p>
        )}

        {porMotivo.length > 0 && (
          <section className="admin-panel">
            <h2 className="admin-subtitle">Por motivo</h2>
            <ul className="resumo-motivos">
              {porMotivo.map(([motivo, lista]) => (
                <li key={motivo}>
                  <strong>{lista.length}</strong>
                  <span>{lista[0].rotulo}</span>
                  <small>{lista[0].explicacao}</small>
                </li>
              ))}
            </ul>
          </section>
        )}

        {recusas.length > 0 && (
          <section className="admin-panel">
            <h2 className="admin-subtitle">Histórico</h2>
            <div className="tabela-rolavel">
              <table className="tabela-recusas">
                <thead>
                  <tr>
                    <th>Quando</th>
                    <th>Lote</th>
                    <th>Quem</th>
                    <th>Valor</th>
                    <th>Motivo</th>
                  </tr>
                </thead>
                <tbody>
                  {recusas.map((recusa) => (
                    <tr key={recusa.lanceUuid}>
                      <td>{formatarDataHora(recusa.criadoEm)}</td>
                      <td>
                        {String(recusa.loteNumero).padStart(2, '0')} · {recusa.nomeAnimal}
                      </td>
                      <td>
                        {recusa.daCasa ? (
                          <span className="etiqueta-casa">Casa</span>
                        ) : (
                          recusa.comprador
                        )}
                      </td>
                      <td>{formatarMoeda(recusa.valor)}</td>
                      <td>
                        <span className="motivo">{recusa.rotulo}</span>
                        <small className="motivo-explicacao">{recusa.explicacao}</small>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        <section className="admin-panel">
          <h2 className="admin-subtitle">Falhas de infraestrutura</h2>
          <p className="pregao-dica">
            Mensagens que o consumidor não conseguiu processar nem depois das tentativas, e
            foram parar na DLT. Não são lances recusados — são lances que não chegaram a ser
            julgados.
          </p>
          {falhas.length === 0 ? (
            <p className="catalog-state">Nenhuma falha registrada.</p>
          ) : (
            <ul className="lista-falhas">
              {falhas.map((falha) => (
                <li key={`${falha.ocorridaEm}-${falha.mensagem}`}>
                  <span>{formatarDataHora(falha.ocorridaEm)}</span>
                  <code>{falha.mensagem}</code>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  )
}

function agruparPorMotivo(recusas) {
  const mapa = new Map()

  for (const recusa of recusas) {
    const lista = mapa.get(recusa.motivo) || []
    lista.push(recusa)
    mapa.set(recusa.motivo, lista)
  }

  return [...mapa.entries()].sort((a, b) => b[1].length - a[1].length)
}

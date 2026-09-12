import { formatarMoeda } from '../../utils/format'

/** O que está entrando na fila, aceito ou recusado, na ordem em que foi julgado. */
export default function FeedDeLances({ lances }) {
  if (lances.length === 0) {
    return <p className="catalog-state">Nenhum lance ainda.</p>
  }

  return (
    <ul className="feed-lances">
      {lances.map((lance) => (
        <li
          key={lance.lanceUuid}
          className={`feed-item ${lance.aceito ? 'feed-item--aceito' : 'feed-item--recusado'}`}
        >
          <span className="feed-valor">{formatarMoeda(lance.valorTentado)}</span>

          <span className="feed-quem">
            {lance.daCasa ? <span className="etiqueta-casa">Casa</span> : lance.comprador}
          </span>

          <span className="feed-desfecho">
            {lance.aceito ? 'Aceito' : lance.motivoRotulo}
          </span>
        </li>
      ))}
    </ul>
  )
}

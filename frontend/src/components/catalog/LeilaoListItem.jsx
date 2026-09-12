import { Link } from 'react-router-dom'
import { formatarDataHora, rotuloStatus } from '../../utils/format'

export default function LeilaoListItem({ leilao }) {
  return (
    <article className="leilao-item">
      <div className="leilao-item-main">
        <p className="leilao-item-meta">
          <span className={`status status--${leilao.status?.toLowerCase()}`}>
            {rotuloStatus(leilao.status)}
          </span>
          <span>{leilao.qtdLotes} {leilao.qtdLotes === 1 ? 'lote' : 'lotes'}</span>
        </p>
        <h2 className="leilao-item-title">{leilao.titulo}</h2>
        {leilao.subtitulo && (
          <p className="leilao-item-sub">{leilao.subtitulo}</p>
        )}
        <p className="leilao-item-info">
          {formatarDataHora(leilao.dataInicio)}
          {leilao.local ? ` · ${leilao.local}` : ''}
        </p>
      </div>

      <Link className="leilao-item-link" to={`/leiloes/${leilao.uuid}`}>
        Entrar no leilão
      </Link>
    </article>
  )
}

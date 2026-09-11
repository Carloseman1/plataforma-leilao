/**
 * Linha de lote na página de detalhe.
 */

import { formatarMoeda } from '../../utils/format'

export default function LoteRow({ lote }) {
  return (
    <tr>
      <td className="lote-num">{String(lote.numero).padStart(2, '0')}</td>
      <td>
        <strong>{lote.nomeAnimal}</strong>
        {lote.descricaoCurta && (
          <span className="lote-desc">{lote.descricaoCurta}</span>
        )}
      </td>
      <td>{lote.raca || '—'}</td>
      <td>{lote.sexo || '—'}</td>
      <td>{lote.idadeAnos != null ? `${lote.idadeAnos} anos` : '—'}</td>
      <td className="lote-valor">{formatarMoeda(lote.lanceInicial)}</td>
    </tr>
  )
}

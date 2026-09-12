import { useState } from 'react'
import { formatarMoeda } from '../../utils/format'
import { rotuloOferta } from '../../utils/lote'

export default function LoteCard({ lote }) {
  const [erroImagem, setErroImagem] = useState(false)
  const numero = String(lote.numero).padStart(2, '0')
  const temFoto = Boolean(lote.imagemPrincipal) && !erroImagem

  return (
    <article className="lote-card">
      <div className="lote-foto">
        {temFoto ? (
          <img
            src={lote.imagemPrincipal}
            alt={lote.nomeAnimal}
            loading="lazy"
            onError={() => setErroImagem(true)}
          />
        ) : (
          <span className="lote-foto-vazia">Foto em breve</span>
        )}
        <span className="lote-selo">Lote {numero}</span>
      </div>

      <div className="lote-corpo">
        <h3>{lote.nomeAnimal}</h3>
        <p className="lote-ficha">
          {[lote.raca, lote.sexo, lote.registro && `Registro ${lote.registro}`]
            .filter(Boolean)
            .join(' · ')}
        </p>

        <span className="lote-oferta">{rotuloOferta(lote.tipoOferta)}</span>

        {lote.descricaoOferta && (
          <p className="lote-desc">{lote.descricaoOferta}</p>
        )}

        <dl className="lote-valores">
          <div>
            <dt>Lance atual</dt>
            <dd>{lote.lanceAtual ? formatarMoeda(lote.lanceAtual) : 'Sem lances'}</dd>
          </div>
          <div>
            <dt>Próximo lance</dt>
            <dd className="lote-proximo">{formatarMoeda(lote.proximoLance)}</dd>
          </div>
        </dl>
      </div>
    </article>
  )
}

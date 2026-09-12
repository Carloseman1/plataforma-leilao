import useContagemRegressiva from '../../hooks/useContagemRegressiva'
import { formatarMoeda } from '../../utils/format'
import { rotuloOferta } from '../../utils/lote'

export default function LoteEmPregao({ lote, children }) {
  const restantes = useContagemRegressiva(lote.fechaEm)
  const numero = String(lote.numero).padStart(2, '0')
  const acabando = restantes !== null && restantes <= 10

  return (
    <article className="pregao-lote">
      <div className="pregao-foto">
        {lote.imagemPrincipal ? (
          <img src={lote.imagemPrincipal} alt={lote.nomeAnimal} />
        ) : (
          <span className="lote-foto-vazia">Foto em breve</span>
        )}
        <span className="lote-selo">Lote {numero}</span>
      </div>

      <div className="pregao-dados">
        <p className={`pregao-relogio ${acabando ? 'pregao-relogio--fim' : ''}`}>
          {restantes === null ? '—' : `${restantes}s`}
        </p>

        <h2>{lote.nomeAnimal}</h2>
        <p className="lote-ficha">
          {[lote.raca, lote.sexo, lote.registro && `Registro ${lote.registro}`]
            .filter(Boolean)
            .join(' · ')}
        </p>
        <span className="lote-oferta">{rotuloOferta(lote.tipoOferta)}</span>

        <dl className="pregao-valores">
          <div>
            <dt>Lance atual</dt>
            <dd className="pregao-valor-atual">
              {lote.lanceAtual ? formatarMoeda(lote.lanceAtual) : 'Sem lances'}
            </dd>
            {lote.comprador && (
              <dd className="pregao-comprador">
                {lote.lanceDaCasa ? (
                  <span className="etiqueta-casa">Lance da casa</span>
                ) : (
                  lote.comprador
                )}
              </dd>
            )}
          </div>
          <div>
            <dt>Próximo lance</dt>
            <dd className="pregao-valor-proximo">{formatarMoeda(lote.proximoLance)}</dd>
          </div>
        </dl>

        {children}
      </div>
    </article>
  )
}

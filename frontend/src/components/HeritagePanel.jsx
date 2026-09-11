/**
 * Painel esquerdo (marca / herança visual).
 * Compartilhado por Login e Cadastro.
 */

import HorseLineArt from './HorseLineArt'

export default function HeritagePanel() {
  return (
    <section className="heritage">
      <div className="brand">
        <p className="brand-mark">Haras Real</p>
        <h1 className="brand-name">
          Leilões de cavalos
          <br />
          de linhagem certificada
        </h1>
      </div>

      <div className="horse-wrap">
        <HorseLineArt />
      </div>

      <div className="heritage-foot">
        <p className="foot-line">
          Registro genealógico verificado, procedência documentada e
          acompanhamento veterinário em cada lote.
        </p>
        <div className="foot-stat">
          <span className="n">312</span>
          <span className="l">lotes no próximo leilão</span>
        </div>
      </div>
    </section>
  )
}

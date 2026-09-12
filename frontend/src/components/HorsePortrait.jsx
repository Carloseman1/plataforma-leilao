import elfoDoPortoAzul from '../assets/elfo-do-porto-azul.png'

export default function HorsePortrait() {
  return (
    <figure className="horse-wrap">
      <div className="horse-frame">
        <img
          className="horse-photo"
          src={elfoDoPortoAzul}
          alt="Elfo do Porto Azul, garanhão de linhagem certificada"
        />
        <span className="horse-scrim" aria-hidden="true" />
        <span className="horse-inner-rule" aria-hidden="true" />
      </div>

      <figcaption className="horse-caption">
        <span className="horse-name">Elfo do Porto Azul</span>
        <span className="horse-tag">
          <i aria-hidden="true" />
          Cobertura em leilão
          <i aria-hidden="true" />
        </span>
      </figcaption>
    </figure>
  )
}

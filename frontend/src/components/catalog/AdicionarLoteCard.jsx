export default function AdicionarLoteCard({ onClick }) {
  return (
    <button type="button" className="lote-card lote-novo" onClick={onClick}>
      <span className="lote-novo-sinal" aria-hidden="true">
        +
      </span>
      <span className="lote-novo-texto">Adicionar animal</span>
    </button>
  )
}

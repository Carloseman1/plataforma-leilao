export default function ProvocarCasos({ lote, aoEnviar, aoReenviar, temUltimoLance }) {
  const proximo = Number(lote.proximoLance)
  const incremento = Number(lote.incrementoMinimo)

  const casos = [
    {
      rotulo: 'Lance válido',
      detalhe: 'Entra e vira o lance atual',
      acao: () => aoEnviar(lote.uuid, proximo),
    },
    {
      rotulo: 'Abaixo do pedido',
      detalhe: 'Valor insuficiente',
      acao: () => aoEnviar(lote.uuid, Math.max(proximo - incremento, 1)),
    },
    {
      rotulo: 'Fora do incremento',
      detalhe: 'Valor quebrado no meio do degrau',
      acao: () => aoEnviar(lote.uuid, proximo + incremento / 2),
    },
    {
      rotulo: 'Valor absurdo',
      detalhe: 'Erro de digitação',
      acao: () => aoEnviar(lote.uuid, proximo + incremento * 10000),
    },
  ]

  return (
    <section className="provocar">
      <h3 className="admin-subtitle">Reproduzir um caso</h3>

      <div className="provocar-botoes">
        {casos.map((caso) => (
          <button key={caso.rotulo} type="button" className="provocar-botao" onClick={caso.acao}>
            <strong>{caso.rotulo}</strong>
            <span>{caso.detalhe}</span>
          </button>
        ))}

        <button
          type="button"
          className="provocar-botao"
          onClick={aoReenviar}
          disabled={!temUltimoLance}
        >
          <strong>Reenviar o último</strong>
          <span>Mesma chave — deve voltar como repetido</span>
        </button>
      </div>
    </section>
  )
}

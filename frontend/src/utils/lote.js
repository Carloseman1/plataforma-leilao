export const TIPOS_DE_OFERTA = [
  { valor: 'VENDA_ANIMAL', rotulo: 'Venda de animal' },
  { valor: 'COBERTURA', rotulo: 'Cobertura' },
  { valor: 'EMBRIAO', rotulo: 'Embrião' },
  { valor: 'OUTRO', rotulo: 'Oferta especial' },
]

export function rotuloOferta(tipo) {
  return TIPOS_DE_OFERTA.find((item) => item.valor === tipo)?.rotulo || tipo
}

export function nascimentoPorIdade(idadeAnos) {
  if (!idadeAnos) return null
  const data = new Date()
  data.setFullYear(data.getFullYear() - Number(idadeAnos))
  return data.toISOString().slice(0, 10)
}

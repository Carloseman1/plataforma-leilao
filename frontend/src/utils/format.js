/**
 * Formatação de datas e valores para o catálogo.
 */

export function formatarDataHora(iso) {
  if (!iso) return '—'
  const data = new Date(iso)
  return data.toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export function formatarMoeda(valor) {
  if (valor == null) return '—'
  return Number(valor).toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  })
}

export function rotuloStatus(status) {
  const mapa = {
    AGENDADO: 'Agendado',
    EM_ANDAMENTO: 'Em andamento',
    ENCERRADO: 'Encerrado',
  }
  return mapa[status] || status
}

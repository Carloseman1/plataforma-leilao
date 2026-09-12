import { get, post } from './httpClient'

export function listarLeiloes() {
  return get('/api/leiloes')
}

export function buscarLeilao(uuid) {
  return get(`/api/leiloes/${uuid}`)
}

export function criarLeilao(body) {
  return post('/api/leiloes', body)
}

export function adicionarLote(uuidLeilao, body) {
  return post(`/api/leiloes/${uuidLeilao}/lotes`, body)
}

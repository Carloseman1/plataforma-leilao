import { get, post } from './httpClient'

export function buscarPregao(uuidLeilao) {
  return get(`/api/leiloes/${uuidLeilao}/pregao`)
}

export function iniciarPregao(uuidLeilao) {
  return post(`/api/leiloes/${uuidLeilao}/pregao/iniciar`, {})
}

export function avancarPregao(uuidLeilao) {
  return post(`/api/leiloes/${uuidLeilao}/pregao/avancar`, {})
}

export function encerrarPregao(uuidLeilao) {
  return post(`/api/leiloes/${uuidLeilao}/pregao/encerrar`, {})
}

/**
 * O lanceUuid vai daqui de propósito: reenviar o mesmo lance com a mesma chave
 * é reconhecido como repetição no servidor, em vez de virar um segundo lance.
 */
export function darLance(uuidLote, valor, lanceUuid) {
  return post(`/api/lotes/${uuidLote}/lances`, { lanceUuid, valor })
}

export function listarRecusas(uuidLeilao) {
  return get(`/api/leiloes/${uuidLeilao}/recusas`)
}

export function listarFalhas() {
  return get('/api/pregao/falhas')
}

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

export function reabrirPregao(uuidLeilao) {
  return post(`/api/leiloes/${uuidLeilao}/pregao/reabrir`, {})
}

export function darLance(uuidLote, valor, lanceUuid) {
  return post(`/api/lotes/${uuidLote}/lances`, { lanceUuid, valor })
}

export function listarRecusas(uuidLeilao) {
  return get(`/api/leiloes/${uuidLeilao}/recusas`)
}

export function listarFalhas() {
  return get('/api/pregao/falhas')
}

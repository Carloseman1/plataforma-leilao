/**
 * Serviço de leilões — catálogo e detalhe.
 */

import { get, post } from './httpClient'

export function listarLeiloes() {
  return get('/api/leiloes')
}

export function buscarLeilao(id) {
  return get(`/api/leiloes/${id}`)
}

export function criarLeilao(body) {
  return post('/api/leiloes', body)
}

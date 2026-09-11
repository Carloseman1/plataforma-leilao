/**
 * Serviço de usuário — rotas de autenticação.
 */

import { post } from './httpClient'

export function cadastrarUsuario({ email, password }) {
  return post('/api/user/cadastrar', { email, password })
}

/** @returns {Promise<{ id: number, email: string, token: string }>} */
export function loginUsuario({ email, password }) {
  return post('/api/user/login', { email, password })
}

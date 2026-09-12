import { post } from './httpClient'

export function cadastrarUsuario({ email, password }) {
  return post('/api/user/cadastrar', { email, password })
}

export function loginUsuario({ email, password }) {
  return post('/api/user/login', { email, password })
}

/**
 * API de administração (grupos e usuários).
 */

import { get, post, put } from './httpClient'

export function listarGrupos() {
  return get('/api/admin/grupos')
}

export function criarGrupo(body) {
  return post('/api/admin/grupos', body)
}

export function atualizarGrupo(id, body) {
  return put(`/api/admin/grupos/${id}`, body)
}

export function listarUsuarios() {
  return get('/api/admin/usuarios')
}

export function atribuirGrupo(userId, grupoId) {
  return put(`/api/admin/usuarios/${userId}/grupo`, { grupoId })
}

export function listarPermissoesDisponiveis() {
  return get('/api/admin/permissoes')
}

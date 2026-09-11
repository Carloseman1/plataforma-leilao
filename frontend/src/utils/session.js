/**
 * Sessão no navegador: guarda o JWT + dados básicos do usuário.
 */

const STORAGE_KEY = 'haras_real_sessao'

export function getSessao() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function getToken() {
  return getSessao()?.token || null
}

export function salvarSessao(dados) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(dados))
}

export function limparSessao() {
  localStorage.removeItem(STORAGE_KEY)
}

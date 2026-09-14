const STORAGE_KEY = 'haras_real_sessao'

function lerSessao(storage) {
  try {
    const raw = storage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function decodificarPayload(token) {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const bytes = atob(base64)
    const json = decodeURIComponent(
      Array.from(bytes, (c) => `%${c.charCodeAt(0).toString(16).padStart(2, '0')}`).join(''),
    )
    return JSON.parse(json)
  } catch {
    return null
  }
}

export function getExpiracaoToken(token) {
  const exp = decodificarPayload(token)?.exp
  return typeof exp === 'number' ? exp * 1000 : null
}

export function tokenExpirado(token) {
  const expiraEm = getExpiracaoToken(token)
  return expiraEm !== null && expiraEm <= Date.now()
}

export function getSessao() {
  const sessao = lerSessao(localStorage) || lerSessao(sessionStorage)

  if (!sessao?.token) {
    return null
  }

  if (tokenExpirado(sessao.token)) {
    limparSessao()
    return null
  }

  return sessao
}

export function getToken() {
  return getSessao()?.token || null
}

export function salvarSessao(dados, { lembrar = true } = {}) {
  limparSessao()
  const storage = lembrar ? localStorage : sessionStorage
  storage.setItem(STORAGE_KEY, JSON.stringify(dados))
}

export function limparSessao() {
  localStorage.removeItem(STORAGE_KEY)
  sessionStorage.removeItem(STORAGE_KEY)
}

/**
 * Cliente HTTP genérico.
 * Envia o JWT no header Authorization quando existir.
 */

import { getToken, limparSessao } from '../utils/session'

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

async function request(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  }

  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  })

  if (response.status === 401 && !path.includes('/api/user/login')) {
    limparSessao()
    window.location.assign('/login')
    throw new Error('Sessão expirada. Faça login novamente.')
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}))
    throw new Error(errorBody.message || 'Erro na requisição')
  }

  if (response.status === 204) {
    return null
  }

  const text = await response.text()
  if (!text) {
    return null
  }

  return JSON.parse(text)
}

export function get(path) {
  return request(path, { method: 'GET' })
}

export function post(path, body) {
  return request(path, {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function put(path, body) {
  return request(path, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

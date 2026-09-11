/**
 * Contexto de autenticação.
 * Sessão = JWT salvo no localStorage.
 */

import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { getSessao, limparSessao, salvarSessao } from '../utils/session'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => getSessao())

  const login = useCallback((dados) => {
    // dados: { id, email, token }
    salvarSessao(dados)
    setUsuario(dados)
  }, [])

  const logout = useCallback(() => {
    limparSessao()
    setUsuario(null)
  }, [])

  const value = useMemo(
    () => ({
      usuario,
      isAuthenticated: Boolean(usuario?.token),
      login,
      logout,
    }),
    [usuario, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export default function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  return ctx
}

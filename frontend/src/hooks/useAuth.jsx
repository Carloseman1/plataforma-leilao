import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { getExpiracaoToken, getSessao, limparSessao, salvarSessao } from '../utils/session'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => getSessao())

  const login = useCallback((dados, opcoes) => {
    salvarSessao(dados, opcoes)
    setUsuario(dados)
  }, [])

  const logout = useCallback(() => {
    limparSessao()
    setUsuario(null)
  }, [])

  // Mantém o estado alinhado ao storage: outra aba que fez login/logout, ou um
  // token que expirou enquanto a aba estava em segundo plano.
  const sincronizar = useCallback(() => {
    const sessao = getSessao()
    setUsuario((anterior) => {
      if (!sessao) return anterior === null ? anterior : null
      if (anterior?.token === sessao.token) return anterior
      return sessao
    })
  }, [])

  useEffect(() => {
    window.addEventListener('focus', sincronizar)
    window.addEventListener('storage', sincronizar)
    document.addEventListener('visibilitychange', sincronizar)
    return () => {
      window.removeEventListener('focus', sincronizar)
      window.removeEventListener('storage', sincronizar)
      document.removeEventListener('visibilitychange', sincronizar)
    }
  }, [sincronizar])

  // Encerra a sessão no exato momento em que o JWT expira, sem depender de
  // uma nova requisição para descobrir que o token morreu.
  useEffect(() => {
    if (!usuario?.token) return undefined

    const expiraEm = getExpiracaoToken(usuario.token)
    if (expiraEm === null) return undefined

    const timer = setTimeout(logout, Math.max(expiraEm - Date.now(), 0))
    return () => clearTimeout(timer)
  }, [usuario, logout])

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

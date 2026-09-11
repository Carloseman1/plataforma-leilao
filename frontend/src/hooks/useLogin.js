/**
 * Hook do formulário de login.
 * Em sucesso, grava sessão via contexto e redireciona ao catálogo.
 */

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { loginUsuario } from '../api/userService'
import useAuth from './useAuth'

export default function useLogin() {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setErro('')
    setSucesso('')
    setLoading(true)

    try {
      const usuario = await loginUsuario({ email, password })
      login(usuario)
      navigate('/catalogo')
    } catch (err) {
      setErro(err.message)
    } finally {
      setLoading(false)
    }
  }

  return {
    email,
    setEmail,
    password,
    setPassword,
    erro,
    sucesso,
    loading,
    handleSubmit,
  }
}

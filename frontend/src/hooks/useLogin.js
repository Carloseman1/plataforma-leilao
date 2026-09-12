import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { loginUsuario } from '../api/userService'
import useAuth from './useAuth'

export default function useLogin() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [lembrar, setLembrar] = useState(false)
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
      login(usuario, { lembrar })
      navigate(location.state?.from?.pathname || '/catalogo', { replace: true })
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
    lembrar,
    setLembrar,
    erro,
    sucesso,
    loading,
    handleSubmit,
  }
}

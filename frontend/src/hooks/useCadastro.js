import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { cadastrarUsuario } from '../api/userService'
import { senhaEhValida } from '../utils/passwordRules'

export default function useCadastro() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [erro, setErro] = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setErro('')

    if (!senhaEhValida(password)) {
      setErro(
        'A senha deve ter pelo menos 8 caracteres, uma maiúscula, uma minúscula, um número e um caractere especial.',
      )
      return
    }

    setLoading(true)

    try {
      await cadastrarUsuario({ email, password })
      navigate('/login', {
        state: { mensagem: 'Cadastro realizado! Faça login.' },
      })
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
    loading,
    handleSubmit,
  }
}

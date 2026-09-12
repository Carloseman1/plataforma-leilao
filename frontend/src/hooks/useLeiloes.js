import { useEffect, useState } from 'react'
import { listarLeiloes } from '../api/leilaoService'

export default function useLeiloes() {
  const [leiloes, setLeiloes] = useState([])
  const [loading, setLoading] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true

    async function carregar() {
      setLoading(true)
      setErro('')
      try {
        const dados = await listarLeiloes()
        if (ativo) setLeiloes(dados)
      } catch (err) {
        if (ativo) setErro(err.message)
      } finally {
        if (ativo) setLoading(false)
      }
    }

    carregar()
    return () => {
      ativo = false
    }
  }, [])

  return { leiloes, loading, erro }
}

import { useCallback, useEffect, useState } from 'react'
import { buscarLeilao } from '../api/leilaoService'

export default function useLeilaoDetalhe(uuid) {
  const [leilao, setLeilao] = useState(null)
  const [loading, setLoading] = useState(true)
  const [erro, setErro] = useState('')
  const [recargas, setRecargas] = useState(0)

  const recarregar = useCallback(() => setRecargas((total) => total + 1), [])

  useEffect(() => {
    if (!uuid) return undefined

    let ativo = true

    async function carregar() {
      setLoading(true)
      setErro('')
      try {
        const dados = await buscarLeilao(uuid)
        if (ativo) setLeilao(dados)
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
  }, [uuid, recargas])

  return { leilao, loading, erro, recarregar }
}

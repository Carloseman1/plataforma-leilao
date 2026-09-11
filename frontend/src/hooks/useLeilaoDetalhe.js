/**
 * Hook: detalhe de um leilão (com lotes).
 */

import { useEffect, useState } from 'react'
import { buscarLeilao } from '../api/leilaoService'

export default function useLeilaoDetalhe(id) {
  const [leilao, setLeilao] = useState(null)
  const [loading, setLoading] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    if (!id) return undefined

    let ativo = true

    async function carregar() {
      setLoading(true)
      setErro('')
      try {
        const dados = await buscarLeilao(id)
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
  }, [id])

  return { leilao, loading, erro }
}

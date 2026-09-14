import { useCallback, useRef, useState } from 'react'
import { darLance } from '../api/pregaoService'

export default function useDarLance() {
  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState('')
  const ultimoLance = useRef(null)

  const enviar = useCallback(async (uuidLote, valor, lanceUuid) => {
    setErro('')
    setEnviando(true)

    const chave = lanceUuid || crypto.randomUUID()

    try {
      await darLance(uuidLote, valor, chave)
      ultimoLance.current = { uuidLote, valor, lanceUuid: chave }
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }, [])

  const reenviarUltimo = useCallback(async () => {
    const ultimo = ultimoLance.current
    if (!ultimo) return
    await enviar(ultimo.uuidLote, ultimo.valor, ultimo.lanceUuid)
  }, [enviar])

  return { enviar, reenviarUltimo, temUltimoLance: () => ultimoLance.current !== null, enviando, erro }
}

import { useCallback, useRef, useState } from 'react'
import { darLance } from '../api/pregaoService'

/**
 * Envia lances e guarda a chave do último.
 *
 * Guardar a chave é o que permite reenviar exatamente a mesma tentativa — é
 * assim que se distingue "o comprador clicou duas vezes" de "o comprador deu
 * dois lances".
 */
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

  /** Reenvia o último lance com a mesma chave: deve voltar como repetido. */
  const reenviarUltimo = useCallback(async () => {
    const ultimo = ultimoLance.current
    if (!ultimo) return
    await enviar(ultimo.uuidLote, ultimo.valor, ultimo.lanceUuid)
  }, [enviar])

  return { enviar, reenviarUltimo, temUltimoLance: () => ultimoLance.current !== null, enviando, erro }
}

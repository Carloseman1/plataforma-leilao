import { useCallback, useEffect, useRef, useState } from 'react'
import { assinarPregao } from '../api/canalDoPregao'
import { buscarPregao } from '../api/pregaoService'

const LANCES_NO_FEED = 30

export default function usePregao(uuidLeilao) {
  const [pregao, setPregao] = useState(null)
  const [lances, setLances] = useState([])
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [conectado, setConectado] = useState(false)

  const recarregar = useCallback(async () => {
    try {
      setPregao(await buscarPregao(uuidLeilao))
      setErro('')
    } catch (err) {
      setErro(err.message)
    } finally {
      setCarregando(false)
    }
  }, [uuidLeilao])

  const recarregarRef = useRef(recarregar)

  useEffect(() => {
    recarregarRef.current = recarregar
  }, [recarregar])

  useEffect(() => {
    recarregar()
  }, [recarregar])

  useEffect(() => {
    if (!uuidLeilao) return undefined

    return assinarPregao(uuidLeilao, {
      aoMudarConexao: setConectado,
      aoReceber: (evento) => {
        if (evento.tipo === 'LANCE') {
          registrarLance(evento.lance)
          return
        }
        recarregarRef.current()
      },
    })

    function registrarLance(lance) {
      setLances((anteriores) => [lance, ...anteriores].slice(0, LANCES_NO_FEED))

      if (!lance.aceito) return

      setPregao((atual) => {
        if (!atual?.loteAtual || atual.loteAtual.uuid !== lance.loteUuid) {
          return atual
        }
        return {
          ...atual,
          loteAtual: {
            ...atual.loteAtual,
            lanceAtual: lance.lanceAtual,
            proximoLance: lance.proximoLance,
            comprador: lance.comprador,
            lanceDaCasa: lance.daCasa,
          },
        }
      })
    }
  }, [uuidLeilao])

  return { pregao, lances, erro, carregando, conectado, recarregar }
}

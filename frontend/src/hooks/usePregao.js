import { useCallback, useEffect, useRef, useState } from 'react'
import { assinarPregao } from '../api/canalDoPregao'
import { buscarPregao } from '../api/pregaoService'

const LANCES_NO_FEED = 30

/**
 * Estado do pregão ao vivo.
 *
 * O estado completo vem por REST; o canal traz o que muda com a tela aberta.
 * Lance aceito é aplicado na hora para a tela responder rápido, e troca de lote
 * ou de status do leilão recarrega tudo — nesses casos muita coisa muda junto e
 * remendar em pedaços sairia errado.
 */
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

  // O canal precisa chamar a versão mais recente de recarregar sem se reassinar
  // a cada render.
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

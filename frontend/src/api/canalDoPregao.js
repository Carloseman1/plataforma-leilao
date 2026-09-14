import { Client } from '@stomp/stompjs'
import { getToken } from '../utils/session'

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const WS_URL = `${BASE_URL.replace(/^http/, 'ws')}/ws`

export function assinarPregao(uuidLeilao, { aoReceber, aoMudarConexao }) {
  const cliente = new Client({
    brokerURL: WS_URL,
    reconnectDelay: 3000,
    connectHeaders: { Authorization: `Bearer ${getToken()}` },
    onConnect: () => {
      aoMudarConexao(true)
      cliente.subscribe(`/topic/pregao/${uuidLeilao}`, (mensagem) => {
        aoReceber(JSON.parse(mensagem.body))
      })
    },
    onWebSocketClose: () => aoMudarConexao(false),
    onStompError: () => aoMudarConexao(false),
  })

  cliente.activate()

  return () => cliente.deactivate()
}

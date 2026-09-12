import { useEffect, useState } from 'react'

/** Segundos que faltam até o horário informado, atualizados a cada segundo. */
export default function useContagemRegressiva(fechaEm) {
  const [restantes, setRestantes] = useState(() => segundosAte(fechaEm))

  useEffect(() => {
    setRestantes(segundosAte(fechaEm))

    if (!fechaEm) return undefined

    const timer = setInterval(() => setRestantes(segundosAte(fechaEm)), 1000)
    return () => clearInterval(timer)
  }, [fechaEm])

  return restantes
}

function segundosAte(fechaEm) {
  if (!fechaEm) return null
  return Math.max(0, Math.ceil((new Date(fechaEm) - Date.now()) / 1000))
}

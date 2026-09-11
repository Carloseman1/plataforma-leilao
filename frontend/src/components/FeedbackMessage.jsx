/**
 * Mensagem de feedback (erro ou sucesso).
 */

export default function FeedbackMessage({ type = 'error', children }) {
  if (!children) return null

  return <p className={`feedback feedback--${type}`}>{children}</p>
}

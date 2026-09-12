export default function FeedbackMessage({ type = 'error', children }) {
  if (!children) return null

  return (
    <p
      className={`feedback feedback--${type}`}
      role={type === 'error' ? 'alert' : 'status'}
    >
      {children}
    </p>
  )
}

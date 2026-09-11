/**
 * Layout das telas de autenticação (Haras Real).
 * Esquerda = marca | Direita = formulário (children).
 */

import { Link } from 'react-router-dom'
import HeritagePanel from './HeritagePanel'
import '../styles/auth.css'

export default function AuthLayout({ title, subtitle, children, footer }) {
  return (
    <div className="stage">
      <HeritagePanel />

      <section className="desk">
        <div className="form-card">
          <div className="form-head">
            <h1>{title}</h1>
            {subtitle && <p>{subtitle}</p>}
          </div>

          {children}

          {footer}
        </div>
      </section>
    </div>
  )
}

export function AuthLink({ to, children }) {
  return (
    <Link className="link" to={to}>
      {children}
    </Link>
  )
}

import { useState } from 'react'
import FormField from './FormField'

export default function PasswordField({
  id,
  label = 'Senha',
  value,
  onChange,
  autoComplete,
  placeholder,
  minLength,
}) {
  const [visivel, setVisivel] = useState(false)

  const botaoVisibilidade = (
    <button
      type="button"
      className="field-action"
      onClick={() => setVisivel(!visivel)}
      aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
    >
      {visivel ? 'Ocultar' : 'Mostrar'}
    </button>
  )

  return (
    <FormField
      id={id}
      label={label}
      type={visivel ? 'text' : 'password'}
      value={value}
      onChange={onChange}
      autoComplete={autoComplete}
      placeholder={placeholder}
      minLength={minLength}
      action={botaoVisibilidade}
    />
  )
}

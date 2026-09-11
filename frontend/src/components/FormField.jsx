/**
 * Campo de formulário (label + input) no estilo Haras Real.
 */

export default function FormField({
  id,
  label,
  type = 'text',
  value,
  onChange,
  autoComplete,
  placeholder,
  required = true,
  minLength,
}) {
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={onChange}
        autoComplete={autoComplete}
        placeholder={placeholder}
        required={required}
        minLength={minLength}
      />
    </div>
  )
}

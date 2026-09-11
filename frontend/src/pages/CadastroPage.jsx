/**
 * Página de Cadastro — mesmo layout Haras Real do login.
 */

import AuthLayout, { AuthLink } from '../components/AuthLayout'
import FormField from '../components/FormField'
import Button from '../components/Button'
import FeedbackMessage from '../components/FeedbackMessage'
import useCadastro from '../hooks/useCadastro'
import { SENHA_HINT, SENHA_MIN_LENGTH } from '../utils/passwordRules'

export default function CadastroPage() {
  const {
    email,
    setEmail,
    password,
    setPassword,
    erro,
    loading,
    handleSubmit,
  } = useCadastro()

  return (
    <AuthLayout
      title="Criar sua conta"
      subtitle="Cadastre-se para acompanhar leilões, favoritos e participar com lances."
      footer={
        <>
          <div className="divider">ou</div>
          <p className="signup">
            Já tem conta? <AuthLink to="/login">Entrar</AuthLink>
          </p>
        </>
      }
    >
      <form onSubmit={handleSubmit} autoComplete="on">
        <FormField
          id="cadastro-email"
          label="E-mail"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoComplete="email"
          placeholder="seunome@email.com"
        />

        <FormField
          id="cadastro-password"
          label="Senha"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="new-password"
          placeholder="Crie uma senha segura"
          minLength={SENHA_MIN_LENGTH}
        />

        <p className="auth-hint">{SENHA_HINT}</p>

        <FeedbackMessage type="error">{erro}</FeedbackMessage>

        <Button type="submit" disabled={loading}>
          {loading ? 'Cadastrando...' : 'Cadastrar'}
        </Button>

        <div className="credential-note">
          Para dar lances é necessário ter cadastro validado com documento e
          comprovante de residência.
        </div>
      </form>
    </AuthLayout>
  )
}

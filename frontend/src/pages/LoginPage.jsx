import { useLocation } from 'react-router-dom'
import AuthLayout, { AuthLink } from '../components/AuthLayout'
import FormField from '../components/FormField'
import PasswordField from '../components/PasswordField'
import Button from '../components/Button'
import FeedbackMessage from '../components/FeedbackMessage'
import useLogin from '../hooks/useLogin'
import { SENHA_MIN_LENGTH } from '../utils/passwordRules'

export default function LoginPage() {
  const location = useLocation()
  const mensagemCadastro = location.state?.mensagem

  const {
    email,
    setEmail,
    password,
    setPassword,
    lembrar,
    setLembrar,
    erro,
    sucesso,
    loading,
    handleSubmit,
  } = useLogin()

  return (
    <AuthLayout
      title="Entrar na sua conta"
      subtitle="Acompanhe seus lances, favoritos e o histórico de leilões que você participou."
      footer={
        <>
          <div className="divider">ou</div>
          <p className="signup">
            Ainda não tem conta?{' '}
            <AuthLink to="/cadastro">Cadastre-se para participar</AuthLink>
          </p>
        </>
      }
    >
      <form onSubmit={handleSubmit} autoComplete="on">
        <FormField
          id="email"
          label="E-mail"
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          autoComplete="email"
          placeholder="seunome@email.com"
        />

        <PasswordField
          id="senha"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
          placeholder="Digite sua senha"
          minLength={SENHA_MIN_LENGTH}
        />

        <div className="row-between">
          <label className="remember">
            <input
              type="checkbox"
              checked={lembrar}
              onChange={(e) => setLembrar(e.target.checked)}
            />
            Manter-me conectado
          </label>
          <a className="link" href="#">
            Esqueceu a senha?
          </a>
        </div>

        <FeedbackMessage type="success">
          {mensagemCadastro || sucesso}
        </FeedbackMessage>
        <FeedbackMessage type="error">{erro}</FeedbackMessage>

        <Button type="submit" disabled={loading}>
          {loading ? 'Entrando...' : 'Entrar'}
        </Button>

        <div className="credential-note">
          Para dar lances é necessário ter cadastro validado com documento e
          comprovante de residência.
        </div>
      </form>
    </AuthLayout>
  )
}

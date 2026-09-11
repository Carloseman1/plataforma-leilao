/**
 * Regras de senha alinhadas ao backend (UserService).
 * Usamos no front só para orientar o usuário antes de enviar.
 */

export const SENHA_MIN_LENGTH = 8

export const SENHA_HINT =
  'Mín. 8 caracteres, com maiúscula, minúscula, número e especial (@#$%^&+=!*)'

/** Mesma ideia do regex do backend — útil para validação opcional no front */
export const SENHA_REGEX =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%^&+=!*]).{8,}$/

export function senhaEhValida(senha) {
  return SENHA_REGEX.test(senha)
}

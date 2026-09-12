export const PERMISSOES = {
  CRIAR_LEILAO: 'CRIAR_LEILAO',
  GERENCIAR_GRUPOS: 'GERENCIAR_GRUPOS',
  GERENCIAR_USUARIOS: 'GERENCIAR_USUARIOS',
}

export function temPermissao(usuario, permissao) {
  if (!usuario) return false
  if (usuario.admin) return true
  return Array.isArray(usuario.permissoes) && usuario.permissoes.includes(permissao)
}

export function podeAdministrar(usuario) {
  return (
    temPermissao(usuario, PERMISSOES.GERENCIAR_GRUPOS) ||
    temPermissao(usuario, PERMISSOES.GERENCIAR_USUARIOS)
  )
}

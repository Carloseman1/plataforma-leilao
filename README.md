# Estrutura do Projeto - Tela de Login

## Controller

### DefaultExceptionHandler

Centraliza o tratamento de erros da API, mapeando cada exceção para uma resposta padronizada com base no DTO de erro (`code`, `message`). Isso evita logs extensos no terminal e facilita o rastreamento futuro de ocorrências. Os principais casos tratados são:

**Erro inesperado** — Exceções não mapeadas retornam `500 Internal Server Error`.

**E-mail já cadastrado** — Impede duplicidade no banco de dados retornando uma resposta legível com o código `EMAIL_CADASTRADO` e a mensagem `"O e-mail já está cadastrado."`, no lugar do stack trace padrão gerado pelo Spring.

---

### UserController

Responsável pelo mapeamento dos endpoints da API. Atualmente expõe o cadastro de usuário, recebendo `e-mail` e `senha` via `UserDTO`. O endpoint de login ainda será implementado, aproveitando as validações já existentes no `DefaultExceptionHandler`.

> Por padrão o Spring retorna `200 OK` em criações — o retorno foi ajustado para `201 Created`.

---

## DTO

**ErrorDTO** — Representa o contrato de erro da API com os campos `code` e `message`.

**UserDTO** — Transporta os dados essenciais do usuário (`e-mail` e `senha`) para o cadastro.

---

## Exceptions

Exceções customizadas para cada cenário de validação:

- E-mail não cadastrado
- Senha nula ou vazia
- Senha fora do padrão: menos de 8 caracteres, ausência de letra maiúscula, número ou caractere especial

---

## Service

### UserService

Contém toda a regra de negócio da aplicação. O método `cadastrar` realiza as seguintes etapas:

**1. Validação da senha** — Verifica se o campo é nulo e, em seguida, aplica um regex para garantir que a senha atenda ao padrão exigido. Em cada caso de falha, a exceção correspondente é lançada.

**2. Criptografia com BCrypt** — Após a validação, a senha é criptografada antes de ser persistida. O BCrypt foi escolhido pela sua robustez: o custo computacional do algoritmo dificulta ataques de força bruta em cenários de vazamento de dados, tornando-o mais seguro que alternativas como MD5.

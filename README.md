# Plataforma de Leilão — Haras Real

Plataforma de leilão de cavalos Mangalarga Marchador. Cobre o cadastro e login dos compradores, o catálogo de leilões e lotes, a administração de grupos e permissões, e o pregão ao vivo — onde os lances passam por uma fila no Kafka antes de serem julgados.

O documento está organizado assim: primeiro como rodar e como o projeto está montado, depois a referência de domínio e API, e no fim as anotações de estudo — o que foi decidido e onde o desenvolvimento travou.

---

## Stack

**Backend** — Java 17, Spring Boot 4.0.6, Spring Data JPA, Flyway, PostgreSQL, jjwt 0.12.6 para o token, BCrypt para a senha, Spring Kafka e WebSocket/STOMP.

**Frontend** — React 19, Vite 8, React Router 7, `@stomp/stompjs`. Sem biblioteca de estado nem de UI: o estado fica em hooks próprios e o estilo em CSS escrito à mão.

**Infra** — PostgreSQL e Redpanda (broker compatível com a API do Kafka), este último via `docker-compose.yml`.

---

## Como rodar

### Pré-requisitos

- JDK 17 ou superior
- PostgreSQL rodando em `localhost:5432` com um banco chamado `plataforma-leilao`
- Node 20 ou superior
- Docker, só para o pregão com Kafka — sem ele, veja [Sem Docker](#sem-docker)

### 1. Banco

O Flyway cria e migra o schema sozinho na inicialização. Só é preciso o banco vazio existir:

```sql
CREATE DATABASE "plataforma-leilao";
```

As credenciais estão em `src/main/resources/application.properties` (`postgres` / `xxxx`).

> `spring.jpa.hibernate.ddl-auto=validate`. O Hibernate não altera a estrutura do banco: quem faz isso é o Flyway. Campo novo na entidade sem migração correspondente derruba a aplicação na inicialização.

### 2. Broker (opcional)

O projeto sobe sem broker: o padrão é `app.pregao.transporte=direto`, que avalia o lance dentro da própria requisição. Para exercitar a fila de verdade:

```bash
docker compose up -d
```

Sobe o Redpanda em `localhost:9092` e o console web em `localhost:8081`, onde dá para ver os tópicos, as partições e as mensagens — que trafegam em JSON puro. Com o broker no ar, troque em `application.properties`:

```properties
app.pregao.transporte=kafka
```

A aplicação diz na subida qual modo está ativo. Deixar `kafka` sem broker não derruba nada, mas o consumidor fica tentando reconectar e o log enche de `Rebootstrapping`.

### 3. Backend

```bash
./mvnw spring-boot:run
```

Sobe em `localhost:8080`. Na primeira execução os seeds criam os grupos de permissão e um leilão de exemplo com três lotes.

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

Sobe em `localhost:5173`. A URL da API pode ser trocada pela variável `VITE_API_URL`.

### 5. Simulador de lances

```bash
pip install -r simulador/requirements.txt
python simulador/simular_pregao.py --leilao <uuid-do-leilao> --compradores 5
```

O uuid do leilão está na URL da tela de detalhe. Antes de rodar, o pregão precisa estar aberto — botão *Abrir pregão*.

Variações:

```bash
# intercala lances inválidos de propósito, um de cada tipo
python simulador/simular_pregao.py --leilao <uuid> --caos

# todos os compradores dão o mesmo valor no mesmo instante
python simulador/simular_pregao.py --leilao <uuid> --empate
```

O `--empate` é o que demonstra a regra de prioridade: só um lance entra, e os outros aparecem na tela de recusas como `VALOR_INSUFICIENTE`.

### Sem Docker

É o padrão do repositório (`app.pregao.transporte=direto`): o lance é avaliado dentro da própria requisição, sem broker. A trava do lote continua valendo, então o resultado é o mesmo — o que se perde é a ordenação por partição e o replay do tópico. Serve para desenvolver, não para valer.

### Primeiro acesso

Qualquer cadastro novo cai no grupo **Participante**, que não tem permissão nenhuma além de acompanhar e dar lances. O e-mail definido em `AdminConstants.EMAIL_ADMIN` é promovido a administrador automaticamente no cadastro e em todo login — é por onde se entra para administrar.

Para promover outra conta sem mexer no código, entre como administrador e use *Administração → Usuários*.

---

## Estrutura do projeto

```
src/main/java/com/plataforma_leilao/app/
├── config/          seeds, CORS, Kafka, WebSocket
├── controller/      endpoints REST e o handler de exceções
├── dto/             contratos de entrada e saída da API
├── exceptions/      exceções de negócio, uma por cenário
├── model/           entidades JPA e enums do domínio
├── pregao/          tudo do pregão ao vivo
│   └── kafka/       tópicos, publicador, consumidor, serialização
├── repository/      Spring Data
├── security/        JWT e usuário autenticado
└── service/         regra de negócio de usuário, admin e leilão

src/main/resources/db/migration/   migrações Flyway

frontend/src/
├── api/             clientes HTTP e o canal WebSocket
├── components/      componentes de tela, agrupados por área
├── hooks/           estado e efeitos — formulários, sessão, pregão
├── pages/           uma por rota
├── styles/          CSS por área
└── utils/           formatação, sessão, permissões

simulador/           script Python que simula compradores
docs/pregao.md       detalhamento da arquitetura do pregão
```

A separação que o frontend segue: **hook guarda estado e fala com a API, componente só desenha**. `useNovoLote` sabe montar e enviar o lote; `FormularioNovoLote` só sabe desenhar os campos.

---

## Modelo de domínio

```
usuario ──┐
          ├── lance ──► lote ──► leilao
animal ◄──┘              │
   │                     └──► animal
   └── pai / mae (auto-relacionamento)

grupo_permissao ──< grupo_permissao_item
        │
        └──► usuario
```

**usuario** — e-mail único, senha em BCrypt, papel (`ADMIN`/`USER`) e grupo de permissão. O campo `ativo` permite desativar sem apagar.

**grupo_permissao** — nome, descrição e um conjunto de permissões guardado em tabela auxiliar (`@ElementCollection`).

**animal** — nome, registro, raça, sexo, nascimento, pelagem, marcha, criador, proprietário, descrição, foto. Tem `pai` e `mae` apontando para outros animais, o que permite montar genealogia.

**leilao** — título, subtítulo, local, início, fim, status, e os marcos do pregão (`iniciadoEm`, `encerradoEm`). Contém a lista de lotes, ordenada por número.

**lote** — pertence a um leilão e aponta para um animal. Guarda número, tipo de oferta, descrição, valor inicial, incremento mínimo, valor de reserva, status, e a janela do pregão (`abertoEm`, `fechaEm`).

**lance** — pertence a um lote e, quando é de comprador, a um usuário. Guarda valor, tipo, status, motivo da recusa e o `uuid` que serve de chave de idempotência.

### Identificadores

`leilao`, `lote` e `animal` têm duas chaves: o `id BIGINT`, usado internamente e nas FKs, e o `uuid`, que é o que aparece na API e nas URLs. O motivo está em [Identificador público em UUID](#identificador-público-em-uuid).

### Enums

| Enum | Valores |
|---|---|
| `ELeilaoStatus` | `RASCUNHO`, `AGENDADO`, `EM_ANDAMENTO`, `ENCERRADO`, `CANCELADO` |
| `ELoteStatus` | `DISPONIVEL`, `ENCERRADO`, `VENDIDO`, `RESERVA_NAO_ATINGIDA` |
| `ETipoOferta` | `VENDA_ANIMAL`, `COBERTURA`, `EMBRIAO`, `OUTRO` |
| `ETipoLance` | `REAL` (comprador), `SIMULADO` (lance da casa) |
| `EStatusLance` | `VALIDO`, `INVALIDO`, `CANCELADO` |
| `EPermissao` | `CRIAR_LEILAO`, `GERENCIAR_GRUPOS`, `GERENCIAR_USUARIOS` |
| `EUserPermission` | `ADMIN`, `USER` |

---

## Autenticação e permissões

### O token

No login, `JwtService` emite um JWT assinado em HS256 com validade de 24 horas. Dentro dele vão o `jti`, o e-mail no `subject`, e os claims `userId`, `admin` e `permissoes`.

O frontend guarda a sessão em `localStorage` quando *Manter-me conectado* está marcado, e em `sessionStorage` quando não está.

### O filtro

`JwtAuthFilter` intercepta tudo que começa com `/api/`, com duas exceções públicas: `/api/user/login` e `/api/user/cadastrar`. Sem token válido, responde `401` com `{"code":"TOKEN_INVALIDO"}`. Com token válido, guarda o e-mail no atributo `userEmail` da requisição, que é de onde `UsuarioAutenticado` recupera o usuário.

O que não começa com `/api/` passa direto — inclusive o handshake do WebSocket em `/ws`, que por isso tem sua própria checagem no CONNECT do STOMP.

### Os dois níveis

Existe o papel (`ADMIN`/`USER`) e existe o grupo de permissões. `User.temPermissao()` resolve os dois: administrador pode tudo; os demais dependem do que o grupo concede.

Os seeds criam dois grupos: **Administrador**, com todas as permissões, e **Participante**, sem nenhuma.

`PermissaoService.exigir(permissao)` é o que barra no backend, lançando `AcessoNegadoException` (`403`). No frontend, `temPermissao()` em `utils/permissions.js` decide o que aparece na tela — mas quem de fato protege é o backend.

### As guardas de rota no frontend

`ProtectedRoute` manda para `/login` quem não tem sessão, guardando a rota pedida para voltar nela depois do login. `PublicRoute` faz o contrário: manda para `/catalogo` quem já está logado e tenta abrir `/login` ou `/cadastro`.

A sessão é considerada válida só enquanto o `exp` do token não passou — `getSessao()` lê a expiração do próprio JWT e descarta do storage o que venceu.

---

## Endpoints

Tudo sob `/api` exige o cabeçalho `Authorization: Bearer <token>`, menos as duas rotas de usuário.

### Usuário

| Método | Rota | O que faz |
|---|---|---|
| `POST` | `/api/user/cadastrar` | Cria a conta. `201` sem corpo |
| `POST` | `/api/user/login` | Devolve token, grupo, papel e permissões |

### Catálogo

| Método | Rota | Permissão | O que faz |
|---|---|---|---|
| `GET` | `/api/leiloes` | — | Lista os leilões com a contagem de lotes |
| `GET` | `/api/leiloes/{uuid}` | — | Leilão com todos os lotes |
| `POST` | `/api/leiloes` | `CRIAR_LEILAO` | Cria leilão, opcionalmente já com lotes |
| `POST` | `/api/leiloes/{uuid}/lotes` | `CRIAR_LEILAO` | Acrescenta um lote a um leilão existente |

No `POST` de lote, `numero` e `incrementoMinimo` são opcionais — o serviço completa com o próximo número livre do leilão e com o incremento padrão de R$ 500. O animal pode vir por `animalUuid`, reaproveitando um já cadastrado, ou por `animal`, criando um novo.

### Pregão

| Método | Rota | Permissão | O que faz |
|---|---|---|---|
| `GET` | `/api/leiloes/{uuid}/pregao` | — | Estado completo: lote aberto, fila e encerrados |
| `POST` | `/api/leiloes/{uuid}/pregao/iniciar` | `CRIAR_LEILAO` | Abre o pregão e o primeiro lote |
| `POST` | `/api/leiloes/{uuid}/pregao/avancar` | `CRIAR_LEILAO` | Bate o martelo e abre o próximo |
| `POST` | `/api/leiloes/{uuid}/pregao/encerrar` | `CRIAR_LEILAO` | Fecha o lote aberto e encerra o leilão |
| `POST` | `/api/lotes/{uuid}/lances` | — | Enfileira um lance. `202` |
| `GET` | `/api/leiloes/{uuid}/recusas` | `CRIAR_LEILAO` | Lances que não entraram, com o motivo |
| `GET` | `/api/pregao/falhas` | `CRIAR_LEILAO` | Mensagens que pararam na DLT |

O corpo do lance é `{ "lanceUuid": "...", "valor": 62000 }`. O `lanceUuid` é a chave de idempotência e deve vir do cliente; quando não vem, o servidor gera um, e aí cada reenvio conta como lance novo.

A resposta `202` diz que o lance **entrou na fila**, não que foi aceito. O desfecho chega pelo WebSocket.

### Administração

| Método | Rota | Permissão |
|---|---|---|
| `GET` | `/api/admin/permissoes` | `GERENCIAR_GRUPOS` |
| `GET` | `/api/admin/grupos` | `GERENCIAR_GRUPOS` |
| `POST` | `/api/admin/grupos` | `GERENCIAR_GRUPOS` |
| `PUT` | `/api/admin/grupos/{id}` | `GERENCIAR_GRUPOS` |
| `GET` | `/api/admin/usuarios` | `GERENCIAR_USUARIOS` |
| `PUT` | `/api/admin/usuarios/{id}/grupo` | `GERENCIAR_USUARIOS` |

### WebSocket

Endpoint STOMP em `/ws`, com o token no cabeçalho `Authorization` do CONNECT. O canal de um leilão é `/topic/pregao/{leilaoUuid}`, e por ele passam três tipos de evento: `LANCE`, `LOTE` e `PREGAO`.

### Códigos de erro

Todo erro sai no mesmo formato, `{ "code": "...", "message": "..." }`, montado pelo `DefaultExceptionHandler`.

| Código | HTTP | Quando |
|---|---|---|
| `CREDENCIAIS_INVALIDAS` | 401 | E-mail ou senha errados, ou conta inativa |
| `TOKEN_INVALIDO` | 401 | Token ausente, expirado ou adulterado |
| `ACESSO_NEGADO` | 403 | Falta a permissão exigida |
| `EMAIL_CADASTRADO` | 400 | E-mail já existe |
| `SENHA_VAZIA` | 400 | Senha nula |
| `SENHA_CADASTRADA` | 400 | Senha fora do padrão exigido |
| `PARAMETRO_INVALIDO` | 400 | UUID malformado na URL |
| `LEILAO_NAO_ENCONTRADO` | 404 | UUID válido, leilão inexistente |
| `GRUPO_NAO_ENCONTRADO` | 404 | — |
| `USUARIO_NAO_ENCONTRADO` | 404 | — |
| `LEILAO_ENCERRADO` | 409 | Tentativa de somar lote a leilão fechado |
| `PREGAO_INVALIDO` | 409 | Comando que não cabe no estado atual |
| `PREGAO_INDISPONIVEL` | 503 | Broker fora do ar |
| `ERRO_INESPERADO` | 500 | Exceção não mapeada |

---

## Pregão ao vivo

```
navegador → POST /api/lotes/{uuid}/lances → tópico leilao.lances → AvaliadorDeLances → banco
                   (202 Aceito)              (chave = uuid do lote)        │
                                                                          ↓
                                                    WebSocket → tela do pregão
```

### Os tópicos

| Tópico | Partições | Conteúdo |
|---|---|---|
| `leilao.lances` | 6 | Tentativas de lance, ainda sem julgamento |
| `leilao.lances-resultado` | 6 | O que o pregão decidiu sobre cada tentativa |
| `leilao.lances.DLT` | 1 | Mensagens que nem o retry processou |

A chave de toda mensagem é o uuid do lote. É isso que faz os lances de um mesmo lote caírem na mesma partição e serem avaliados em fila, um de cada vez, enquanto lotes diferentes seguem em paralelo.

### Como um lance é julgado

`AvaliadorDeLances` é o único ponto do sistema que altera o valor corrente de um lote. A ordem das checagens importa, porque é ela que decide qual motivo aparece quando mais de um se aplica:

1. **Já vi esse uuid?** → `DUPLICADO`, e responde o que aconteceu da primeira vez
2. **O lote existe?** → `LOTE_NAO_ENCONTRADO`
3. **O comprador existe?** → `SEM_HABILITACAO`
4. **O leilão está no ar?** → `LEILAO_FORA_DO_AR`
5. **O lote ainda está aberto?** → `LOTE_ENCERRADO`
6. **O pregão já chegou nele?** → `LOTE_NAO_ABERTO`
7. **O comprador já está ganhando?** → `AUTO_LANCE`
8. **O valor cobre o próximo lance?** → `VALOR_INSUFICIENTE`
9. **Respeita o incremento?** → `INCREMENTO_INVALIDO`
10. **Está dentro do teto de sanidade?** → `VALOR_SUSPEITO`

Passando por tudo, o lance entra, vira o valor corrente e empurra o cronômetro se estiver perto do fim.

Todo lance avaliado vira linha na tabela `lance`: aceito com `VALIDO`, recusado com `INVALIDO` e o motivo. É desse histórico que sai a tela de recusas.

### Motivos de recusa

| Motivo | Quando acontece |
|---|---|
| `DUPLICADO` | Mesmo `lanceUuid` de novo — duplo clique, retry do cliente, reentrega do Kafka |
| `VALOR_INSUFICIENTE` | Alguém chegou antes e o preço subiu |
| `INCREMENTO_INVALIDO` | O valor não respeita o degrau do lote |
| `VALOR_SUSPEITO` | Acima de 100× o valor inicial — erro de digitação |
| `LOTE_NAO_ABERTO` | O pregão ainda não chegou nesse lote |
| `LOTE_ENCERRADO` | Lance depois do martelo |
| `LEILAO_FORA_DO_AR` | Leilão não iniciado, encerrado ou cancelado |
| `AUTO_LANCE` | O comprador já estava ganhando |
| `LOTE_NAO_ENCONTRADO` | Lote inexistente |
| `SEM_HABILITACAO` | Comprador não existe ou não pode dar lance |

Falha de infraestrutura é outra categoria: a mensagem que nem o retry conseguiu processar vai para a DLT e aparece em seção separada, porque ela não chegou a ser julgada.

### Soft close

Um lance dentro dos últimos 10 segundos empurra o fechamento para 10 segundos adiante. Sem isso, todo mundo espera o último instante e ganha quem tem a rede mais rápida.

### Lance da casa

Passados 8 segundos sem lance, a casa cobre em nome do vendedor — até o `valorReserva` do lote, e para ali. O lance sai pela mesma fila, com tipo `SIMULADO`, passa pelas mesmas regras e aparece identificado na tela.

A casa nunca cobre o próprio lance, senão subiria sozinha até a reserva.

### Como o lote termina

| Situação | Status final |
|---|---|
| Comprador real e valor ≥ reserva | `VENDIDO` |
| Comprador real e valor < reserva | `RESERVA_NAO_ATINGIDA` |
| Último de pé é o lance da casa | `ENCERRADO` — não vendeu |
| Nenhum lance | `ENCERRADO` |

### Martelo automático

`RelogioDoPregao` roda a cada segundo, procura lotes cujo `fechaEm` já passou e manda fechar. A lista que ele monta é só um palpite: quem confirma é `baterMartelo`, já com a linha do lote travada. Se o lote tiver recebido um lance no meio do caminho, o `fechaEm` estará adiado e nada acontece.

Fechado o lote, o próximo abre sozinho. Acabando os lotes, o leilão encerra.

---

## Frontend

### Rotas

| Rota | Guarda | Tela |
|---|---|---|
| `/` | — | Redireciona conforme a sessão |
| `/login` | `PublicRoute` | Entrar |
| `/cadastro` | `PublicRoute` | Criar conta |
| `/catalogo` | `ProtectedRoute` | Lista de leilões |
| `/leiloes/:uuid` | `ProtectedRoute` | Leilão e seus lotes |
| `/leiloes/:uuid/pregao` | `ProtectedRoute` | Pregão ao vivo |
| `/leiloes/:uuid/recusas` | `ProtectedRoute` | Lances recusados |
| `/admin` | `ProtectedRoute` | Painel |
| `/admin/grupos` | `ProtectedRoute` | Grupos de permissão |
| `/admin/usuarios` | `ProtectedRoute` | Usuários e seus grupos |
| `/admin/leiloes/novo` | `ProtectedRoute` | Criar leilão |

O que depende de permissão some da tela quando o usuário não a tem — o cartão de adicionar lote, os comandos do leiloeiro, o link das recusas.

### Camadas

**`api/`** — `httpClient` centraliza base URL, cabeçalho de autorização e tratamento de erro; `401` limpa a sessão e devolve ao login. Os demais arquivos só descrevem as rotas. `canalDoPregao` cuida da conexão STOMP.

**`hooks/`** — `useAuth` é o contexto da sessão; `useLogin`, `useCadastro` e `useNovoLote` guardam estado de formulário e o envio; `useLeiloes` e `useLeilaoDetalhe` carregam o catálogo; `usePregao` junta o estado por REST com os eventos do canal; `useDarLance` envia lances e guarda a chave do último, para conseguir reenviar o mesmo.

**`components/`** — agrupados por área (`catalog/`, `pregao/`) mais os compartilhados na raiz.

### A tela do pregão

Lote em destaque com cronômetro, lance atual e próximo valor. Ao lado, o feed de lances mostrando aceitos e recusados na ordem em que foram julgados, a fila dos próximos lotes e os já encerrados com o desfecho.

Para quem tem `CRIAR_LEILAO` aparecem os comandos (abrir, bater o martelo, encerrar) e um painel **Reproduzir um caso**, com botões que disparam lance válido, abaixo do pedido, fora do incremento, valor absurdo e reenvio do último com a mesma chave. Serve para conferir a tela de recusas sem depender do simulador.

O estado completo vem por REST e o canal traz o que muda com a tela aberta. Lance aceito é aplicado na hora; troca de lote ou de status recarrega tudo, porque nesses casos muita coisa muda junto e remendar em pedaços sairia errado.

---

## Migrações

| Versão | O que fez |
|---|---|
| `V1__schema_inicial` | Tabelas de usuário, grupo, animal, leilão, lote e lance |
| `V2__lote_referencia_animal` | Lote passou a apontar para `animal` em vez de repetir os dados do cavalo em colunas próprias |
| `V3__uuid_publico` | `uuid` em `leilao`, `lote` e `animal`, com backfill e índice único |
| `V4__pregao_e_lances` | `uuid` e `motivo_recusa` em `lance`, `usuario_id` nulo para o lance da casa, janela do lote e marcos do leilão |

---

## Configuração

```properties
# Transporte do lance: "direto" avalia na requisição, "kafka" passa pelo broker
app.pregao.transporte=direto

# Quanto tempo cada lote fica aberto quando ninguém dá lance
app.pregao.segundos-por-lote=45

# Lance dentro desta janela final empurra o fechamento
app.pregao.soft-close-segundos=10

# Silêncio nesse tempo e a casa dá o próximo lance
app.pregao.segundos-sem-lance-ate-casa=8

# Múltiplo do valor inicial acima do qual o lance vira erro de digitação
app.pregao.fator-valor-suspeito=100

# Validade do token, em milissegundos (24h)
app.jwt.expiration-ms=86400000
```

O endereço do broker aceita a variável de ambiente `KAFKA_BROKERS`, e o do banco aceita `POSTGRES_HOST`.

---

# Estrutura do Projeto - Tela de Login

## Controller

### DefaultExceptionHandler

Centraliza o tratamento de erros da API, mapeando cada exceção para uma resposta padronizada com base no DTO de erro (`code`, `message`). Isso evita logs extensos no terminal e facilita o rastreamento futuro de ocorrências. Os principais casos tratados são:

**Erro inesperado** — Exceções não mapeadas retornam `500 Internal Server Error`.

**E-mail já cadastrado** — Impede duplicidade no banco de dados retornando uma resposta legível com o código `EMAIL_CADASTRADO` e a mensagem `"O e-mail já está cadastrado."`, no lugar do stack trace padrão gerado pelo Spring.

**Senha Vazia** - Impede que o usuário cadastre senha vazias dentro do banco de dados

**Senha fora do padrão** - Impede que o usuário cadastre senha fracas no banco, com o intuito de segurança. 
---

### UserController

Responsável pelo mapeamento dos endpoints da API. Atualmente expõe o cadastro de usuário, recebendo `e-mail` e `senha` via `UserDTO`. O endpoint de login ainda será implementado, aproveitando as validações já existentes no `DefaultExceptionHandler`.

> Por padrão o Spring retorna `200 OK` em criações — o retorno foi ajustado para `201 Created`.

---

## DTO

**ErrorDTO** — Representa o contrato de erro da API com os campos `code` e `message`.

**UserDTO** — Transporta os dados essenciais do usuário (`nome`, `e-mail` e `senha`) para o cadastro.

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

---

# Decisões

### Identificador público em UUID

O catálogo expunha o id sequencial do leilão na URL (`/leiloes/4`). Qualquer pessoa logada trocava o número e ia varrendo os leilões de um em um. Troquei por UUID em `leilao`, `lote` e `animal`: o `BIGINT` continua sendo a chave interna e as FKs, e o UUID é o que sai na API e na URL.

Fiz nos três porque esconder só o id do leilão não resolvia nada — o `LoteDTO` devolvia `id` e `animalId` sequenciais no mesmo JSON. O `usuario` ficou de fora por enquanto, porque mexer nele arrasta as telas de administração junto.

> `/api/leiloes/4` agora responde `400 PARAMETRO_INVALIDO` em vez de `500`. O Spring não consegue converter "4" em UUID, e o `DefaultExceptionHandler` passou a tratar `MethodArgumentTypeMismatchException`.

---

### Quem ganha quando dois lances chegam juntos

Foi a decisão que mais custou. Três caminhos possíveis:

**Maior valor vence** — junta os lances numa janela de alguns milissegundos e escolhe o maior. Parece mais justo, mas muda a semântica do leilão e o lance deixa de ser instantâneo.

**Timestamp do cliente** — descartado cedo. Relógio de cliente é manipulável e dessincronizado; viraria brecha de fraude.

**Primeiro a chegar** — o escolhido. É o que o leiloeiro faz na prática, pega a primeira mão levantada. E é o que o Kafka já entrega de graça quando a chave da mensagem é o uuid do lote.

---

### Kafka não dá idempotência

Entrei nessa achando que o Kafka resolveria lance duplicado. Não resolve. O que ele dá:

**Ordem por partição** — publicando com `key = loteUuid`, os lances de um mesmo lote caem sempre na mesma partição e são avaliados em fila, um de cada vez. Lotes diferentes seguem em paralelo.

**Replay** — todo lance julgado vira evento durável, e é disso que sai a tela de recusas.

A entrega do Kafka é *pelo menos uma vez*: um rebalanceamento de consumidor reentrega mensagem já processada. Deduplicar continua sendo problema da aplicação. A saída foi o `lance.uuid`, gerado por quem dá o lance, com índice único no banco — o avaliador vê o uuid repetido, não grava e devolve `DUPLICADO`.

Somei a isso uma trava pessimista (`SELECT ... FOR UPDATE`) no lote durante a avaliação. Parece redundante, já que a partição entrega um lance por vez, mas cobre o intervalo de rebalanceamento, quando dois consumidores podem se sobrepor por um instante, e cobre o modo sem broker.

---

### Lance recusado é linha na tabela de lance

Cheguei a pensar em tabela separada para os recusados. Não precisou: `EStatusLance` já tinha `INVALIDO`, e bastou somar a coluna `motivo_recusa`. A tela de problemas virou uma consulta em cima da mesma tabela, e o histórico de um lote fica inteiro num lugar só — o que entrou e o que tentou entrar.

---

### Lance da casa

O pedido era um comprador fantasma que cobre quando ninguém dá lance. Implementei como lance da casa em nome do vendedor, com teto no `valorReserva` do lote e etiqueta na tela.

Deixar visível foi decisão consciente. Casa cobrindo escondida e sem teto é shill bidding; com teto na reserva e identificação na tela, é a prática legítima. O campo `valorReserva` já existia no modelo, então não custou nada.

Se a casa for a última de pé, o lote fecha em `ENCERRADO` no valor dela, sem venda. A casa segura o preço, não compra o animal.

---

### Redpanda no lugar do Kafka

Fala a API do Kafka, então o código Java é o mesmo. Sobe em segundos, é um binário só e consome bem menos memória. Para máquina de desenvolvimento a diferença é grande.

---

### Resposta 202 no lance

O lance podia responder já com o veredito, esperando o consumidor julgar. Seria mais simples de consumir no frontend, mas amarraria a requisição HTTP ao tempo da fila e desfaria o motivo de ter fila.

Ficou assíncrono: `202` confirma que a tentativa entrou, e o desfecho chega pelo canal. A API se comporta igual nos dois modos de transporte, o que evita a tela funcionar em desenvolvimento e quebrar em produção.

---

# Onde travei

### A sessão voltava para o login

Sintoma: logado no catálogo, voltando para `/login` a tela de login abria normalmente. Deveria abrir só sem sessão, ou com a sessão vencida.

Eram dois problemas empilhados. O primeiro é que `/login` e `/cadastro` eram rotas soltas, sem guarda nenhuma — existia o `ProtectedRoute` barrando quem não tem sessão, mas não o contrário. Daí o `PublicRoute`, que manda para `/catalogo` quem já está logado.

O segundo era mais silencioso: `getSessao()` checava se existia token no storage, nunca se ele ainda valia. JWT vencido continuava passando como sessão boa até alguma requisição levar 401. Agora o `exp` é lido do próprio token, sessão vencida sai do storage, e o `useAuth` derruba a sessão no instante da expiração em vez de esperar a próxima requisição descobrir.

---

### A aplicação parou de subir depois de adicionar o Kafka

Coloquei `org.springframework.kafka:spring-kafka` no pom e o contexto quebrou:

```
Parameter 0 of constructor in PublicadorKafka required a bean of type
'org.springframework.kafka.core.KafkaTemplate' that could not be found.
```

No Spring Boot 3 bastava ter o spring-kafka no classpath que o `KafkaAutoConfiguration` cuidava do resto. No Boot 4 as auto-configurações foram separadas em módulos próprios, e a dependência crua não traz nenhuma. Resolvido trocando por `spring-boot-starter-kafka`, que puxa o `spring-boot-kafka` junto.

---

### Jackson 2 e Jackson 3 no mesmo classpath

O Boot 4 traz `com.fasterxml.jackson` 2.21 e `tools.jackson` 3.1.2 ao mesmo tempo, e o spring-kafka 4 tem duas famílias de serializer, `JsonSerializer` e `JacksonJsonSerializer`, uma para cada. Ficar adivinhando qual o Spring escolheria era pedir para quebrar depois.

Saí pelo caminho explícito: `KafkaTemplate<String, String>` com serializer de String e um `JsonMapper` próprio convertendo as mensagens. O formato do tópico deixou de depender da configuração da API e, como o que trafega é JSON puro sem cabeçalho de tipo, dá para ler as mensagens direto no console do broker.

---

### `ddl-auto=validate` não perdoa

Com `validate` mais Flyway, qualquer campo novo na entidade derruba a aplicação na inicialização se não existir a migração correspondente. Bateu duas vezes até virar reflexo: mexeu no `@Entity`, escreve a migração antes de rodar.

Não é chatice do Spring. É o que impede o Hibernate de alterar a estrutura do banco por conta própria, e o motivo de o schema ser rastreável pelo histórico do Flyway.

---

### `@Transactional` que não fazia nada

O relógio do pregão começou assim:

```java
@Scheduled(fixedDelay = 1000)
public void verificarCronometros() {
    for (Lote lote : lotesVencidos()) { ... }   // método da própria classe
}

@Transactional(readOnly = true)
public List<Lote> lotesVencidos() { ... }
```

A transação nunca abria. O `@Transactional` funciona por proxy, e chamada entre métodos da mesma classe não passa pelo proxy. Estava funcionando por sorte, porque os campos lidos eram todos básicos e nenhum lazy. Tirei a anotação em vez de deixar um enfeite que engana quem ler depois.

---

### O canal do WebSocket estava aberto

O `JwtAuthFilter` só protege o que começa com `/api/`. O handshake do WebSocket vai em `/ws`, passava direto, e qualquer um assinava o canal de qualquer leilão.

Corrigido com um `ChannelInterceptor` conferindo o token no CONNECT do STOMP, e o cliente mandando o `Authorization` nos `connectHeaders`.

---

### O agendador cuspindo stack trace

Com o broker fora do ar, `CompradorDaCasa` tentava publicar a cada segundo e cada tentativa jogava a exceção inteira no log. Em pouco tempo não dava para achar mais nada.

Passou a capturar a falha e registrar uma linha de aviso. O lance da casa deixa de sair, o que é o comportamento certo quando não há fila — mas o log continua legível.

---

### Desenvolver sem Docker

Não tenho Docker na máquina de desenvolvimento e o pregão depende do broker. Em vez de travar, a aplicação ganhou `app.pregao.transporte`: em `kafka` o lance passa pelo broker, em `direto` ele é avaliado dentro da própria requisição.

A trava do lote continua valendo nos dois modos, então o resultado é correto. O que se perde no modo direto é a ordenação por partição e o replay — serve para desenvolver, não para valer.

---

### Lote e lance não são a mesma coisa

Passei boa parte do tempo escrevendo "lote" querendo dizer "lance". Lote é o animal em oferta, lance é a oferta de dinheiro. Conversando passa batido, mas na hora de nomear classe e coluna atrapalha: `LoteRepository` guarda lotes, `LanceRepository` guarda lances, e `lote.proximoLanceApos(vencedor)` só se lê direito se os dois nomes estiverem certos.

---

# Pontos em aberto

**E-mail duplicado responde 500 em vez de 400.** O `catch` em `UserService.cadastrar` procura a mensagem `"Duplicate entry"`, que é a do MySQL. O PostgreSQL diz `duplicate key value violates unique constraint`, então a checagem não casa, a exceção é relançada e cai no handler genérico. O comportamento descrito na seção da tela de login vale para MySQL, não para o banco em uso.

**`usuario` ainda usa id sequencial.** `LoginResponseDTO` devolve o id, e as telas de administração trabalham com ele. Vale estender o UUID para lá, mas mexe em mais superfície do que as outras três tabelas.

**O feed ao vivo mostra o e-mail de quem dá lance.** Num leilão real os concorrentes costumam ser anônimos entre si. As telas de diagnóstico já ficaram restritas a quem tem `CRIAR_LEILAO`; o feed continua aberto a todos por decisão de simulação.

**O pregão não foi testado ponta a ponta com o broker.** Compila, o contexto Spring sobe inteiro com e sem Kafka, e a migração foi validada contra o banco — mas as requisições reais com o Redpanda no ar ainda não foram exercitadas.

**`rotuloStatus` não cobre todos os status.** O mapa em `utils/format.js` traduz `AGENDADO`, `EM_ANDAMENTO` e `ENCERRADO`. `RASCUNHO` e `CANCELADO` aparecem na tela com o nome cru do enum.

**Sem testes automatizados.** As dependências de teste estão no pom, mas não há nada escrito. O avaliador de lances é o candidato mais óbvio: é lógica pura sobre entidades, com uma dúzia de cenários bem definidos.

---

Detalhamento da arquitetura do pregão, com o desenho da fila e as instruções do simulador: [docs/pregao.md](docs/pregao.md).

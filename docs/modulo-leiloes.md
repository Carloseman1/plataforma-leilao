# Módulo de Leilões — decisões e fluxos

Documento da **Etapa 2** do plano (modelo de domínio). As etapas seguintes estão listadas no final.

---

## 1. A decisão central: separar Animal de Lote

Antes, `Lote` guardava os dados do animal direto nas suas colunas (`nomeAnimal`, `sexo`, `raca`, `pelagem`, `registro`). Isso amarrava o animal a um leilão específico.

O problema prático: o **Elfo do Porto Azul** não pertence ao *Leilão das Estrelas*. O que entra no leilão é uma **oferta** daquele animal — nesse caso uma cobertura. O mesmo animal pode voltar em outro leilão, com outro tipo de oferta e outro valor.

Modelo adotado:

```
Animal  ──┐
          ├──> Lote (a oferta) ──> Lance
Leilao  ──┘
```

| Entidade | Responsabilidade |
|---|---|
| `Animal` | Quem é o cavalo. Existe independente de leilão. |
| `Leilao` | O evento: datas, status, descrição. |
| `Lote` | A oferta de um animal em um leilão: número, tipo, valores. |
| `Lance` | Uma proposta de um usuário sobre um lote. |

Sem essa separação, cadastrar o mesmo animal em um segundo leilão significaria duplicar os dados dele — e o histórico do animal ficaria espalhado.

---

## 2. Genealogia: auto-relacionamento, não JSON

`Animal` aponta para outros `Animal`:

```java
@ManyToOne private Animal pai;
@ManyToOne private Animal mae;
```

Não criei uma entidade `Genealogia` separada nem guardei a árvore como JSON.

**Por quê:** o pai também é um animal. Se ele for uma entidade própria, a árvore é recursiva de graça — pai, avô, bisavô, sem limite de gerações — e dá para clicar em qualquer ancestral e abrir o cadastro dele. Com JSON, a árvore vira texto morto: não dá para consultar, nem relacionar, nem navegar.

O custo é que ancestrais sem cadastro completo entram como `Animal` só com o nome preenchido. Isso é aceitável: o registro pode ser completado depois sem migrar nada.

**Cuidado que precisou de atenção:** o projeto usa Lombok `@Data`, que gera `toString()` e `equals()` com todos os campos. Em um auto-relacionamento isso causa recursão infinita. Por isso `pai` e `mae` levam `@ToString.Exclude` e `@EqualsAndHashCode.Exclude`. O mesmo foi aplicado nas relações de `Lance` e `Lote`.

---

## 3. Valor de reserva nunca sai para o comprador

Requisito de segurança: o comprador não pode descobrir o valor mínimo de venda.

A proteção está no **DTO**, não no frontend. `LoteDTO` simplesmente não tem o campo `valorReserva` — logo não existe resposta da API pública que possa vazá-lo, mesmo por engano.

O que o comprador recebe:

```
valorInicial, incrementoMinimo, lanceAtual, proximoLance, status
```

Esconder no frontend não serviria: bastaria abrir o DevTools e ler a resposta da requisição.

---

## 4. `lanceAtual` é calculado, não armazenado

`Lote` não tem coluna `lanceAtual`. O valor vem de uma consulta ao maior lance válido:

```java
lanceRepository.findTopByLoteIdAndTipoAndStatusOrderByValorDesc(
        loteId, ETipoLance.REAL, EStatusLance.VALIDO)
```

**Por quê:** um campo `lanceAtual` guardado é uma cópia da verdade. Se um lance for cancelado, ou se dois lances chegarem juntos, o campo e a tabela de lances divergem — e aí não dá para saber qual está certo. Calculando, a tabela `lance` é sempre a fonte única.

Se o volume crescer a ponto de essa consulta pesar, dá para adicionar cache ou desnormalizar depois. Otimizar agora seria adivinhar.

---

## 5. Lance simulado é separado por tipo, não escondido

`Lance.tipo` é `REAL` ou `SIMULADO`.

O `lanceAtual` público filtra por `REAL`. Ou seja: **um lance simulado nunca aparece como se fosse um comprador real** e nunca pode definir o vencedor. O modo demonstração serve para o administrador testar e apresentar a tela — não para criar pressão artificial sobre quem está dando lance.

Isso é uma decisão deliberada de projeto, não um detalhe técnico: lance falso exibido como real para influenciar comprador é fraude em leilão.

---

## 6. Adaptações ao que já existia

Seguindo a regra de reaproveitar em vez de recriar:

| Situação | Decisão |
|---|---|
| `ELeilaoStatus` já tinha `EM_ANDAMENTO` | Mantido o nome e adicionados `RASCUNHO` e `CANCELADO`. O rótulo "Ao vivo" fica na interface. Renomear quebraria o frontend sem ganho. |
| `PermissaoService.exigir()` já existia | Reutilizado. Não criei um sistema de autorização novo. |
| `Button`, `FormField`, `FeedbackMessage` | Reutilizados. Nenhum componente novo de UI nesta etapa. |
| `CatalogoSeed` já existia | Reescrito com os 3 lotes de demonstração, mantendo o padrão `ApplicationRunner` + `@Order`. |

Campos renomeados no `LoteDTO` (e ajustados no frontend):

- `lanceInicial` → `valorInicial`
- `descricaoCurta` → `descricaoOferta`
- `idadeAnos` passou a ser **calculado** a partir de `Animal.dataNascimento`, em vez de digitado

Idade digitada fica errada sozinha com o tempo. Data de nascimento, não.

---

## 7. Fluxos

### Comprador

```
Catálogo de leilões
   ↓
Leilão das Estrelas
   ↓
Lotes do leilão          → vê lanceAtual e proximoLance
   ↓
Detalhe do lote          → vê animal + genealogia
   ↓
Dar lance                → valor >= proximoLance
```

### Administrador

```
Criar leilão (RASCUNHO)
   ↓
Adicionar lote
   ├── selecionar animal existente  (animalId)
   └── cadastrar animal novo        (animal: { ... })
   ↓
Definir valorInicial, incrementoMinimo, valorReserva
   ↓
Agendar / iniciar
```

O `LoteRequest` aceita **ou** `animalId` **ou** um objeto `animal`. Isso cobre os dois casos sem criar dois endpoints.

### Encerramento do lote

```
maior lance REAL válido >= valorReserva   →  VENDIDO
maior lance REAL válido <  valorReserva   →  RESERVA_NAO_ATINGIDA
```

---

## 8. O que está pronto nesta etapa

- [x] Entidades `Animal`, `Lote`, `Lance` e enums de apoio
- [x] Genealogia recursiva via `pai` / `mae`
- [x] `LoteDTO` público sem `valorReserva`
- [x] `lanceAtual` / `proximoLance` calculados
- [x] Repositórios `AnimalRepository` e `LanceRepository`
- [x] Seed do *Leilão das Estrelas* com os 3 lotes e a genealogia dos lotes 01 e 02
- [x] Backend e frontend compilando

## 9. O que NÃO está pronto

- [ ] **Executar e testar** — bloqueado pelo banco (ver seção 10)
- [ ] `LanceService` com as validações de lance
- [ ] Endpoints de animal, lote e lance
- [ ] Telas do catálogo e do detalhe do animal (Etapa 3 e 4)
- [ ] Tela de genealogia (Etapa 5)
- [ ] Upload e leitura do certificado (Etapa 7)
- [ ] Modo demonstração na interface (Etapa 9)
- [ ] Matriz de permissões por módulo (Etapa 10)
- [ ] Testes automatizados (Etapa 11)

As imagens referenciadas em `Animal.imagemPrincipal` (`/animais/*.png`) ainda **não existem** em `frontend/public`. São caminhos preparados, não arquivos.

---

## 10. Por que adotamos Flyway (e o bug que forçou isso)

Na primeira execução com o modelo novo, o catálogo quebrou com 500. Causa:

`ddl-auto=update` conseguiu adicionar `descricao_oferta` e `valor_reserva` (aceitam nulo), mas **não** conseguiu adicionar `animal_id`, `valor_inicial`, `incremento_minimo`, `tipo_oferta` e `status` — todas `NOT NULL`. A tabela `lote` já tinha 6 linhas do seed antigo, e o PostgreSQL não permite adicionar coluna `NOT NULL` sem default numa tabela com dados.

O ponto perigoso: **o Hibernate falhou em silêncio**. `update` registra o erro no log e segue. A aplicação subiu "normal" e só quebrou na consulta, com a tabela meio antiga e meio nova.

É por isso que `ddl-auto=update` não serve assim que existem dados: ele decide sozinho o que consegue fazer e não avisa o que deixou de fazer.

### O que mudou

| Antes | Depois |
|---|---|
| `ddl-auto=update` | `ddl-auto=validate` |
| Hibernate alterava o schema | Flyway altera; o Hibernate só **confere** e recusa subir se divergir |

Migrations em `src/main/resources/db/migration`:

- `V1__schema_inicial.sql` — o schema como estava quando o Flyway entrou
- `V2__lote_referencia_animal.sql` — remove as colunas de animal do `lote`, adiciona `animal_id` e os valores da oferta, e derruba os `CHECK` de enum gerados pelo Hibernate

O V1 existe para que um banco novo possa ser construído do zero. No banco que já existia, `spring.flyway.baseline-on-migrate=true` marcou o V1 como aplicado sem executá-lo e rodou só o V2. Os dois caminhos chegam ao mesmo schema.

**Por que derrubar os `CHECK` de enum:** o Hibernate tinha gerado `leilao_status_check` aceitando só `AGENDADO`, `EM_ANDAMENTO` e `ENCERRADO`. Ao adicionar `RASCUNHO` e `CANCELADO` no enum, qualquer insert com os valores novos seria recusado pelo banco. Validar enum no banco significa uma migration a cada valor novo — a validação fica na aplicação.

**Detalhe do Spring Boot 4:** só `flyway-core` não ativa nada. No Boot 4 as autoconfigurações foram separadas em módulos, então é preciso `org.springframework.boot:spring-boot-flyway`.

### Estado após a migration

Preservados: os 2 grupos de permissão e o usuário admin. Os 3 leilões de demonstração antigos foram removidos — o `CatalogoSeed` recria o *Leilão das Estrelas* no próximo start.

## 11. Configuração do banco

O projeto foi migrado de MySQL para PostgreSQL (o MySQL Server não estava instalado; o PostgreSQL 18 já estava rodando na porta 5432).

Feito:

- `pom.xml`: driver trocado para `org.postgresql:postgresql`
- `application.properties`: URL, driver e usuário ajustados

Pendente, e **depende de você**:

1. A senha de `spring.datasource.password` está como `postgres` (chute). Precisa ser a senha real.
2. O banco precisa existir:

```sql
CREATE DATABASE "plataforma-leilao";
```

As aspas duplas são obrigatórias por causa do hífen no nome.

Enquanto isso não for resolvido, o `ddl-auto=update` não roda, as tabelas novas (`animal`, `lance`, e as colunas novas de `lote`) não são criadas, e nada foi verificado em execução — apenas em compilação.

> **Atenção na primeira execução:** a tabela `lote` muda de forma (perde as colunas do animal, ganha `animal_id NOT NULL`). Com `ddl-auto=update` o Hibernate **não** remove colunas antigas nem migra dados. Como o banco ainda vai ser criado do zero, isso não é problema agora — mas é exatamente o tipo de situação que justifica adotar Flyway antes de ter dados reais.

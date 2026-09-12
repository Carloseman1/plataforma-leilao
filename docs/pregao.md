# Pregão ao vivo

Como o lance sai do navegador e chega no banco:

```
navegador  →  POST /api/lotes/{uuid}/lances  →  tópico leilao.lances  →  AvaliadorDeLances  →  banco
                        (202 Aceito)              (chave = uuid do lote)          │
                                                                                  ↓
                                                          WebSocket  →  tela do pregão
```

A resposta do POST diz que o lance **entrou na fila**, não que foi aceito. Quem
conta o desfecho é o canal WebSocket, depois que o avaliador julgar.

## O que o Kafka resolve, e o que ele não resolve

**Resolve — ordem.** A chave de toda mensagem é o uuid do lote, então todos os
lances de um mesmo lote caem na mesma partição e são avaliados em fila, um de
cada vez, na ordem de chegada. É essa ordem que decide quem tem prioridade:
**quem chegou primeiro na partição ganha**. Dois lances de R$ 60.000 ao mesmo
tempo → o primeiro entra, o segundo é recusado por `VALOR_INSUFICIENTE`, porque
quando ele foi avaliado o próximo lance já era R$ 62.000.

**Resolve — replay.** Todo lance julgado vira evento em `leilao.lances-resultado`.

**Não resolve — idempotência.** A entrega do Kafka é *pelo menos uma vez*: um
rebalanceamento reentrega mensagens já processadas. Quem trata isso é o
`lance.uuid`, gerado por quem dá o lance, com índice único no banco. O avaliador
vê o uuid repetido, não grava de novo e devolve `DUPLICADO`.

Além disso, o lote é travado com `SELECT ... FOR UPDATE` durante a avaliação. A
partição já entrega um lance por vez; a trava cobre o resto — rebalanceamento,
reprocessamento e o modo direto, que não tem partição.

## Motivos de recusa

| Motivo | Quando acontece |
|---|---|
| `DUPLICADO` | Mesmo `lanceUuid` de novo — duplo clique, retry do cliente, reentrega do Kafka |
| `VALOR_INSUFICIENTE` | Alguém chegou antes e o preço subiu |
| `INCREMENTO_INVALIDO` | Valor não respeita o degrau do lote |
| `VALOR_SUSPEITO` | Acima de 100× o valor inicial — erro de digitação |
| `LOTE_NAO_ABERTO` | O pregão ainda não chegou nesse lote |
| `LOTE_ENCERRADO` | Lance depois do martelo |
| `LEILAO_FORA_DO_AR` | Leilão não iniciado, encerrado ou cancelado |
| `AUTO_LANCE` | O comprador já estava ganhando |
| `LOTE_NAO_ENCONTRADO` | Lote inexistente |
| `SEM_HABILITACAO` | Comprador não existe ou não pode dar lance |

Falha de infraestrutura é outra coisa: a mensagem que nem o retry processou vai
para `leilao.lances.DLT` e aparece separada na tela, porque ela não chegou a ser
julgada.

## Soft close

Um lance dentro dos últimos 10 segundos empurra o fechamento para 10 segundos
adiante. Sem isso, todo mundo espera o último instante e ganha quem tem a rede
mais rápida.

## Lance da casa

Passados 8 segundos sem lance, a casa cobre em nome do vendedor — até o
`valorReserva` do lote, e para ali. O lance sai pela mesma fila, com tipo
`SIMULADO`, passa pelas mesmas regras e aparece identificado como **lance da
casa** na tela.

Se ninguém cobrir e o último de pé for o lance da casa, o lote fecha em
`ENCERRADO` no valor da casa: **não foi vendido**. A casa segura o preço, não
compra o animal.

## Como rodar

**1. Broker** (precisa de Docker):

```bash
docker compose up -d
```

Console do broker em http://localhost:8081 — dá para ver os tópicos, as
partições e as mensagens, que são JSON puro.

**2. Backend e frontend**, como de costume. O pregão liga sozinho.

**3. Abrir o pregão**: entre no leilão, clique em *Entrar no pregão ao vivo* e
depois em *Abrir pregão*. Só quem tem `CRIAR_LEILAO` vê os controles.

**4. Simulador**:

```bash
pip install -r simulador/requirements.txt
python simulador/simular_pregao.py --leilao <uuid-do-leilao> --compradores 5
```

Variações:

```bash
# intercala lances inválidos de propósito
python simulador/simular_pregao.py --leilao <uuid> --caos

# todos dão o mesmo valor no mesmo instante: só um entra
python simulador/simular_pregao.py --leilao <uuid> --empate
```

O `--empate` é o que demonstra a regra de prioridade. Depois de rodar, a tela de
recusas mostra os perdedores com `VALOR_INSUFICIENTE`.

### Sem Docker

```properties
app.pregao.transporte=direto
```

O lance passa a ser avaliado dentro da própria requisição. A trava do lote
continua valendo, então o resultado é correto — o que se perde é a ordenação por
partição e o replay. Serve para desenvolver, não para valer.

## Ajustes

```properties
app.pregao.segundos-por-lote=45
app.pregao.soft-close-segundos=10
app.pregao.segundos-sem-lance-ate-casa=8
app.pregao.fator-valor-suspeito=100
```

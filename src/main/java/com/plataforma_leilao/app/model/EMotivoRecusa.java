package com.plataforma_leilao.app.model;

/**
 * Por que um lance não entrou. Cada valor aqui vira uma linha na tela de
 * lances recusados, então o nome precisa dizer o que aconteceu sem consulta.
 */
public enum EMotivoRecusa {

    /** Mesmo lanceUuid já processado — duplo clique, retry do cliente ou reentrega do Kafka. */
    DUPLICADO("Lance repetido", "O mesmo lance chegou mais de uma vez e só valeu a primeira."),

    /** Alguém chegou antes e o preço já subiu. É a disputa normal do pregão. */
    VALOR_INSUFICIENTE("Valor abaixo do próximo lance", "Outro comprador cobriu primeiro e o valor pedido mudou."),

    /** Valor não bate com o incremento combinado do lote. */
    INCREMENTO_INVALIDO("Fora do incremento", "O valor não respeita o incremento mínimo do lote."),

    /** Valor tão alto que provavelmente é erro de digitação. */
    VALOR_SUSPEITO("Valor fora da faixa", "O valor é alto demais para este lote e precisa de confirmação."),

    /** O pregão ainda não chegou neste lote. */
    LOTE_NAO_ABERTO("Lote ainda fechado", "O pregão ainda não abriu este lote."),

    /** Martelo já batido. */
    LOTE_ENCERRADO("Lote já encerrado", "O lance chegou depois do martelo."),

    /** Leilão não está em andamento. */
    LEILAO_FORA_DO_AR("Leilão não está no ar", "O leilão não começou, já encerrou ou foi cancelado."),

    /** O comprador já é o vencedor atual. */
    AUTO_LANCE("Cobrindo o próprio lance", "O comprador já estava ganhando o lote."),

    /** Lote não existe. */
    LOTE_NAO_ENCONTRADO("Lote inexistente", "O lance apontava para um lote que não existe."),

    /** Comprador não existe ou não pode dar lance. */
    SEM_HABILITACAO("Comprador sem habilitação", "O comprador não está habilitado a dar lances.");

    private final String rotulo;
    private final String explicacao;

    EMotivoRecusa(String rotulo, String explicacao) {
        this.rotulo = rotulo;
        this.explicacao = explicacao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getExplicacao() {
        return explicacao;
    }
}

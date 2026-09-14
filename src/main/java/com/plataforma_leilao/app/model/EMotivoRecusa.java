package com.plataforma_leilao.app.model;

public enum EMotivoRecusa {

    DUPLICADO("Lance repetido", "O mesmo lance chegou mais de uma vez e só valeu a primeira."),

    VALOR_INSUFICIENTE("Valor abaixo do próximo lance", "Outro comprador cobriu primeiro e o valor pedido mudou."),

    INCREMENTO_INVALIDO("Fora do incremento", "O valor não respeita o incremento mínimo do lote."),

    VALOR_SUSPEITO("Valor fora da faixa", "O valor é alto demais para este lote e precisa de confirmação."),

    LOTE_NAO_ABERTO("Lote ainda fechado", "O pregão ainda não abriu este lote."),

    LOTE_ENCERRADO("Lote já encerrado", "O lance chegou depois do martelo."),

    LEILAO_FORA_DO_AR("Leilão não está no ar", "O leilão não começou, já encerrou ou foi cancelado."),

    AUTO_LANCE("Cobrindo o próprio lance", "O comprador já estava ganhando o lote."),

    LOTE_NAO_ENCONTRADO("Lote inexistente", "O lance apontava para um lote que não existe."),

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

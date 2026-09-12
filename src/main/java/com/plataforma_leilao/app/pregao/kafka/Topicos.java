package com.plataforma_leilao.app.pregao.kafka;

/**
 * Tópicos do pregão.
 *
 * A chave de toda mensagem é o uuid do lote. É isso que faz os lances de um
 * mesmo lote caírem sempre na mesma partição e serem avaliados em fila; lotes
 * diferentes seguem em paralelo, em partições diferentes.
 */
public final class Topicos {

    /** Tentativas de lance, ainda sem julgamento. */
    public static final String LANCES = "leilao.lances";

    /** O que o pregão decidiu sobre cada tentativa. */
    public static final String RESULTADOS = "leilao.lances-resultado";

    /** Mensagens que o consumidor não conseguiu processar nem depois das tentativas. */
    public static final String FALHAS = "leilao.lances.DLT";

    /**
     * Mais partições do que lotes simultâneos esperados. Aumentar depois é
     * possível, mas redistribui as chaves e quebra a ordem durante a mudança.
     */
    public static final int PARTICOES = 6;

    private Topicos() {}
}

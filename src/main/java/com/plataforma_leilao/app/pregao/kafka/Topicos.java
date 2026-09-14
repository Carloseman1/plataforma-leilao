package com.plataforma_leilao.app.pregao.kafka;

public final class Topicos {

    public static final String LANCES = "leilao.lances";
    public static final String RESULTADOS = "leilao.lances-resultado";
    public static final String FALHAS = "leilao.lances.DLT";
    public static final int PARTICOES = 6;

    private Topicos() {}
}

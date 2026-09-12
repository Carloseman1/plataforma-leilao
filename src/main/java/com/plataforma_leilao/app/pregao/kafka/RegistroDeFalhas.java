package com.plataforma_leilao.app.pregao.kafka;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Últimas mensagens que morreram na DLT, guardadas em memória só para a tela
 * de problemas ter o que mostrar.
 *
 * O registro durável é o próprio tópico de falhas; isto aqui é uma janela sobre
 * ele, e some quando a aplicação reinicia.
 */
public final class RegistroDeFalhas {

    private static final int LIMITE = 50;
    private static final Deque<Falha> FALHAS = new ArrayDeque<>();

    private RegistroDeFalhas() {}

    public static synchronized void registrar(String mensagem) {
        FALHAS.addFirst(new Falha(mensagem, LocalDateTime.now()));
        while (FALHAS.size() > LIMITE) {
            FALHAS.removeLast();
        }
    }

    public static synchronized List<Falha> listar() {
        return List.copyOf(FALHAS);
    }

    public record Falha(String mensagem, LocalDateTime ocorridaEm) {}
}

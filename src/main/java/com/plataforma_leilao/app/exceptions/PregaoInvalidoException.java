package com.plataforma_leilao.app.exceptions;

/** Comando de pregão que não faz sentido no estado atual do leilão. */
public class PregaoInvalidoException extends RuntimeException {

    public PregaoInvalidoException(String motivo) {
        super(motivo);
    }
}

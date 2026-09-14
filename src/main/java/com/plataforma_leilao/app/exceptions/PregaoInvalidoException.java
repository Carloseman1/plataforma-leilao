package com.plataforma_leilao.app.exceptions;

public class PregaoInvalidoException extends RuntimeException {

    public PregaoInvalidoException(String motivo) {
        super(motivo);
    }
}

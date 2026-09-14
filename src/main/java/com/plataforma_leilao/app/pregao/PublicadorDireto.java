package com.plataforma_leilao.app.pregao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.pregao.transporte", havingValue = "direto")
public class PublicadorDireto implements PublicadorDeLances {

    private final ProcessadorDeLances processador;

    public PublicadorDireto(ProcessadorDeLances processador) {
        this.processador = processador;
    }

    @Override
    public void publicar(LanceCommand comando) {
        processador.processar(comando);
    }
}

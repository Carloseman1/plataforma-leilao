package com.plataforma_leilao.app.pregao;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Avalia o lance na própria requisição, sem broker.
 *
 * Serve para rodar a aplicação numa máquina sem Docker. A trava de linha do
 * lote continua valendo, então o resultado é correto — o que se perde é a
 * ordenação por partição e o histórico replayável do tópico.
 */
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

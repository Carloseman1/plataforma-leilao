package com.plataforma_leilao.app.pregao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Avalia o lance e conta o resultado para quem está assistindo.
 *
 * Fica entre o transporte e a regra de negócio: tanto o consumidor do Kafka
 * quanto o modo direto passam por aqui, então o que acontece depois de um lance
 * é o mesmo nos dois caminhos.
 */
@Service
public class ProcessadorDeLances {

    private static final Logger log = LoggerFactory.getLogger(ProcessadorDeLances.class);

    private final AvaliadorDeLances avaliador;
    private final PainelDoPregao painel;

    public ProcessadorDeLances(AvaliadorDeLances avaliador, PainelDoPregao painel) {
        this.avaliador = avaliador;
        this.painel = painel;
    }

    public LanceResultado processar(LanceCommand comando) {
        LanceResultado resultado = avaliador.avaliar(comando);

        if (resultado.aceito()) {
            log.info("Lance aceito: lote {} por {} em {}",
                    resultado.loteUuid(), resultado.comprador(), resultado.valorTentado());
        } else {
            log.info("Lance recusado ({}): lote {} por {} em {}",
                    resultado.motivo(), resultado.loteUuid(),
                    resultado.comprador(), resultado.valorTentado());
        }

        painel.anunciarLance(resultado);
        return resultado;
    }
}

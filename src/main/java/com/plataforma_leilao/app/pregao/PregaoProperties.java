package com.plataforma_leilao.app.pregao;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.pregao")
@Data
public class PregaoProperties {

    private static final Logger log = LoggerFactory.getLogger(PregaoProperties.class);

    /**
     * "kafka" passa os lances pelo broker; "direto" avalia na hora, dentro da
     * própria requisição. O modo direto existe para conseguir mexer na aplicação
     * sem subir o broker — ele perde a ordenação por partição e o replay.
     */
    private String transporte = "kafka";

    /** Quanto tempo cada lote fica aberto quando ninguém dá lance. */
    private int segundosPorLote = 45;

    /** Lance dentro desta janela final empurra o fechamento para frente (anti-sniping). */
    private int softCloseSegundos = 10;

    /** Silêncio nesse tempo e a casa dá o próximo lance, até o valor de reserva. */
    private int segundosSemLanceAteCasa = 8;

    /** Múltiplo do valor inicial acima do qual o lance é tratado como erro de digitação. */
    private int fatorValorSuspeito = 100;

    public boolean usaKafka() {
        return "kafka".equalsIgnoreCase(transporte);
    }

    /** Dizer o modo na subida evita procurar no log por que o lance não passou pela fila. */
    @PostConstruct
    void anunciarModo() {
        if (usaKafka()) {
            log.info("Pregão usando Kafka. O broker precisa estar no ar.");
        } else {
            log.info("Pregão em modo direto: o lance é avaliado na própria requisição, "
                    + "sem broker. Sem ordenação por partição e sem replay.");
        }
    }
}

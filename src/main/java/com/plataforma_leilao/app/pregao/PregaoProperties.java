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

    private String transporte = "kafka";

    private int segundosPorLote = 45;

    private int softCloseSegundos = 10;

    private int segundosSemLanceAteCasa = 8;

    private int fatorValorSuspeito = 100;

    public boolean usaKafka() {
        return "kafka".equalsIgnoreCase(transporte);
    }

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

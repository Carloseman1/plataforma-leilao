package com.plataforma_leilao.app.pregao.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.plataforma_leilao.app.exceptions.PregaoIndisponivelException;
import com.plataforma_leilao.app.pregao.LanceCommand;
import com.plataforma_leilao.app.pregao.PublicadorDeLances;

@Component
@ConditionalOnProperty(name = "app.pregao.transporte", havingValue = "kafka", matchIfMissing = true)
public class PublicadorKafka implements PublicadorDeLances {

    private static final Logger log = LoggerFactory.getLogger(PublicadorKafka.class);

    private final KafkaTemplate<String, String> kafka;
    private final MensagemJson json;

    public PublicadorKafka(KafkaTemplate<String, String> kafka, MensagemJson json) {
        this.kafka = kafka;
        this.json = json;
    }

    @Override
    public void publicar(LanceCommand comando) {
        String chave = comando.loteUuid().toString();

        try {
            kafka.send(Topicos.LANCES, chave, json.paraJson(comando));
        } catch (Exception ex) {
            log.error("Não foi possível publicar o lance {} no broker", comando.lanceUuid(), ex);
            throw new PregaoIndisponivelException();
        }
    }
}

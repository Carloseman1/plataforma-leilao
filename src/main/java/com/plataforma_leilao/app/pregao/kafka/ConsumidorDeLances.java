package com.plataforma_leilao.app.pregao.kafka;

import com.plataforma_leilao.app.pregao.LanceCommand;
import com.plataforma_leilao.app.pregao.LanceResultado;
import com.plataforma_leilao.app.pregao.ProcessadorDeLances;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.pregao.transporte", havingValue = "kafka", matchIfMissing = true)
public class ConsumidorDeLances {

    private static final Logger log = LoggerFactory.getLogger(ConsumidorDeLances.class);

    private final ProcessadorDeLances processador;
    private final KafkaTemplate<String, String> kafka;
    private final MensagemJson json;

    public ConsumidorDeLances(ProcessadorDeLances processador,
                              KafkaTemplate<String, String> kafka,
                              MensagemJson json) {
        this.processador = processador;
        this.kafka = kafka;
        this.json = json;
    }

    @KafkaListener(
            topics = Topicos.LANCES,
            groupId = "pregao",
            concurrency = "" + Topicos.PARTICOES
    )
    public void consumir(String mensagem) {
        LanceCommand comando = json.de(mensagem, LanceCommand.class);
        LanceResultado resultado = processador.processar(comando);

        kafka.send(Topicos.RESULTADOS, resultado.loteUuid().toString(), json.paraJson(resultado));
    }

    @KafkaListener(topics = Topicos.FALHAS, groupId = "pregao-falhas")
    public void consumirFalha(String mensagem) {
        log.error("Lance parou na DLT: {}", mensagem);
        RegistroDeFalhas.registrar(mensagem);
    }
}

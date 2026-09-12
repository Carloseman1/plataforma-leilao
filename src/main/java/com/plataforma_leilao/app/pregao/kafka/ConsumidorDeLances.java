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

/**
 * Tira os lances da fila e manda avaliar.
 *
 * Há um consumidor por partição, e cada partição concentra os lances de um
 * mesmo lote — por isso dois lances do mesmo lote nunca são avaliados ao mesmo
 * tempo, e quem chegou primeiro à partição é julgado primeiro. Essa é a regra
 * de prioridade do pregão.
 *
 * A entrega do Kafka é "pelo menos uma vez": a mesma mensagem pode voltar depois
 * de um rebalanceamento. Quem trata isso é o uuid do lance, no avaliador.
 */
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

        // O resultado vira evento também: é o que permite reconstruir o pregão
        // inteiro depois, sem depender do que a tela viu na hora.
        kafka.send(Topicos.RESULTADOS, resultado.loteUuid().toString(), json.paraJson(resultado));
    }

    /**
     * O que nem o retry deu conta de processar. Não é lance recusado — é falha
     * de infraestrutura, e aparece separado na tela de problemas.
     */
    @KafkaListener(topics = Topicos.FALHAS, groupId = "pregao-falhas")
    public void consumirFalha(String mensagem) {
        log.error("Lance parou na DLT: {}", mensagem);
        RegistroDeFalhas.registrar(mensagem);
    }
}

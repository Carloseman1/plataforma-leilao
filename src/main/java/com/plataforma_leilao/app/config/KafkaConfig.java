package com.plataforma_leilao.app.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import com.plataforma_leilao.app.pregao.kafka.Topicos;

@Configuration
@ConditionalOnProperty(name = "app.pregao.transporte", havingValue = "kafka", matchIfMissing = true)
public class KafkaConfig {

    @Bean
    NewTopic topicoDeLances() {
        return TopicBuilder.name(Topicos.LANCES)
                .partitions(Topicos.PARTICOES)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic topicoDeResultados() {
        return TopicBuilder.name(Topicos.RESULTADOS)
                .partitions(Topicos.PARTICOES)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic topicoDeFalhas() {
        return TopicBuilder.name(Topicos.FALHAS)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    DefaultErrorHandler tratadorDeErroDoPregao(KafkaTemplate<String, String> kafka) {
        DeadLetterPublishingRecoverer paraDLT = new DeadLetterPublishingRecoverer(
                kafka,
                (registro, excecao) -> new TopicPartition(Topicos.FALHAS, 0));

        return new DefaultErrorHandler(paraDLT, new FixedBackOff(500L, 3L));
    }
}

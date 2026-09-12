package com.plataforma_leilao.app.pregao;

/**
 * Por onde o lance entra na fila do pregão.
 *
 * Em produção é o Kafka. O modo direto existe para conseguir rodar a aplicação
 * numa máquina sem broker — ele avalia na hora e perde tanto a ordenação por
 * partição quanto o replay, então serve para desenvolver, não para valer.
 */
public interface PublicadorDeLances {

    void publicar(LanceCommand comando);
}

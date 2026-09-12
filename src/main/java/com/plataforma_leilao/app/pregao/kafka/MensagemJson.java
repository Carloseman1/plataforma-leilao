package com.plataforma_leilao.app.pregao.kafka;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converte as mensagens do pregão de e para JSON.
 *
 * O mapper é próprio, e não o da aplicação web, de propósito: o formato que
 * trafega no tópico não deve mudar junto com a configuração da API. Como o que
 * vai no tópico é JSON puro, sem cabeçalho de tipo, dá para ler as mensagens
 * direto no console do broker.
 */
@Component
public class MensagemJson {

    private final JsonMapper mapper = JsonMapper.builder().build();

    public String paraJson(Object mensagem) {
        return mapper.writeValueAsString(mensagem);
    }

    public <T> T de(String json, Class<T> tipo) {
        return mapper.readValue(json, tipo);
    }
}

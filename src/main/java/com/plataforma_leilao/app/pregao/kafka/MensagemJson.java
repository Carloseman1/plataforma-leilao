package com.plataforma_leilao.app.pregao.kafka;

import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

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

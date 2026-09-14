package com.plataforma_leilao.app.pregao;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DarLanceRequest {
    private UUID lanceUuid;
    private BigDecimal valor;

    public UUID lanceUuidOuNovo() {
        return lanceUuid != null ? lanceUuid : UUID.randomUUID();
    }
}

package com.plataforma_leilao.app.pregao;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * O lanceUuid é a chave de idempotência e deve vir de quem dá o lance: é ele que
 * permite reenviar a mesma tentativa sem risco de ela entrar duas vezes. Quando
 * não vem, o servidor gera um — aí cada reenvio conta como um lance novo.
 */
@Data
public class DarLanceRequest {
    private UUID lanceUuid;
    private BigDecimal valor;

    public UUID lanceUuidOuNovo() {
        return lanceUuid != null ? lanceUuid : UUID.randomUUID();
    }
}

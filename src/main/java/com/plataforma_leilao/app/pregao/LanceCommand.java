package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.ETipoLance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Uma tentativa de lance, do jeito que entra na fila.
 *
 * O lanceUuid vem de quem envia — é ele que permite reconhecer a mesma tentativa
 * chegando duas vezes. O loteUuid é a chave da mensagem no Kafka: lances do mesmo
 * lote caem sempre na mesma partição e por isso são avaliados um de cada vez,
 * na ordem em que chegaram.
 */
public record LanceCommand(
        UUID lanceUuid,
        UUID loteUuid,
        Long usuarioId,
        BigDecimal valor,
        ETipoLance tipo,
        LocalDateTime enviadoEm
) {

    public static LanceCommand deComprador(UUID lanceUuid, UUID loteUuid, Long usuarioId, BigDecimal valor) {
        return new LanceCommand(lanceUuid, loteUuid, usuarioId, valor, ETipoLance.REAL, LocalDateTime.now());
    }

    public static LanceCommand daCasa(UUID loteUuid, BigDecimal valor) {
        return new LanceCommand(UUID.randomUUID(), loteUuid, null, valor, ETipoLance.SIMULADO, LocalDateTime.now());
    }
}

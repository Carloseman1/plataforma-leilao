package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.ETipoLance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

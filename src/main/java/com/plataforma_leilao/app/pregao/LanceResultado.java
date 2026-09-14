package com.plataforma_leilao.app.pregao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.plataforma_leilao.app.model.EMotivoRecusa;

public record LanceResultado(
                UUID lanceUuid,
                UUID loteUuid,
                UUID leilaoUuid,
                boolean aceito,
                EMotivoRecusa motivo,
                String motivoRotulo,
                String motivoExplicacao,
                BigDecimal valorTentado,
                BigDecimal lanceAtual,
                BigDecimal proximoLance,
                String comprador,
                boolean daCasa,
                LocalDateTime decididoEm) {

        public static LanceResultado aceito(UUID lanceUuid, UUID loteUuid, UUID leilaoUuid,
                        BigDecimal valor, BigDecimal proximoLance,
                        String comprador, boolean daCasa) {
                return new LanceResultado(lanceUuid, loteUuid, leilaoUuid, true, null, null, null,
                                valor, valor, proximoLance, comprador, daCasa, LocalDateTime.now());
        }

        public static LanceResultado recusado(UUID lanceUuid, UUID loteUuid, UUID leilaoUuid,
                        EMotivoRecusa motivo, BigDecimal valorTentado,
                        BigDecimal lanceAtual, BigDecimal proximoLance,
                        String comprador, boolean daCasa) {
                return new LanceResultado(lanceUuid, loteUuid, leilaoUuid, false, motivo,
                                motivo.getRotulo(), motivo.getExplicacao(), valorTentado, lanceAtual,
                                proximoLance, comprador, daCasa, LocalDateTime.now());
        }
}

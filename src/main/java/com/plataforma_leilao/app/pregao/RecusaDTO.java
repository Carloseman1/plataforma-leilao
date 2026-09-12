package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.EMotivoRecusa;
import com.plataforma_leilao.app.model.Lance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Um lance que não entrou, do jeito que a tela de problemas mostra. */
public record RecusaDTO(
        UUID lanceUuid,
        Integer loteNumero,
        String nomeAnimal,
        String comprador,
        BigDecimal valor,
        EMotivoRecusa motivo,
        String rotulo,
        String explicacao,
        boolean daCasa,
        LocalDateTime criadoEm
) {

    public static RecusaDTO de(Lance lance) {
        EMotivoRecusa motivo = lance.getMotivoRecusa();

        return new RecusaDTO(
                lance.getUuid(),
                lance.getLote().getNumero(),
                lance.getLote().getAnimal().getNome(),
                lance.getUsuario() == null ? "Casa" : lance.getUsuario().getEmail(),
                lance.getValor(),
                motivo,
                motivo == null ? null : motivo.getRotulo(),
                motivo == null ? null : motivo.getExplicacao(),
                lance.ehDaCasa(),
                lance.getCriadoEm()
        );
    }
}

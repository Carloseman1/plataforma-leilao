package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.ETipoOferta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/** Um lote do jeito que a tela do pregão precisa vê-lo. */
public record LoteAoVivoDTO(
        UUID uuid,
        Integer numero,
        String nomeAnimal,
        String raca,
        String sexo,
        String registro,
        String imagemPrincipal,
        ETipoOferta tipoOferta,
        String descricaoOferta,
        ELoteStatus status,
        BigDecimal valorInicial,
        BigDecimal incrementoMinimo,
        BigDecimal lanceAtual,
        BigDecimal proximoLance,
        String comprador,
        boolean lanceDaCasa,
        LocalDateTime abertoEm,
        LocalDateTime fechaEm
) {}

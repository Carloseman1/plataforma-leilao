package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.ELeilaoStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Estado completo do pregão: serve para abrir a tela e para reconciliar depois de uma queda. */
public record PregaoDTO(
        UUID leilaoUuid,
        String titulo,
        ELeilaoStatus status,
        LocalDateTime iniciadoEm,
        LocalDateTime encerradoEm,
        LoteAoVivoDTO loteAtual,
        List<LoteAoVivoDTO> naFila,
        List<LoteAoVivoDTO> encerrados
) {}

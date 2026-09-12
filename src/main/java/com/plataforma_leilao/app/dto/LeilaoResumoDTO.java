package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.ELeilaoStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LeilaoResumoDTO {
    private UUID uuid;
    private String titulo;
    private String subtitulo;
    private String local;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private ELeilaoStatus status;
    private int qtdLotes;

    public LeilaoResumoDTO(UUID uuid, String titulo, String subtitulo, String local,
                           LocalDateTime dataInicio, LocalDateTime dataFim,
                           ELeilaoStatus status, int qtdLotes) {
        this.uuid = uuid;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.local = local;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
        this.qtdLotes = qtdLotes;
    }
}

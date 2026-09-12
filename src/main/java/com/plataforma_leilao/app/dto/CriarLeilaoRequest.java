package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.ELeilaoStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CriarLeilaoRequest {
    private String titulo;
    private String subtitulo;
    private String local;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private ELeilaoStatus status;
    private List<LoteRequest> lotes;
}

package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.ELeilaoStatus;
import lombok.Data;

import java.math.BigDecimal;
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

    @Data
    public static class LoteRequest {
        private Integer numero;
        private String nomeAnimal;
        private String sexo;
        private String raca;
        private Integer idadeAnos;
        private String pelagem;
        private String registro;
        private String descricaoCurta;
        private BigDecimal lanceInicial;
    }
}

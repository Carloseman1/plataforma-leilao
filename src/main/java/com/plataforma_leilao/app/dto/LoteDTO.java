package com.plataforma_leilao.app.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoteDTO {
    private Long id;
    private Integer numero;
    private String nomeAnimal;
    private String sexo;
    private String raca;
    private Integer idadeAnos;
    private String pelagem;
    private String registro;
    private String descricaoCurta;
    private BigDecimal lanceInicial;

    public LoteDTO(Long id, Integer numero, String nomeAnimal, String sexo, String raca,
                   Integer idadeAnos, String pelagem, String registro,
                   String descricaoCurta, BigDecimal lanceInicial) {
        this.id = id;
        this.numero = numero;
        this.nomeAnimal = nomeAnimal;
        this.sexo = sexo;
        this.raca = raca;
        this.idadeAnos = idadeAnos;
        this.pelagem = pelagem;
        this.registro = registro;
        this.descricaoCurta = descricaoCurta;
        this.lanceInicial = lanceInicial;
    }
}

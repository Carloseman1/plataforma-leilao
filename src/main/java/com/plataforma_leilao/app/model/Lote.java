package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "lote")
@Data
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leilao_id", nullable = false)
    private Leilao leilao;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false)
    private String nomeAnimal;

    private String sexo;

    private String raca;

    private Integer idadeAnos;

    private String pelagem;

    private String registro;

    @Column(length = 500)
    private String descricaoCurta;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lanceInicial;

    public Lote() {}

    public Lote(Integer numero, String nomeAnimal, String sexo, String raca,
                Integer idadeAnos, String pelagem, String registro,
                String descricaoCurta, BigDecimal lanceInicial) {
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

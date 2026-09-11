package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leilao")
@Data
public class Leilao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private String subtitulo;

    private String local;

    @Column(nullable = false)
    private LocalDateTime dataInicio;

    @Column(nullable = false)
    private LocalDateTime dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ELeilaoStatus status = ELeilaoStatus.AGENDADO;

    @OneToMany(mappedBy = "leilao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    private List<Lote> lotes = new ArrayList<>();

    public Leilao() {}

    public Leilao(String titulo, String subtitulo, String local,
                  LocalDateTime dataInicio, LocalDateTime dataFim, ELeilaoStatus status) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.local = local;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.status = status;
    }

    public void adicionarLote(Lote lote) {
        lotes.add(lote);
        lote.setLeilao(this);
    }
}

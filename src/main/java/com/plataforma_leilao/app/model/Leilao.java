package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leilao")
@Data
public class Leilao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador público, usado na API e nas URLs no lugar do id sequencial. */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

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

    /** Momento em que o leiloeiro abriu o pregão. */
    private LocalDateTime iniciadoEm;

    private LocalDateTime encerradoEm;

    public void adicionarLote(Lote lote) {
        lotes.add(lote);
        lote.setLeilao(this);
    }

    /** Próximo número livre, para quem adiciona um lote sem informar o número. */
    public int proximoNumeroDeLote() {
        return lotes.stream()
                .mapToInt(Lote::getNumero)
                .max()
                .orElse(0) + 1;
    }

    public boolean aceitaNovosLotes() {
        return status != ELeilaoStatus.ENCERRADO && status != ELeilaoStatus.CANCELADO;
    }

    public boolean estaNoAr() {
        return status == ELeilaoStatus.EM_ANDAMENTO;
    }
}

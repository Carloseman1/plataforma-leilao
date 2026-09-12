package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lote")
@Data
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador público, usado na API e nas URLs no lugar do id sequencial. */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leilao_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Leilao leilao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @Column(nullable = false)
    private Integer numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETipoOferta tipoOferta = ETipoOferta.VENDA_ANIMAL;

    @Column(length = 500)
    private String descricaoOferta;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorInicial;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal incrementoMinimo;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorReserva;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ELoteStatus status = ELoteStatus.DISPONIVEL;

    /** Momento em que o pregão abriu este lote. Nulo enquanto ele espera a vez. */
    private LocalDateTime abertoEm;

    /** Alvo do cronômetro. Um lance perto do fim empurra este horário para frente. */
    private LocalDateTime fechaEm;

    public Lote() {}

    public Lote(Integer numero, Animal animal, ETipoOferta tipoOferta, String descricaoOferta,
                BigDecimal valorInicial, BigDecimal incrementoMinimo, BigDecimal valorReserva) {
        this.numero = numero;
        this.animal = animal;
        this.tipoOferta = tipoOferta;
        this.descricaoOferta = descricaoOferta;
        this.valorInicial = valorInicial;
        this.incrementoMinimo = incrementoMinimo;
        this.valorReserva = valorReserva;
    }

    public boolean estaAberto() {
        return status == ELoteStatus.DISPONIVEL && abertoEm != null;
    }

    public boolean esperandoAVez() {
        return status == ELoteStatus.DISPONIVEL && abertoEm == null;
    }

    public boolean temReserva() {
        return valorReserva != null;
    }

    /** Quanto vale o próximo lance, dado quem está ganhando agora. */
    public BigDecimal proximoLanceApos(Lance vencedor) {
        if (vencedor == null) {
            return valorInicial;
        }
        return vencedor.getValor().add(incrementoMinimo);
    }
}

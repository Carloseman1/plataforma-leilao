package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "lance")
@Data
public class Lance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Chave de idempotência gerada por quem envia o lance. O índice único no
     * banco é o que garante que a mesma tentativa não entre duas vezes, mesmo
     * que o Kafka reentregue a mensagem.
     */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Lote lote;

    /** Nulo no lance da casa, que não tem comprador por trás. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User usuario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ETipoLance tipo = ETipoLance.REAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EStatusLance status = EStatusLance.VALIDO;

    /** Preenchido só quando o lance é recusado. */
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private EMotivoRecusa motivoRecusa;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Lance() {}

    public Lance(UUID uuid, Lote lote, User usuario, BigDecimal valor, ETipoLance tipo) {
        this.uuid = uuid;
        this.lote = lote;
        this.usuario = usuario;
        this.valor = valor;
        this.tipo = tipo;
    }

    public static Lance recusado(UUID uuid, Lote lote, User usuario, BigDecimal valor,
                                 ETipoLance tipo, EMotivoRecusa motivo) {
        Lance lance = new Lance(uuid, lote, usuario, valor, tipo);
        lance.status = EStatusLance.INVALIDO;
        lance.motivoRecusa = motivo;
        return lance;
    }

    public boolean foiAceito() {
        return status == EStatusLance.VALIDO;
    }

    public boolean ehDaCasa() {
        return tipo == ETipoLance.SIMULADO;
    }
}

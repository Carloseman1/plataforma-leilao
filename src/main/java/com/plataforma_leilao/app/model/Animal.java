package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "animal")
@Data
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador público, usado na API e nas URLs no lugar do id sequencial. */
    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private String nome;

    private String registro;

    private String raca;

    private String sexo;

    private LocalDate dataNascimento;

    private String pelagem;

    private String modalidadeMarcha;

    private String criador;

    private String proprietario;

    @Column(length = 1000)
    private String descricao;

    private String imagemPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pai_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Animal pai;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mae_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Animal mae;

    public Animal() {}

    public Animal(String nome, String registro, String raca, String sexo,
                  LocalDate dataNascimento, String pelagem, String modalidadeMarcha) {
        this.nome = nome;
        this.registro = registro;
        this.raca = raca;
        this.sexo = sexo;
        this.dataNascimento = dataNascimento;
        this.pelagem = pelagem;
        this.modalidadeMarcha = modalidadeMarcha;
    }
}

package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grupo_permissao")
@Data
public class GrupoPermissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "grupo_permissao_item", joinColumns = @JoinColumn(name = "grupo_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao")
    private Set<EPermissao> permissoes = new HashSet<>();

    public GrupoPermissao() {}

    public GrupoPermissao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public boolean temPermissao(EPermissao permissao) {
        return permissoes != null && permissoes.contains(permissao);
    }
}

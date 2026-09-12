package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EUserPermission permissao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grupo_id")
    private GrupoPermissao grupo;

    @Column(nullable = false)
    private boolean ativo = true;

    public User() {}

    public User(String email, String senha) {
        this.email = email;
        this.senha = senha;
        this.permissao = EUserPermission.USER;
    }

    public boolean isAdmin() {
        return permissao == EUserPermission.ADMIN;
    }

    public boolean temPermissao(EPermissao desejada) {
        if (isAdmin()) {
            return true;
        }
        return grupo != null && grupo.temPermissao(desejada);
    }
}

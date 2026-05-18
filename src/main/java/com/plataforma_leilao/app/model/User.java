package com.plataforma_leilao.app.model;

import jakarta.persistence.*;
import lombok.Builder;
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

    @Column(nullable = false)
    private boolean ativo = true;

    public User() {}

    public User(String email, String senha) {
        this.email = email;
        this.senha = senha;
        this.permissao = EUserPermission.USER;
    }
}
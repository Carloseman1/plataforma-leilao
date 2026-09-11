package com.plataforma_leilao.app.dto;

import lombok.Data;

@Data
public class UsuarioResumoDTO {
    private Long id;
    private String email;
    private String grupo;
    private boolean admin;
    private boolean ativo;

    public UsuarioResumoDTO(Long id, String email, String grupo, boolean admin, boolean ativo) {
        this.id = id;
        this.email = email;
        this.grupo = grupo;
        this.admin = admin;
        this.ativo = ativo;
    }
}

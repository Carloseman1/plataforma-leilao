package com.plataforma_leilao.app.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponseDTO {
    private Long id;
    private String email;
    private String token;
    private String grupo;
    private boolean admin;
    private List<String> permissoes;

    public LoginResponseDTO(
            Long id,
            String email,
            String token,
            String grupo,
            boolean admin,
            List<String> permissoes
    ) {
        this.id = id;
        this.email = email;
        this.token = token;
        this.grupo = grupo;
        this.admin = admin;
        this.permissoes = permissoes;
    }
}

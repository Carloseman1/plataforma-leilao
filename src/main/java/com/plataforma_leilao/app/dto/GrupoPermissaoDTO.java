package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.EPermissao;
import lombok.Data;

import java.util.Set;

@Data
public class GrupoPermissaoDTO {
    private Long id;
    private String nome;
    private String descricao;
    private Set<EPermissao> permissoes;

    public GrupoPermissaoDTO(Long id, String nome, String descricao, Set<EPermissao> permissoes) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.permissoes = permissoes;
    }
}

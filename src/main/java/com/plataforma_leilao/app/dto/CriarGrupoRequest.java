package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.EPermissao;
import lombok.Data;

import java.util.Set;

@Data
public class CriarGrupoRequest {
    private String nome;
    private String descricao;
    private Set<EPermissao> permissoes;
}

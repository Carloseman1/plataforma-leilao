package com.plataforma_leilao.app.dto;

import lombok.Data;

import java.time.LocalDate;

/** Dados de um animal novo, enviados junto com o lote que vai colocá-lo em leilão. */
@Data
public class AnimalRequest {
    private String nome;
    private String registro;
    private String raca;
    private String sexo;
    private LocalDate dataNascimento;
    private String pelagem;
    private String modalidadeMarcha;
    private String criador;
    private String proprietario;
    private String descricao;
    private String imagemPrincipal;
}

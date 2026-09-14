package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.ETipoOferta;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class LoteRequest {
    private Integer numero;
    private UUID animalUuid;
    private AnimalRequest animal;
    private ETipoOferta tipoOferta;
    private String descricaoOferta;
    private BigDecimal valorInicial;
    private BigDecimal incrementoMinimo;
    private BigDecimal valorReserva;
}

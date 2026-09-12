package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.ETipoOferta;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Um lote a ser criado. O animal pode ser um já cadastrado (animalUuid) ou um
 * novo (animal). Numero e incrementoMinimo são opcionais: o serviço completa
 * com o próximo número livre do leilão e com o incremento padrão.
 */
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

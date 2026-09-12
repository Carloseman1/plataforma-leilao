package com.plataforma_leilao.app.dto;

import com.plataforma_leilao.app.model.Animal;
import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.ETipoOferta;
import com.plataforma_leilao.app.model.Lote;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

@Data
public class LoteDTO {
    private UUID uuid;
    private Integer numero;
    private ETipoOferta tipoOferta;
    private String descricaoOferta;
    private BigDecimal valorInicial;
    private BigDecimal incrementoMinimo;
    private BigDecimal lanceAtual;
    private BigDecimal proximoLance;
    private ELoteStatus status;
    private UUID animalUuid;
    private String nomeAnimal;
    private String sexo;
    private String raca;
    private Integer idadeAnos;
    private String pelagem;
    private String registro;
    private String imagemPrincipal;

    public LoteDTO(Lote lote, BigDecimal lanceAtual) {
        Animal animal = lote.getAnimal();
        this.uuid = lote.getUuid();
        this.numero = lote.getNumero();
        this.tipoOferta = lote.getTipoOferta();
        this.descricaoOferta = lote.getDescricaoOferta();
        this.valorInicial = lote.getValorInicial();
        this.incrementoMinimo = lote.getIncrementoMinimo();
        this.lanceAtual = lanceAtual;
        this.proximoLance = proximoLance(lote, lanceAtual);
        this.status = lote.getStatus();
        this.animalUuid = animal.getUuid();
        this.nomeAnimal = animal.getNome();
        this.sexo = animal.getSexo();
        this.raca = animal.getRaca();
        this.idadeAnos = idadeEmAnos(animal.getDataNascimento());
        this.pelagem = animal.getPelagem();
        this.registro = animal.getRegistro();
        this.imagemPrincipal = animal.getImagemPrincipal();
    }

    private static BigDecimal proximoLance(Lote lote, BigDecimal lanceAtual) {
        if (lanceAtual == null) {
            return lote.getValorInicial();
        }
        return lanceAtual.add(lote.getIncrementoMinimo());
    }

    private static Integer idadeEmAnos(LocalDate nascimento) {
        if (nascimento == null) {
            return null;
        }
        return Period.between(nascimento, LocalDate.now()).getYears();
    }
}

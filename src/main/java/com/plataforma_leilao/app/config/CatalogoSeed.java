package com.plataforma_leilao.app.config;

import com.plataforma_leilao.app.model.*;
import com.plataforma_leilao.app.repository.AnimalRepository;
import com.plataforma_leilao.app.repository.LeilaoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Order(2)
public class CatalogoSeed implements ApplicationRunner {

    private final LeilaoRepository leilaoRepository;
    private final AnimalRepository animalRepository;

    public CatalogoSeed(LeilaoRepository leilaoRepository, AnimalRepository animalRepository) {
        this.leilaoRepository = leilaoRepository;
        this.animalRepository = animalRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (leilaoRepository.count() > 0) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();

        Leilao estrelas = new Leilao(
                "Leilão das Estrelas",
                "Uma seleção especial de exemplares Mangalarga Marchador, reunindo genética, tradição e qualidade.",
                "Haras Real — transmissão online",
                agora.minusHours(1),
                agora.plusDays(2),
                ELeilaoStatus.EM_ANDAMENTO
        );

        estrelas.adicionarLote(loteElfo());
        estrelas.adicionarLote(lotePerfeicao());
        estrelas.adicionarLote(loteMascarado());

        leilaoRepository.save(estrelas);
    }

    private Lote loteElfo() {
        Animal pai = salvarPai("Haity Caxambuense");
        Animal mae = salvarMae("Samira do Porto Azul");

        Animal elfo = new Animal(
                "Elfo do Porto Azul", "5/016989", "Mangalarga Marchador", "Macho",
                LocalDate.of(2001, 1, 19), null, "Marcha Picada"
        );
        elfo.setImagemPrincipal("/animais/elfo-do-porto-azul.png");
        elfo.setPai(pai);
        elfo.setMae(mae);
        animalRepository.save(elfo);

        return new Lote(
                1, elfo, ETipoOferta.COBERTURA,
                "Cobertura com garanhão de marcha picada e linhagem consagrada.",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), new BigDecimal("20000.00")
        );
    }

    private Lote lotePerfeicao() {
        Animal pai = salvarPai("Lótus da Catimba");
        Animal mae = salvarMae("Arábia do Quociente");

        Animal perfeicao = new Animal(
                "Perfeição da Terra Brava", "0184313", "Mangalarga Marchador", "Fêmea",
                LocalDate.of(2017, 10, 10), null, "Marcha Batida"
        );
        perfeicao.setDescricao("Campeã das Campeãs Nacional de Marcha Batida 2024.");
        perfeicao.setImagemPrincipal("/animais/perfeicao-da-terra-brava.png");
        perfeicao.setPai(pai);
        perfeicao.setMae(mae);
        animalRepository.save(perfeicao);

        return new Lote(
                2, perfeicao, ETipoOferta.VENDA_ANIMAL,
                "Matriz premiada, pronta para pista e criação.",
                new BigDecimal("60000.00"), new BigDecimal("2000.00"), new BigDecimal("90000.00")
        );
    }

    private Lote loteMascarado() {
        Animal mascarado = new Animal(
                "Mascarado Águas Formosa", "061771", "Mangalarga Marchador", "Macho",
                LocalDate.of(2018, 11, 24), "Pampa de Preta", "Marcha Batida"
        );
        mascarado.setCriador("Joaquim Roberto de Sá");
        mascarado.setProprietario("Haras Águas Formosa");
        mascarado.setImagemPrincipal("/animais/mascarado-aguas-formosa.png");
        animalRepository.save(mascarado);

        return new Lote(
                3, mascarado, ETipoOferta.VENDA_ANIMAL,
                "Animal de pelagem pampa, morfologia equilibrada.",
                new BigDecimal("35000.00"), new BigDecimal("1000.00"), new BigDecimal("55000.00")
        );
    }

    private Animal salvarPai(String nome) {
        return animalRepository.save(
                new Animal(nome, null, "Mangalarga Marchador", "Macho", null, null, null));
    }

    private Animal salvarMae(String nome) {
        return animalRepository.save(
                new Animal(nome, null, "Mangalarga Marchador", "Fêmea", null, null, null));
    }
}

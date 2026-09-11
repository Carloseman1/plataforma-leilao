package com.plataforma_leilao.app.config;

import com.plataforma_leilao.app.model.ELeilaoStatus;
import com.plataforma_leilao.app.model.Leilao;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.repository.LeilaoRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Popula leilões de demonstração só se o banco estiver vazio.
 */
@Component
@Order(2)
public class CatalogoSeed implements ApplicationRunner {

    private final LeilaoRepository leilaoRepository;

    public CatalogoSeed(LeilaoRepository leilaoRepository) {
        this.leilaoRepository = leilaoRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (leilaoRepository.count() > 0) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();

        Leilao primavera = new Leilao(
                "Leilão Primavera Mangalarga",
                "Seleção de reprodutores e matrizes com registro ABCCM",
                "Haras Real — Campinas, SP",
                agora.plusDays(12).withHour(20).withMinute(0).withSecond(0).withNano(0),
                agora.plusDays(12).withHour(23).withMinute(0).withSecond(0).withNano(0),
                ELeilaoStatus.AGENDADO
        );
        primavera.adicionarLote(new Lote(
                1, "Estrela do Vale", "Fêmea", "Mangalarga Marchador", 4,
                "Castanha", "ABCCM 48291",
                "Marcha de centro bem marcada, linhagem de pista.",
                new BigDecimal("18000.00")
        ));
        primavera.adicionarLote(new Lote(
                2, "Relâmpago da Serra", "Macho", "Mangalarga Marchador", 5,
                "Alazão", "ABCCM 47102",
                "Garanhão jovem, morfologia equilibrada.",
                new BigDecimal("25000.00")
        ));
        primavera.adicionarLote(new Lote(
                3, "Lua Clara", "Fêmea", "Mangalarga Marchador", 3,
                "Tordilha", "ABCCM 50118",
                "Matriz promissora, docilidade comprovada.",
                new BigDecimal("15000.00")
        ));

        Leilao qm = new Leilao(
                "Leilão Virtual Quarto de Milha",
                "Potros e animais de trabalho com pedigree documentado",
                "Online — transmissão Haras Real",
                agora.plusDays(3).withHour(19).withMinute(30).withSecond(0).withNano(0),
                agora.plusDays(3).withHour(22).withMinute(0).withSecond(0).withNano(0),
                ELeilaoStatus.AGENDADO
        );
        qm.adicionarLote(new Lote(
                1, "Thunder Bay", "Macho", "Quarto de Milha", 2,
                "Baio", "ABQM 902114",
                "Potro com aptidão para laço e tambor.",
                new BigDecimal("12000.00")
        ));
        qm.adicionarLote(new Lote(
                2, "Silver Dust", "Fêmea", "Quarto de Milha", 6,
                "Tordilha", "ABQM 887301",
                "Égua de pista, já premiada em provas regionais.",
                new BigDecimal("32000.00")
        ));

        Leilao encerrado = new Leilao(
                "Leilão de Outono — Criatório Santa Rita",
                "Lotes remanescentes e animais de lazer",
                "Haras Real — Campinas, SP",
                agora.minusDays(20).withHour(20).withMinute(0).withSecond(0).withNano(0),
                agora.minusDays(20).withHour(22).withMinute(30).withSecond(0).withNano(0),
                ELeilaoStatus.ENCERRADO
        );
        encerrado.adicionarLote(new Lote(
                1, "Ventania", "Macho", "Campolina", 7,
                "Preto", "ABCC 22041",
                "Animal de sela, temperamento estável.",
                new BigDecimal("9000.00")
        ));

        leilaoRepository.save(primavera);
        leilaoRepository.save(qm);
        leilaoRepository.save(encerrado);
    }
}

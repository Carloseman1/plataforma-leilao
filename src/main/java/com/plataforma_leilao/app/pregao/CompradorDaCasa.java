package com.plataforma_leilao.app.pregao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.EStatusLance;
import com.plataforma_leilao.app.model.Lance;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.repository.LoteRepository;

@Component
public class CompradorDaCasa {

    private static final Logger log = LoggerFactory.getLogger(CompradorDaCasa.class);

    private final LoteRepository loteRepository;
    private final LanceRepository lanceRepository;
    private final PublicadorDeLances publicador;
    private final PregaoProperties propriedades;

    public CompradorDaCasa(LoteRepository loteRepository,
            LanceRepository lanceRepository,
            PublicadorDeLances publicador,
            PregaoProperties propriedades) {
        this.loteRepository = loteRepository;
        this.lanceRepository = lanceRepository;
        this.publicador = publicador;
        this.propriedades = propriedades;
    }

    @Scheduled(fixedDelay = 1000)
    public void cobrirSilencio() {
        for (Lote lote : lotesEmSilencio()) {
            BigDecimal valor = quantoACasaDaria(lote);
            if (valor == null) {
                continue;
            }

            try {
                log.info("Casa cobre o lote {} em {}", lote.getNumero(), valor);
                publicador.publicar(LanceCommand.daCasa(lote.getUuid(), valor));
            } catch (RuntimeException ex) {
                log.warn("Casa não conseguiu publicar o lance do lote {}: {}",
                        lote.getNumero(), ex.getMessage());
            }
        }
    }

    private List<Lote> lotesEmSilencio() {
        LocalDateTime agora = LocalDateTime.now();

        return loteRepository.findByStatusAndAbertoEmNotNull(ELoteStatus.DISPONIVEL).stream()
                .filter(lote -> silencioSuficiente(lote, agora))
                .toList();
    }

    private boolean silencioSuficiente(Lote lote, LocalDateTime agora) {
        LocalDateTime ultimoMovimento = vencedorDe(lote)
                .map(Lance::getCriadoEm)
                .orElse(lote.getAbertoEm());

        if (ultimoMovimento == null) {
            return false;
        }

        long paradoHa = Duration.between(ultimoMovimento, agora).toSeconds();
        return paradoHa >= propriedades.getSegundosSemLanceAteCasa();
    }

    private BigDecimal quantoACasaDaria(Lote lote) {
        if (!lote.temReserva()) {
            return null;
        }

        Lance vencedor = vencedorDe(lote).orElse(null);

        if (vencedor != null && vencedor.ehDaCasa()) {
            return null;
        }

        BigDecimal proximo = lote.proximoLanceApos(vencedor);

        return proximo.compareTo(lote.getValorReserva()) <= 0 ? proximo : null;
    }

    private Optional<Lance> vencedorDe(Lote lote) {
        return lanceRepository.findTopByLoteIdAndStatusOrderByValorDesc(lote.getId(), EStatusLance.VALIDO);
    }
}

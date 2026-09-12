package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.repository.LoteRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** Bate o martelo sozinho quando o cronômetro do lote chega ao fim. */
@Component
public class RelogioDoPregao {

    private final LoteRepository loteRepository;
    private final PregaoService pregaoService;

    public RelogioDoPregao(LoteRepository loteRepository, PregaoService pregaoService) {
        this.loteRepository = loteRepository;
        this.pregaoService = pregaoService;
    }

    @Scheduled(fixedDelay = 1000)
    public void verificarCronometros() {
        for (Lote lote : lotesVencidos()) {
            pregaoService.baterMartelo(lote.getUuid());
        }
    }

    /**
     * A lista aqui é só um palpite do que precisa fechar; quem confirma é o
     * baterMartelo, já com a linha travada. Se o lote tiver recebido um lance
     * no meio do caminho, ele encontra o fechaEm adiado e não faz nada.
     */
    private List<Lote> lotesVencidos() {
        LocalDateTime agora = LocalDateTime.now();

        return loteRepository.findByStatusAndAbertoEmNotNull(ELoteStatus.DISPONIVEL).stream()
                .filter(lote -> lote.getFechaEm() != null && lote.getFechaEm().isBefore(agora))
                .toList();
    }
}

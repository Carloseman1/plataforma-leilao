package com.plataforma_leilao.app.pregao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.repository.LoteRepository;

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

    private List<Lote> lotesVencidos() {
        LocalDateTime agora = LocalDateTime.now();

        return loteRepository.findByStatusAndAbertoEmNotNull(ELoteStatus.DISPONIVEL).stream()
                .filter(lote -> lote.getFechaEm() != null && lote.getFechaEm().isBefore(agora))
                .toList();
    }
}

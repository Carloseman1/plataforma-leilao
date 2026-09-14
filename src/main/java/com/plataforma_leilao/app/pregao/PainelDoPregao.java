package com.plataforma_leilao.app.pregao;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PainelDoPregao {

    public static final String CANAL = "/topic/pregao/";

    private final SimpMessagingTemplate mensageiro;

    public PainelDoPregao(SimpMessagingTemplate mensageiro) {
        this.mensageiro = mensageiro;
    }

    public void anunciarLance(LanceResultado resultado) {
        if (resultado.leilaoUuid() == null) {
            return;
        }
        enviar(resultado.leilaoUuid(), new EventoDoPregao("LANCE", resultado, null));
    }

    public void anunciarLote(UUID leilaoUuid, LoteAoVivoDTO lote) {
        enviar(leilaoUuid, new EventoDoPregao("LOTE", null, lote));
    }

    public void anunciarPregao(UUID leilaoUuid) {
        enviar(leilaoUuid, new EventoDoPregao("PREGAO", null, null));
    }

    private void enviar(UUID leilaoUuid, EventoDoPregao evento) {
        mensageiro.convertAndSend(CANAL + leilaoUuid, evento);
    }

    public record EventoDoPregao(String tipo, LanceResultado lance, LoteAoVivoDTO lote) {}
}

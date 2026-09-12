package com.plataforma_leilao.app.pregao;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Empurra o que acontece no pregão para as telas abertas.
 *
 * Cada leilão tem seu canal, então quem está vendo um leilão não recebe o
 * barulho dos outros. A tela também consegue o estado inteiro por REST — o
 * canal serve para o que muda enquanto ela está aberta.
 */
@Component
public class PainelDoPregao {

    public static final String CANAL = "/topic/pregao/";

    private final SimpMessagingTemplate mensageiro;

    public PainelDoPregao(SimpMessagingTemplate mensageiro) {
        this.mensageiro = mensageiro;
    }

    /** Um lance foi julgado — aceito ou recusado, os dois interessam à tela. */
    public void anunciarLance(LanceResultado resultado) {
        if (resultado.leilaoUuid() == null) {
            return;
        }
        enviar(resultado.leilaoUuid(), new EventoDoPregao("LANCE", resultado, null));
    }

    /** O pregão trocou de lote, ou o lote mudou de estado. */
    public void anunciarLote(UUID leilaoUuid, LoteAoVivoDTO lote) {
        enviar(leilaoUuid, new EventoDoPregao("LOTE", null, lote));
    }

    /** Mudou o estado do leilão como um todo (abriu, encerrou). */
    public void anunciarPregao(UUID leilaoUuid) {
        enviar(leilaoUuid, new EventoDoPregao("PREGAO", null, null));
    }

    private void enviar(UUID leilaoUuid, EventoDoPregao evento) {
        mensageiro.convertAndSend(CANAL + leilaoUuid, evento);
    }

    /**
     * Envelope único do canal. A tela olha o tipo para saber qual campo ler —
     * é mais simples de tratar no React do que um canal por tipo de evento.
     */
    public record EventoDoPregao(String tipo, LanceResultado lance, LoteAoVivoDTO lote) {}
}

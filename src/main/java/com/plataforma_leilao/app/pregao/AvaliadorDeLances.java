package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.model.*;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.repository.LoteRepository;
import com.plataforma_leilao.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Decide se um lance entra ou não. É o único ponto do sistema que altera o valor
 * corrente de um lote, e avalia um lance de cada vez por lote.
 *
 * Todo lance avaliado vira linha na tabela — aceito com status VALIDO, recusado
 * com status INVALIDO e o motivo. É desse histórico que sai a tela de recusas.
 */
@Service
public class AvaliadorDeLances {

    private final LoteRepository loteRepository;
    private final LanceRepository lanceRepository;
    private final UserRepository userRepository;
    private final PregaoProperties propriedades;

    public AvaliadorDeLances(LoteRepository loteRepository,
                             LanceRepository lanceRepository,
                             UserRepository userRepository,
                             PregaoProperties propriedades) {
        this.loteRepository = loteRepository;
        this.lanceRepository = lanceRepository;
        this.userRepository = userRepository;
        this.propriedades = propriedades;
    }

    @Transactional
    public LanceResultado avaliar(LanceCommand comando) {
        Optional<Lance> jaVisto = lanceRepository.findByUuid(comando.lanceUuid());
        if (jaVisto.isPresent()) {
            return resultadoRepetido(jaVisto.get());
        }

        Optional<Lote> encontrado = loteRepository.travarPorUuid(comando.loteUuid());
        if (encontrado.isEmpty()) {
            // Sem lote não há onde gravar o histórico; sobra o resultado avulso.
            return LanceResultado.recusado(comando.lanceUuid(), comando.loteUuid(), null,
                    EMotivoRecusa.LOTE_NAO_ENCONTRADO, comando.valor(), null, null,
                    "—", comando.tipo() == ETipoLance.SIMULADO);
        }

        Lote lote = encontrado.get();
        User comprador = comando.usuarioId() == null
                ? null
                : userRepository.findById(comando.usuarioId()).orElse(null);

        if (comando.usuarioId() != null && comprador == null) {
            return recusar(comando, lote, null, EMotivoRecusa.SEM_HABILITACAO);
        }

        EMotivoRecusa impedimento = procurarImpedimento(comando, lote, comprador);
        if (impedimento != null) {
            return recusar(comando, lote, comprador, impedimento);
        }

        return aceitar(comando, lote, comprador);
    }

    /**
     * Checagens em ordem de precedência: primeiro se o pregão está de pé, depois
     * quem está dando o lance, e só então o valor.
     */
    private EMotivoRecusa procurarImpedimento(LanceCommand comando, Lote lote, User comprador) {
        if (!lote.getLeilao().estaNoAr()) {
            return EMotivoRecusa.LEILAO_FORA_DO_AR;
        }
        if (lote.getStatus() != ELoteStatus.DISPONIVEL) {
            return EMotivoRecusa.LOTE_ENCERRADO;
        }
        if (lote.getAbertoEm() == null) {
            return EMotivoRecusa.LOTE_NAO_ABERTO;
        }

        Lance vencedor = vencedorAtual(lote).orElse(null);

        if (comprador != null && vencedor != null && comprador.equals(vencedor.getUsuario())) {
            return EMotivoRecusa.AUTO_LANCE;
        }

        BigDecimal proximo = lote.proximoLanceApos(vencedor);

        if (comando.valor().compareTo(proximo) < 0) {
            return EMotivoRecusa.VALOR_INSUFICIENTE;
        }
        if (!respeitaIncremento(comando.valor(), proximo, lote.getIncrementoMinimo())) {
            return EMotivoRecusa.INCREMENTO_INVALIDO;
        }
        if (comando.valor().compareTo(tetoDeSanidade(lote)) > 0) {
            return EMotivoRecusa.VALOR_SUSPEITO;
        }

        return null;
    }

    private LanceResultado aceitar(LanceCommand comando, Lote lote, User comprador) {
        Lance lance = new Lance(comando.lanceUuid(), lote, comprador, comando.valor(), comando.tipo());
        lanceRepository.save(lance);

        adiarFechamentoSePerto(lote);

        return LanceResultado.aceito(
                comando.lanceUuid(),
                lote.getUuid(),
                lote.getLeilao().getUuid(),
                comando.valor(),
                comando.valor().add(lote.getIncrementoMinimo()),
                nomeDoComprador(comprador),
                lance.ehDaCasa()
        );
    }

    private LanceResultado recusar(LanceCommand comando, Lote lote, User comprador, EMotivoRecusa motivo) {
        Lance lance = Lance.recusado(
                comando.lanceUuid(), lote, comprador, comando.valor(), comando.tipo(), motivo);
        lanceRepository.save(lance);

        Lance vencedor = vencedorAtual(lote).orElse(null);

        return LanceResultado.recusado(
                comando.lanceUuid(),
                lote.getUuid(),
                lote.getLeilao().getUuid(),
                motivo,
                comando.valor(),
                vencedor == null ? null : vencedor.getValor(),
                lote.proximoLanceApos(vencedor),
                nomeDoComprador(comprador),
                lance.ehDaCasa()
        );
    }

    /**
     * Um lance que chega no fim do cronômetro empurra o fechamento. Sem isso,
     * todo mundo espera o último segundo e ganha quem tem a rede mais rápida.
     */
    private void adiarFechamentoSePerto(Lote lote) {
        if (lote.getFechaEm() == null) {
            return;
        }

        LocalDateTime limite = LocalDateTime.now().plusSeconds(propriedades.getSoftCloseSegundos());
        if (lote.getFechaEm().isBefore(limite)) {
            lote.setFechaEm(limite);
        }
    }

    /** Resposta para a tentativa repetida: conta o que aconteceu da primeira vez. */
    private LanceResultado resultadoRepetido(Lance original) {
        Lote lote = original.getLote();
        Lance vencedor = vencedorAtual(lote).orElse(null);

        return LanceResultado.recusado(
                original.getUuid(),
                lote.getUuid(),
                lote.getLeilao().getUuid(),
                EMotivoRecusa.DUPLICADO,
                original.getValor(),
                vencedor == null ? null : vencedor.getValor(),
                lote.proximoLanceApos(vencedor),
                nomeDoComprador(original.getUsuario()),
                original.ehDaCasa()
        );
    }

    private Optional<Lance> vencedorAtual(Lote lote) {
        return lanceRepository.findTopByLoteIdAndStatusOrderByValorDesc(lote.getId(), EStatusLance.VALIDO);
    }

    /** O que passa do próximo lance tem que ser múltiplo do incremento combinado. */
    private boolean respeitaIncremento(BigDecimal valor, BigDecimal proximo, BigDecimal incremento) {
        if (incremento.signum() <= 0) {
            return true;
        }
        return valor.subtract(proximo).remainder(incremento).signum() == 0;
    }

    private BigDecimal tetoDeSanidade(Lote lote) {
        return lote.getValorInicial().multiply(BigDecimal.valueOf(propriedades.getFatorValorSuspeito()));
    }

    private String nomeDoComprador(User comprador) {
        return comprador == null ? "Casa" : comprador.getEmail();
    }
}

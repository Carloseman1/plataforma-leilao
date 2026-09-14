package com.plataforma_leilao.app.pregao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.EMotivoRecusa;
import com.plataforma_leilao.app.model.EStatusLance;
import com.plataforma_leilao.app.model.ETipoLance;
import com.plataforma_leilao.app.model.Lance;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.repository.LoteRepository;
import com.plataforma_leilao.app.repository.UserRepository;

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
                lance.ehDaCasa());
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
                lance.ehDaCasa());
    }

    private void adiarFechamentoSePerto(Lote lote) {
        if (lote.getFechaEm() == null) {
            return;
        }

        LocalDateTime limite = LocalDateTime.now().plusSeconds(propriedades.getSoftCloseSegundos());
        if (lote.getFechaEm().isBefore(limite)) {
            lote.setFechaEm(limite);
        }
    }

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
                original.ehDaCasa());
    }

    private Optional<Lance> vencedorAtual(Lote lote) {
        return lanceRepository.findTopByLoteIdAndStatusOrderByValorDesc(lote.getId(), EStatusLance.VALIDO);
    }

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

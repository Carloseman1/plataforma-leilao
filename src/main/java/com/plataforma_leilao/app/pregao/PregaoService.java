package com.plataforma_leilao.app.pregao;

import com.plataforma_leilao.app.exceptions.LeilaoNaoEncontradoException;
import com.plataforma_leilao.app.exceptions.PregaoInvalidoException;
import com.plataforma_leilao.app.model.*;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.repository.LeilaoRepository;
import com.plataforma_leilao.app.repository.LoteRepository;
import com.plataforma_leilao.app.service.PermissaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * O leiloeiro: abre o pregão, passa de lote em lote e bate o martelo.
 *
 * Os lances não passam por aqui — quem decide lance é o {@link AvaliadorDeLances},
 * do outro lado da fila. Esta classe cuida da moldura em volta deles.
 */
@Service
public class PregaoService {

    private static final Logger log = LoggerFactory.getLogger(PregaoService.class);

    private final LeilaoRepository leilaoRepository;
    private final LoteRepository loteRepository;
    private final LanceRepository lanceRepository;
    private final PermissaoService permissaoService;
    private final PainelDoPregao painel;
    private final PregaoProperties propriedades;

    public PregaoService(LeilaoRepository leilaoRepository,
                         LoteRepository loteRepository,
                         LanceRepository lanceRepository,
                         PermissaoService permissaoService,
                         PainelDoPregao painel,
                         PregaoProperties propriedades) {
        this.leilaoRepository = leilaoRepository;
        this.loteRepository = loteRepository;
        this.lanceRepository = lanceRepository;
        this.permissaoService = permissaoService;
        this.painel = painel;
        this.propriedades = propriedades;
    }

    // ---------------------------------------------------------------- comandos

    @Transactional
    public PregaoDTO iniciar(UUID uuidLeilao) {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);

        Leilao leilao = buscar(uuidLeilao);

        if (leilao.estaNoAr()) {
            throw new PregaoInvalidoException("Este pregão já está no ar.");
        }
        if (leilao.getStatus() == ELeilaoStatus.ENCERRADO) {
            throw new PregaoInvalidoException("Este leilão já foi encerrado.");
        }
        if (leilao.getLotes().isEmpty()) {
            throw new PregaoInvalidoException("Não dá para abrir um pregão sem lotes.");
        }

        leilao.setStatus(ELeilaoStatus.EM_ANDAMENTO);
        leilao.setIniciadoEm(LocalDateTime.now());
        abrirProximoLote(leilao);

        log.info("Pregão aberto: {}", leilao.getTitulo());
        painel.anunciarPregao(uuidLeilao);

        return montar(leilao);
    }

    /** O leiloeiro bate o martelo antes do cronômetro acabar. */
    @Transactional
    public PregaoDTO avancar(UUID uuidLeilao) {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);

        Leilao leilao = buscar(uuidLeilao);
        if (!leilao.estaNoAr()) {
            throw new PregaoInvalidoException("O pregão não está no ar.");
        }

        loteAberto(leilao).ifPresent(this::fechar);
        abrirProximoLote(leilao);
        encerrarSeAcabaramOsLotes(leilao);

        painel.anunciarPregao(uuidLeilao);
        return montar(leilao);
    }

    @Transactional
    public PregaoDTO encerrar(UUID uuidLeilao) {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);

        Leilao leilao = buscar(uuidLeilao);
        loteAberto(leilao).ifPresent(this::fechar);
        marcarEncerrado(leilao);

        painel.anunciarPregao(uuidLeilao);
        return montar(leilao);
    }

    /**
     * Fim do cronômetro. Chamado pelo relógio, sem usuário por trás, então não
     * passa por checagem de permissão.
     */
    @Transactional
    public void baterMartelo(UUID uuidLote) {
        Lote lote = loteRepository.travarPorUuid(uuidLote).orElse(null);
        if (lote == null || !lote.estaAberto()) {
            return;
        }

        fechar(lote);

        Leilao leilao = lote.getLeilao();
        abrirProximoLote(leilao);
        encerrarSeAcabaramOsLotes(leilao);

        painel.anunciarPregao(leilao.getUuid());
    }

    // ------------------------------------------------------------ mecânica

    private Optional<Lote> abrirProximoLote(Leilao leilao) {
        Optional<Lote> proximo = leilao.getLotes().stream()
                .filter(Lote::esperandoAVez)
                .min(Comparator.comparing(Lote::getNumero));

        proximo.ifPresent(lote -> {
            LocalDateTime agora = LocalDateTime.now();
            lote.setAbertoEm(agora);
            lote.setFechaEm(agora.plusSeconds(propriedades.getSegundosPorLote()));
            log.info("Lote {} aberto: {}", lote.getNumero(), lote.getAnimal().getNome());
            painel.anunciarLote(leilao.getUuid(), aoVivo(lote));
        });

        return proximo;
    }

    private void fechar(Lote lote) {
        Lance vencedor = vencedorDe(lote).orElse(null);

        lote.setStatus(desfechoDe(lote, vencedor));
        lote.setFechaEm(LocalDateTime.now());

        log.info("Martelo no lote {}: {}", lote.getNumero(), lote.getStatus());
        painel.anunciarLote(lote.getLeilao().getUuid(), aoVivo(lote));
    }

    /**
     * Como o lote termina.
     *
     * O lance da casa segura o preço até a reserva, mas não compra: se ele for o
     * último de pé, o lote fecha sem venda, no valor em que a casa parou.
     */
    private ELoteStatus desfechoDe(Lote lote, Lance vencedor) {
        if (vencedor == null) {
            return ELoteStatus.ENCERRADO;
        }
        if (vencedor.ehDaCasa()) {
            return ELoteStatus.ENCERRADO;
        }
        if (lote.temReserva() && vencedor.getValor().compareTo(lote.getValorReserva()) < 0) {
            return ELoteStatus.RESERVA_NAO_ATINGIDA;
        }
        return ELoteStatus.VENDIDO;
    }

    private void encerrarSeAcabaramOsLotes(Leilao leilao) {
        boolean aindaTemLote = leilao.getLotes().stream().anyMatch(Lote::esperandoAVez)
                || loteAberto(leilao).isPresent();

        if (!aindaTemLote) {
            marcarEncerrado(leilao);
        }
    }

    private void marcarEncerrado(Leilao leilao) {
        leilao.setStatus(ELeilaoStatus.ENCERRADO);
        leilao.setEncerradoEm(LocalDateTime.now());
        log.info("Pregão encerrado: {}", leilao.getTitulo());
    }

    private Optional<Lote> loteAberto(Leilao leilao) {
        return leilao.getLotes().stream().filter(Lote::estaAberto).findFirst();
    }

    // ---------------------------------------------------------------- leitura

    @Transactional(readOnly = true)
    public PregaoDTO estado(UUID uuidLeilao) {
        return montar(buscar(uuidLeilao));
    }

    private PregaoDTO montar(Leilao leilao) {
        List<LoteAoVivoDTO> naFila = leilao.getLotes().stream()
                .filter(Lote::esperandoAVez)
                .sorted(Comparator.comparing(Lote::getNumero))
                .map(this::aoVivo)
                .toList();

        List<LoteAoVivoDTO> encerrados = leilao.getLotes().stream()
                .filter(lote -> lote.getStatus() != ELoteStatus.DISPONIVEL)
                .sorted(Comparator.comparing(Lote::getNumero))
                .map(this::aoVivo)
                .toList();

        return new PregaoDTO(
                leilao.getUuid(),
                leilao.getTitulo(),
                leilao.getStatus(),
                leilao.getIniciadoEm(),
                leilao.getEncerradoEm(),
                loteAberto(leilao).map(this::aoVivo).orElse(null),
                naFila,
                encerrados
        );
    }

    LoteAoVivoDTO aoVivo(Lote lote) {
        Lance vencedor = vencedorDe(lote).orElse(null);
        Animal animal = lote.getAnimal();

        return new LoteAoVivoDTO(
                lote.getUuid(),
                lote.getNumero(),
                animal.getNome(),
                animal.getRaca(),
                animal.getSexo(),
                animal.getRegistro(),
                animal.getImagemPrincipal(),
                lote.getTipoOferta(),
                lote.getDescricaoOferta(),
                lote.getStatus(),
                lote.getValorInicial(),
                lote.getIncrementoMinimo(),
                vencedor == null ? null : vencedor.getValor(),
                lote.proximoLanceApos(vencedor),
                vencedor == null ? null : nomeDe(vencedor),
                vencedor != null && vencedor.ehDaCasa(),
                lote.getAbertoEm(),
                lote.getFechaEm()
        );
    }

    private Optional<Lance> vencedorDe(Lote lote) {
        return lanceRepository.findTopByLoteIdAndStatusOrderByValorDesc(lote.getId(), EStatusLance.VALIDO);
    }

    private String nomeDe(Lance lance) {
        return lance.getUsuario() == null ? "Casa" : lance.getUsuario().getEmail();
    }

    private Leilao buscar(UUID uuid) {
        return leilaoRepository.findByUuid(uuid).orElseThrow(LeilaoNaoEncontradoException::new);
    }
}

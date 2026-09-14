package com.plataforma_leilao.app.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plataforma_leilao.app.dto.AnimalRequest;
import com.plataforma_leilao.app.dto.CriarLeilaoRequest;
import com.plataforma_leilao.app.dto.LeilaoDetalheDTO;
import com.plataforma_leilao.app.dto.LeilaoResumoDTO;
import com.plataforma_leilao.app.dto.LoteDTO;
import com.plataforma_leilao.app.dto.LoteRequest;
import com.plataforma_leilao.app.exceptions.LeilaoEncerradoException;
import com.plataforma_leilao.app.exceptions.LeilaoNaoEncontradoException;
import com.plataforma_leilao.app.model.Animal;
import com.plataforma_leilao.app.model.ELeilaoStatus;
import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.EStatusLance;
import com.plataforma_leilao.app.model.ETipoLance;
import com.plataforma_leilao.app.model.ETipoOferta;
import com.plataforma_leilao.app.model.Lance;
import com.plataforma_leilao.app.model.Leilao;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.repository.AnimalRepository;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.repository.LeilaoRepository;

@Service
public class LeilaoService {

        private static final BigDecimal INCREMENTO_PADRAO = new BigDecimal("500.00");

        private final LeilaoRepository leilaoRepository;
        private final AnimalRepository animalRepository;
        private final LanceRepository lanceRepository;
        private final PermissaoService permissaoService;

        public LeilaoService(LeilaoRepository leilaoRepository,
                        AnimalRepository animalRepository,
                        LanceRepository lanceRepository,
                        PermissaoService permissaoService) {
                this.leilaoRepository = leilaoRepository;
                this.animalRepository = animalRepository;
                this.lanceRepository = lanceRepository;
                this.permissaoService = permissaoService;
        }

        @Transactional(readOnly = true)
        public List<LeilaoResumoDTO> listar() {
                return leilaoRepository.findAll().stream()
                                .map(this::toResumo)
                                .toList();
        }

        @Transactional(readOnly = true)
        public LeilaoDetalheDTO buscarPorUuid(UUID uuid) {
                Leilao leilao = leilaoRepository.findByUuid(uuid)
                                .orElseThrow(LeilaoNaoEncontradoException::new);
                return toDetalhe(leilao);
        }

        @Transactional
        public LeilaoDetalheDTO criar(CriarLeilaoRequest request) {
                permissaoService.exigir(EPermissao.CRIAR_LEILAO);

                ELeilaoStatus status = request.getStatus() != null
                                ? request.getStatus()
                                : ELeilaoStatus.AGENDADO;

                Leilao leilao = new Leilao(
                                request.getTitulo(),
                                request.getSubtitulo(),
                                request.getLocal(),
                                request.getDataInicio(),
                                request.getDataFim(),
                                status);

                if (request.getLotes() != null) {
                        for (LoteRequest item : request.getLotes()) {
                                leilao.adicionarLote(montarLote(item, leilao));
                        }
                }

                return toDetalhe(leilaoRepository.save(leilao));
        }

        @Transactional
        public LoteDTO adicionarLote(UUID uuidLeilao, LoteRequest request) {
                permissaoService.exigir(EPermissao.CRIAR_LEILAO);

                Leilao leilao = leilaoRepository.findByUuid(uuidLeilao)
                                .orElseThrow(LeilaoNaoEncontradoException::new);

                if (!leilao.aceitaNovosLotes()) {
                        throw new LeilaoEncerradoException();
                }

                Lote lote = montarLote(request, leilao);
                leilao.adicionarLote(lote);
                leilaoRepository.save(leilao);

                return new LoteDTO(lote, null);
        }

        private Lote montarLote(LoteRequest item, Leilao leilao) {
                Animal animal = resolverAnimal(item);

                ETipoOferta tipo = item.getTipoOferta() != null
                                ? item.getTipoOferta()
                                : ETipoOferta.VENDA_ANIMAL;

                BigDecimal incremento = item.getIncrementoMinimo() != null
                                ? item.getIncrementoMinimo()
                                : INCREMENTO_PADRAO;

                Integer numero = item.getNumero() != null
                                ? item.getNumero()
                                : leilao.proximoNumeroDeLote();

                return new Lote(
                                numero,
                                animal,
                                tipo,
                                item.getDescricaoOferta(),
                                item.getValorInicial(),
                                incremento,
                                item.getValorReserva());
        }

        private Animal resolverAnimal(LoteRequest item) {
                if (item.getAnimalUuid() != null) {
                        return animalRepository.findByUuid(item.getAnimalUuid())
                                        .orElseThrow(LeilaoNaoEncontradoException::new);
                }

                AnimalRequest dados = item.getAnimal();
                Animal animal = new Animal(
                                dados.getNome(),
                                dados.getRegistro(),
                                dados.getRaca(),
                                dados.getSexo(),
                                dados.getDataNascimento(),
                                dados.getPelagem(),
                                dados.getModalidadeMarcha());
                animal.setCriador(dados.getCriador());
                animal.setProprietario(dados.getProprietario());
                animal.setDescricao(dados.getDescricao());
                animal.setImagemPrincipal(dados.getImagemPrincipal());
                return animalRepository.save(animal);
        }

        private LeilaoResumoDTO toResumo(Leilao leilao) {
                return new LeilaoResumoDTO(
                                leilao.getUuid(),
                                leilao.getTitulo(),
                                leilao.getSubtitulo(),
                                leilao.getLocal(),
                                leilao.getDataInicio(),
                                leilao.getDataFim(),
                                leilao.getStatus(),
                                leilao.getLotes() == null ? 0 : leilao.getLotes().size());
        }

        private LeilaoDetalheDTO toDetalhe(Leilao leilao) {
                List<LoteDTO> lotes = leilao.getLotes().stream()
                                .map(lote -> new LoteDTO(lote, lanceAtualReal(lote)))
                                .toList();

                return new LeilaoDetalheDTO(
                                leilao.getUuid(),
                                leilao.getTitulo(),
                                leilao.getSubtitulo(),
                                leilao.getLocal(),
                                leilao.getDataInicio(),
                                leilao.getDataFim(),
                                leilao.getStatus(),
                                lotes);
        }

        private BigDecimal lanceAtualReal(Lote lote) {
                if (lote.getId() == null) {
                        return null;
                }
                return lanceRepository
                                .findTopByLoteIdAndTipoAndStatusOrderByValorDesc(
                                                lote.getId(), ETipoLance.REAL, EStatusLance.VALIDO)
                                .map(Lance::getValor)
                                .orElse(null);
        }
}

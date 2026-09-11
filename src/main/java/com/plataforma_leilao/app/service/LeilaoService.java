package com.plataforma_leilao.app.service;

import com.plataforma_leilao.app.dto.CriarLeilaoRequest;
import com.plataforma_leilao.app.dto.LeilaoDetalheDTO;
import com.plataforma_leilao.app.dto.LeilaoResumoDTO;
import com.plataforma_leilao.app.dto.LoteDTO;
import com.plataforma_leilao.app.exceptions.LeilaoNaoEncontradoException;
import com.plataforma_leilao.app.model.ELeilaoStatus;
import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.Leilao;
import com.plataforma_leilao.app.model.Lote;
import com.plataforma_leilao.app.repository.LeilaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeilaoService {

    private final LeilaoRepository leilaoRepository;
    private final PermissaoService permissaoService;

    public LeilaoService(LeilaoRepository leilaoRepository, PermissaoService permissaoService) {
        this.leilaoRepository = leilaoRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public List<LeilaoResumoDTO> listar() {
        return leilaoRepository.findAll().stream()
                .map(this::toResumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeilaoDetalheDTO buscarPorId(Long id) {
        Leilao leilao = leilaoRepository.findById(id)
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
                status
        );

        if (request.getLotes() != null) {
            for (CriarLeilaoRequest.LoteRequest item : request.getLotes()) {
                Lote lote = new Lote(
                        item.getNumero(),
                        item.getNomeAnimal(),
                        item.getSexo(),
                        item.getRaca(),
                        item.getIdadeAnos(),
                        item.getPelagem(),
                        item.getRegistro(),
                        item.getDescricaoCurta(),
                        item.getLanceInicial()
                );
                leilao.adicionarLote(lote);
            }
        }

        return toDetalhe(leilaoRepository.save(leilao));
    }

    private LeilaoResumoDTO toResumo(Leilao leilao) {
        return new LeilaoResumoDTO(
                leilao.getId(),
                leilao.getTitulo(),
                leilao.getSubtitulo(),
                leilao.getLocal(),
                leilao.getDataInicio(),
                leilao.getDataFim(),
                leilao.getStatus(),
                leilao.getLotes() == null ? 0 : leilao.getLotes().size()
        );
    }

    private LeilaoDetalheDTO toDetalhe(Leilao leilao) {
        List<LoteDTO> lotes = leilao.getLotes().stream()
                .map(lote -> new LoteDTO(
                        lote.getId(),
                        lote.getNumero(),
                        lote.getNomeAnimal(),
                        lote.getSexo(),
                        lote.getRaca(),
                        lote.getIdadeAnos(),
                        lote.getPelagem(),
                        lote.getRegistro(),
                        lote.getDescricaoCurta(),
                        lote.getLanceInicial()
                ))
                .toList();

        return new LeilaoDetalheDTO(
                leilao.getId(),
                leilao.getTitulo(),
                leilao.getSubtitulo(),
                leilao.getLocal(),
                leilao.getDataInicio(),
                leilao.getDataFim(),
                leilao.getStatus(),
                lotes
        );
    }
}

package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.Lance;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.pregao.*;
import com.plataforma_leilao.app.pregao.kafka.RegistroDeFalhas;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.service.PermissaoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class PregaoController {

    private static final int RECUSAS_NA_TELA = 100;

    private final PregaoService pregaoService;
    private final PublicadorDeLances publicador;
    private final PermissaoService permissaoService;
    private final LanceRepository lanceRepository;

    public PregaoController(PregaoService pregaoService,
                            PublicadorDeLances publicador,
                            PermissaoService permissaoService,
                            LanceRepository lanceRepository) {
        this.pregaoService = pregaoService;
        this.publicador = publicador;
        this.permissaoService = permissaoService;
        this.lanceRepository = lanceRepository;
    }

    @GetMapping("/leiloes/{uuid}/pregao")
    public PregaoDTO estado(@PathVariable UUID uuid) {
        return pregaoService.estado(uuid);
    }

    @PostMapping("/leiloes/{uuid}/pregao/iniciar")
    public PregaoDTO iniciar(@PathVariable UUID uuid) {
        return pregaoService.iniciar(uuid);
    }

    @PostMapping("/leiloes/{uuid}/pregao/avancar")
    public PregaoDTO avancar(@PathVariable UUID uuid) {
        return pregaoService.avancar(uuid);
    }

    @PostMapping("/leiloes/{uuid}/pregao/encerrar")
    public PregaoDTO encerrar(@PathVariable UUID uuid) {
        return pregaoService.encerrar(uuid);
    }

    /**
     * Recebe o lance e devolve na hora: a resposta diz que a tentativa entrou na
     * fila, não que ela foi aceita. Quem conta o desfecho é o canal do pregão,
     * depois que o lance for avaliado.
     */
    @PostMapping("/lotes/{uuid}/lances")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> darLance(@PathVariable UUID uuid, @RequestBody DarLanceRequest request) {
        User comprador = permissaoService.usuarioAtual();
        UUID lanceUuid = request.lanceUuidOuNovo();

        publicador.publicar(LanceCommand.deComprador(lanceUuid, uuid, comprador.getId(), request.getValor()));

        return Map.of("lanceUuid", lanceUuid, "situacao", "NA_FILA");
    }

    /**
     * Lances que não entraram, com o motivo de cada um. É tela de diagnóstico e
     * mostra o e-mail de quem tentou, então fica restrita a quem toca o pregão.
     */
    @GetMapping("/leiloes/{uuid}/recusas")
    public List<RecusaDTO> recusas(@PathVariable UUID uuid) {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);

        List<Lance> recusados =
                lanceRepository.recusadosDoLeilao(uuid, PageRequest.of(0, RECUSAS_NA_TELA));

        return recusados.stream().map(RecusaDTO::de).toList();
    }

    /** Mensagens que o consumidor não conseguiu processar — falha de infra, não lance recusado. */
    @GetMapping("/pregao/falhas")
    public List<RegistroDeFalhas.Falha> falhas() {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);
        return RegistroDeFalhas.listar();
    }
}

package com.plataforma_leilao.app.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.Lance;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.pregao.DarLanceRequest;
import com.plataforma_leilao.app.pregao.LanceCommand;
import com.plataforma_leilao.app.pregao.PregaoDTO;
import com.plataforma_leilao.app.pregao.PregaoService;
import com.plataforma_leilao.app.pregao.PublicadorDeLances;
import com.plataforma_leilao.app.pregao.RecusaDTO;
import com.plataforma_leilao.app.pregao.kafka.RegistroDeFalhas;
import com.plataforma_leilao.app.repository.LanceRepository;
import com.plataforma_leilao.app.service.PermissaoService;

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

    @PostMapping("/leiloes/{uuid}/pregao/reabrir")
    public PregaoDTO reabrir(@PathVariable UUID uuid) {
        return pregaoService.reabrir(uuid);
    }

    @PostMapping("/lotes/{uuid}/lances")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> darLance(@PathVariable UUID uuid, @RequestBody DarLanceRequest request) {
        User comprador = permissaoService.usuarioAtual();
        UUID lanceUuid = request.lanceUuidOuNovo();

        publicador.publicar(LanceCommand.deComprador(lanceUuid, uuid, comprador.getId(), request.getValor()));

        return Map.of("lanceUuid", lanceUuid, "situacao", "NA_FILA");
    }

    @GetMapping("/leiloes/{uuid}/recusas")
    public List<RecusaDTO> recusas(@PathVariable UUID uuid) {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);

        List<Lance> recusados = lanceRepository.recusadosDoLeilao(uuid, PageRequest.of(0, RECUSAS_NA_TELA));

        return recusados.stream().map(RecusaDTO::de).toList();
    }

    @GetMapping("/pregao/falhas")
    public List<RegistroDeFalhas.Falha> falhas() {
        permissaoService.exigir(EPermissao.CRIAR_LEILAO);
        return RegistroDeFalhas.listar();
    }
}

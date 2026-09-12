package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.dto.CriarLeilaoRequest;
import com.plataforma_leilao.app.dto.LeilaoDetalheDTO;
import com.plataforma_leilao.app.dto.LeilaoResumoDTO;
import com.plataforma_leilao.app.dto.LoteDTO;
import com.plataforma_leilao.app.dto.LoteRequest;
import com.plataforma_leilao.app.service.LeilaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leiloes")
@CrossOrigin(origins = "http://localhost:5173")
public class LeilaoController {

    private final LeilaoService leilaoService;

    public LeilaoController(LeilaoService leilaoService) {
        this.leilaoService = leilaoService;
    }

    @GetMapping
    public List<LeilaoResumoDTO> listar() {
        return leilaoService.listar();
    }

    @GetMapping("/{uuid}")
    public LeilaoDetalheDTO buscar(@PathVariable UUID uuid) {
        return leilaoService.buscarPorUuid(uuid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeilaoDetalheDTO criar(@RequestBody CriarLeilaoRequest request) {
        return leilaoService.criar(request);
    }

    @PostMapping("/{uuid}/lotes")
    @ResponseStatus(HttpStatus.CREATED)
    public LoteDTO adicionarLote(@PathVariable UUID uuid, @RequestBody LoteRequest request) {
        return leilaoService.adicionarLote(uuid, request);
    }
}

package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.dto.CriarLeilaoRequest;
import com.plataforma_leilao.app.dto.LeilaoDetalheDTO;
import com.plataforma_leilao.app.dto.LeilaoResumoDTO;
import com.plataforma_leilao.app.service.LeilaoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}")
    public LeilaoDetalheDTO buscar(@PathVariable Long id) {
        return leilaoService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LeilaoDetalheDTO criar(@RequestBody CriarLeilaoRequest request) {
        return leilaoService.criar(request);
    }
}

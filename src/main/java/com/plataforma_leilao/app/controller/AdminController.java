package com.plataforma_leilao.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma_leilao.app.dto.AtribuirGrupoRequest;
import com.plataforma_leilao.app.dto.CriarGrupoRequest;
import com.plataforma_leilao.app.dto.GrupoPermissaoDTO;
import com.plataforma_leilao.app.dto.UsuarioResumoDTO;
import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/permissoes")
    public List<EPermissao> listarPermissoes() {
        return adminService.listarPermissoesDisponiveis();
    }

    @GetMapping("/grupos")
    public List<GrupoPermissaoDTO> listarGrupos() {
        return adminService.listarGrupos();
    }

    @PostMapping("/grupos")
    @ResponseStatus(HttpStatus.CREATED)
    public GrupoPermissaoDTO criarGrupo(@RequestBody CriarGrupoRequest request) {
        return adminService.criarGrupo(request);
    }

    @PutMapping("/grupos/{id}")
    public GrupoPermissaoDTO atualizarGrupo(
            @PathVariable Long id,
            @RequestBody CriarGrupoRequest request
    ) {
        return adminService.atualizarGrupo(id, request);
    }

    @GetMapping("/usuarios")
    public List<UsuarioResumoDTO> listarUsuarios() {
        return adminService.listarUsuarios();
    }

    @PutMapping("/usuarios/{id}/grupo")
    public UsuarioResumoDTO atribuirGrupo(
            @PathVariable Long id,
            @RequestBody AtribuirGrupoRequest request
    ) {
        return adminService.atribuirGrupo(id, request.getGrupoId());
    }
}

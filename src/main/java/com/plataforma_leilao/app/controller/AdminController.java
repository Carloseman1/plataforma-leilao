package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.dto.AtribuirGrupoRequest;
import com.plataforma_leilao.app.dto.CriarGrupoRequest;
import com.plataforma_leilao.app.dto.GrupoPermissaoDTO;
import com.plataforma_leilao.app.dto.UsuarioResumoDTO;
import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

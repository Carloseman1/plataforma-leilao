package com.plataforma_leilao.app.service;

import com.plataforma_leilao.app.dto.CriarGrupoRequest;
import com.plataforma_leilao.app.dto.GrupoPermissaoDTO;
import com.plataforma_leilao.app.dto.UsuarioResumoDTO;
import com.plataforma_leilao.app.exceptions.GrupoNaoEncontradoException;
import com.plataforma_leilao.app.exceptions.UsuarioNaoEncontradoException;
import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.EUserPermission;
import com.plataforma_leilao.app.model.GrupoPermissao;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.repository.GrupoPermissaoRepository;
import com.plataforma_leilao.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class AdminService {

    private final GrupoPermissaoRepository grupoRepository;
    private final UserRepository userRepository;
    private final PermissaoService permissaoService;

    public AdminService(
            GrupoPermissaoRepository grupoRepository,
            UserRepository userRepository,
            PermissaoService permissaoService
    ) {
        this.grupoRepository = grupoRepository;
        this.userRepository = userRepository;
        this.permissaoService = permissaoService;
    }

    @Transactional(readOnly = true)
    public List<GrupoPermissaoDTO> listarGrupos() {
        permissaoService.exigir(EPermissao.GERENCIAR_GRUPOS);
        return grupoRepository.findAll().stream().map(this::toGrupoDto).toList();
    }

    @Transactional
    public GrupoPermissaoDTO criarGrupo(CriarGrupoRequest request) {
        permissaoService.exigir(EPermissao.GERENCIAR_GRUPOS);

        GrupoPermissao grupo = new GrupoPermissao(request.getNome(), request.getDescricao());
        if (request.getPermissoes() != null) {
            grupo.setPermissoes(new HashSet<>(request.getPermissoes()));
        }
        return toGrupoDto(grupoRepository.save(grupo));
    }

    @Transactional
    public GrupoPermissaoDTO atualizarGrupo(Long id, CriarGrupoRequest request) {
        permissaoService.exigir(EPermissao.GERENCIAR_GRUPOS);

        GrupoPermissao grupo = grupoRepository.findById(id)
                .orElseThrow(GrupoNaoEncontradoException::new);

        grupo.setNome(request.getNome());
        grupo.setDescricao(request.getDescricao());
        grupo.setPermissoes(
                request.getPermissoes() == null
                        ? new HashSet<>()
                        : new HashSet<>(request.getPermissoes())
        );

        return toGrupoDto(grupoRepository.save(grupo));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumoDTO> listarUsuarios() {
        permissaoService.exigir(EPermissao.GERENCIAR_USUARIOS);
        return userRepository.findAll().stream().map(this::toUsuarioDto).toList();
    }

    @Transactional
    public UsuarioResumoDTO atribuirGrupo(Long userId, Long grupoId) {
        permissaoService.exigir(EPermissao.GERENCIAR_USUARIOS);

        User user = userRepository.findById(userId)
                .orElseThrow(UsuarioNaoEncontradoException::new);
        GrupoPermissao grupo = grupoRepository.findById(grupoId)
                .orElseThrow(GrupoNaoEncontradoException::new);

        user.setGrupo(grupo);

        // Mantém o papel ADMIN só para quem já é admin do sistema
        if (!user.isAdmin()) {
            user.setPermissao(EUserPermission.USER);
        }

        return toUsuarioDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<EPermissao> listarPermissoesDisponiveis() {
        permissaoService.exigir(EPermissao.GERENCIAR_GRUPOS);
        return List.of(EPermissao.values());
    }

    private GrupoPermissaoDTO toGrupoDto(GrupoPermissao grupo) {
        return new GrupoPermissaoDTO(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getPermissoes()
        );
    }

    private UsuarioResumoDTO toUsuarioDto(User user) {
        String nomeGrupo = user.getGrupo() != null ? user.getGrupo().getNome() : null;
        return new UsuarioResumoDTO(
                user.getId(),
                user.getEmail(),
                nomeGrupo,
                user.isAdmin(),
                user.isAtivo()
        );
    }
}

package com.plataforma_leilao.app.config;

import java.util.EnumSet;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.EUserPermission;
import com.plataforma_leilao.app.model.GrupoPermissao;
import com.plataforma_leilao.app.repository.GrupoPermissaoRepository;
import com.plataforma_leilao.app.repository.UserRepository;

@Component
@Order(1)
public class PermissaoSeed implements ApplicationRunner {

    private final GrupoPermissaoRepository grupoRepository;
    private final UserRepository userRepository;

    public PermissaoSeed(
            GrupoPermissaoRepository grupoRepository,
            UserRepository userRepository) {
        this.grupoRepository = grupoRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        GrupoPermissao admin = garantirGrupoAdmin();
        garantirGrupoParticipante();
        promoverAdminSeExistir(admin);
    }

    private GrupoPermissao garantirGrupoAdmin() {
        return grupoRepository.findByNome(AdminConstants.GRUPO_ADMIN)
                .orElseGet(() -> {
                    GrupoPermissao grupo = new GrupoPermissao(
                            AdminConstants.GRUPO_ADMIN,
                            "Acesso total: criar leilões e gerenciar permissões");
                    grupo.setPermissoes(EnumSet.allOf(EPermissao.class));
                    return grupoRepository.save(grupo);
                });
    }

    private void garantirGrupoParticipante() {
        if (grupoRepository.existsByNome(AdminConstants.GRUPO_PARTICIPANTE)) {
            return;
        }
        GrupoPermissao grupo = new GrupoPermissao(
                AdminConstants.GRUPO_PARTICIPANTE,
                "Usuário comum: acompanha leilões e dá lances");
        grupoRepository.save(grupo);
    }

    private void promoverAdminSeExistir(GrupoPermissao grupoAdmin) {
        userRepository.findByEmail(AdminConstants.EMAIL_ADMIN).ifPresent(user -> {
            user.setPermissao(EUserPermission.ADMIN);
            user.setGrupo(grupoAdmin);
            userRepository.save(user);
        });
    }
}

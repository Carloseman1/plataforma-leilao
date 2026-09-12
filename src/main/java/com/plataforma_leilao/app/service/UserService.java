package com.plataforma_leilao.app.service;

import com.plataforma_leilao.app.config.AdminConstants;
import com.plataforma_leilao.app.dto.LoginResponseDTO;
import com.plataforma_leilao.app.exceptions.CredenciaisInvalidasException;
import com.plataforma_leilao.app.exceptions.EmailCadastradoException;
import com.plataforma_leilao.app.exceptions.SenhaCadastradaException;
import com.plataforma_leilao.app.exceptions.SenhaVaziaException;
import com.plataforma_leilao.app.model.EUserPermission;
import com.plataforma_leilao.app.model.GrupoPermissao;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.repository.GrupoPermissaoRepository;
import com.plataforma_leilao.app.repository.UserRepository;
import com.plataforma_leilao.app.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GrupoPermissaoRepository grupoRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(
            UserRepository userRepository,
            GrupoPermissaoRepository grupoRepository,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.grupoRepository = grupoRepository;
        this.jwtService = jwtService;
    }

    public void cadastrar(String email, String password) {
        if (password == null) {
            throw new SenhaVaziaException();
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!*]).{8,}$")) {
            throw new SenhaCadastradaException();
        }

        User user = new User(email, encoder.encode(password));
        aplicarGrupoPadrao(user);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                throw new EmailCadastradoException();
            }
            throw e;
        }
    }

    public LoginResponseDTO login(String email, String password) {
        if (email == null || password == null) {
            throw new CredenciaisInvalidasException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!user.isAtivo() || !encoder.matches(password, user.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        if (AdminConstants.EMAIL_ADMIN.equalsIgnoreCase(user.getEmail())) {
            promoverAdmin(user);
        }

        String token = jwtService.gerarToken(user);
        String nomeGrupo = user.getGrupo() != null ? user.getGrupo().getNome() : null;

        return new LoginResponseDTO(
                user.getId(),
                user.getEmail(),
                token,
                nomeGrupo,
                user.isAdmin(),
                jwtService.listarPermissoes(user)
        );
    }

    private void aplicarGrupoPadrao(User user) {
        if (AdminConstants.EMAIL_ADMIN.equalsIgnoreCase(user.getEmail())) {
            promoverAdmin(user);
            return;
        }

        GrupoPermissao participante = grupoRepository
                .findByNome(AdminConstants.GRUPO_PARTICIPANTE)
                .orElse(null);
        user.setGrupo(participante);
        user.setPermissao(EUserPermission.USER);
    }

    private void promoverAdmin(User user) {
        GrupoPermissao admin = grupoRepository
                .findByNome(AdminConstants.GRUPO_ADMIN)
                .orElse(null);
        user.setPermissao(EUserPermission.ADMIN);
        user.setGrupo(admin);
        userRepository.save(user);
    }
}

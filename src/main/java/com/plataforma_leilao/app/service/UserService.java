package com.plataforma_leilao.app.service;

import com.plataforma_leilao.app.exceptions.EmailCadastradoException;
import com.plataforma_leilao.app.exceptions.SenhaCadastradaException;
import com.plataforma_leilao.app.exceptions.SenhaVaziaException;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void cadastrar(String email, String password) {
        if (password == null) {
            throw new SenhaVaziaException();
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!*]).{8,}$")) {
            throw new SenhaCadastradaException();
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String senha = encoder.encode(password);

        User user = new User(email, senha);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                throw new EmailCadastradoException();
            }
        }
    }
}

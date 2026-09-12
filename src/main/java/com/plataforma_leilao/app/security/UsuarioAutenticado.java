package com.plataforma_leilao.app.security;

import com.plataforma_leilao.app.exceptions.UsuarioNaoEncontradoException;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UsuarioAutenticado {

    private final UserRepository userRepository;

    public UsuarioAutenticado(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw new UsuarioNaoEncontradoException();
        }

        HttpServletRequest request = attrs.getRequest();
        Object emailAttr = request.getAttribute("userEmail");

        if (emailAttr == null) {
            throw new UsuarioNaoEncontradoException();
        }

        return userRepository.findByEmail(emailAttr.toString())
                .orElseThrow(UsuarioNaoEncontradoException::new);
    }
}

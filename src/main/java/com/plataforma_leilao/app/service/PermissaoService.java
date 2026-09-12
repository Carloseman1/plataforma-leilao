package com.plataforma_leilao.app.service;

import com.plataforma_leilao.app.exceptions.AcessoNegadoException;
import com.plataforma_leilao.app.model.EPermissao;
import com.plataforma_leilao.app.model.User;
import com.plataforma_leilao.app.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;

@Service
public class PermissaoService {

    private final UsuarioAutenticado usuarioAutenticado;

    public PermissaoService(UsuarioAutenticado usuarioAutenticado) {
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public void exigir(EPermissao permissao) {
        User user = usuarioAutenticado.get();
        if (!user.temPermissao(permissao)) {
            throw new AcessoNegadoException();
        }
    }

    public User usuarioAtual() {
        return usuarioAutenticado.get();
    }
}

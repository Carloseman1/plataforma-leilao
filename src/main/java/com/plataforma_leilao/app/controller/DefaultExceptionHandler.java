package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.dto.ErrorDTO;
import com.plataforma_leilao.app.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handlException(Exception ex) {
        ErrorDTO error = new ErrorDTO("ERRO_INESPERADO", "Ocorreu um erro inesperado");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EmailCadastradoException.class)
    public ResponseEntity<ErrorDTO> handleEmailCadastradoException(EmailCadastradoException ex) {
        ErrorDTO error = new ErrorDTO("EMAIL_CADASTRADO", "O email já está cadastrado.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SenhaCadastradaException.class)
    public ResponseEntity<ErrorDTO> handlerSenhaCadastradaException(SenhaCadastradaException ex) {
        ErrorDTO error = new ErrorDTO(
                "SENHA_CADASTRADA",
                "A senha deve ter pelo menos 8 caracteres, uma maiúscula, uma minúscula, um número e um caractere especial."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SenhaVaziaException.class)
    public ResponseEntity<ErrorDTO> handlerSenhaVaziaException(SenhaVaziaException ex) {
        ErrorDTO error = new ErrorDTO("SENHA_VAZIA", "Por favor, forneça uma senha.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(LeilaoNaoEncontradoException.class)
    public ResponseEntity<ErrorDTO> handleLeilaoNaoEncontrado(LeilaoNaoEncontradoException ex) {
        ErrorDTO error = new ErrorDTO("LEILAO_NAO_ENCONTRADO", "Leilão não encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorDTO> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        ErrorDTO error = new ErrorDTO("CREDENCIAIS_INVALIDAS", "Email ou senha incorretos.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorDTO> handleTokenInvalido(TokenInvalidoException ex) {
        ErrorDTO error = new ErrorDTO("TOKEN_INVALIDO", "Token ausente ou inválido. Faça login novamente.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErrorDTO> handleAcessoNegado(AcessoNegadoException ex) {
        ErrorDTO error = new ErrorDTO("ACESSO_NEGADO", "Você não tem permissão para esta ação.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(GrupoNaoEncontradoException.class)
    public ResponseEntity<ErrorDTO> handleGrupoNaoEncontrado(GrupoNaoEncontradoException ex) {
        ErrorDTO error = new ErrorDTO("GRUPO_NAO_ENCONTRADO", "Grupo de permissão não encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ErrorDTO> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        ErrorDTO error = new ErrorDTO("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

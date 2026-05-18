package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.dto.ErrorDTO;
import com.plataforma_leilao.app.exceptions.EmailCadastradoException;
import com.plataforma_leilao.app.exceptions.SenhaCadastradaException;
import com.plataforma_leilao.app.exceptions.SenhaVaziaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handlException(Exception ex){
        ErrorDTO error = new ErrorDTO("ERRO_INESPERADO", "Ocorreu um erro inesperado");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(EmailCadastradoException.class)
    public ResponseEntity<ErrorDTO> handleEmailCadastradoException(EmailCadastradoException ex){
        ErrorDTO error = new ErrorDTO("EMAIL_CADASTRADO", "O email já está cadastrado.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SenhaCadastradaException.class)
    public ResponseEntity<ErrorDTO> handlerSenhaCadastradaException(SenhaCadastradaException ex){
        ErrorDTO error = new ErrorDTO("SENHA_CADASTRADA", "A senha deve ter pelo menos 8 caracteres, uma maiúscula, uma minúscula, um número e um caractere especial.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SenhaVaziaException.class)
    public ResponseEntity<ErrorDTO> handlerSenhaVaziaException(SenhaVaziaException ex){
        ErrorDTO error = new ErrorDTO("SENHA_VAZIA", "Por favor, forneça uma senha.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}

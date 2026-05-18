package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma_leilao.app.dto.UserDTO;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }



    @PostMapping("/cadastrar")
    public ResponseEntity<Void> cadastrar(@RequestBody UserDTO dto) {
        userService.cadastrar(dto.getEmail(), dto.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @PostMapping("/login")
    public void login(@RequestBody UserDTO dto) {}
}
package com.plataforma_leilao.app.controller;

import com.plataforma_leilao.app.dto.LoginResponseDTO;
import com.plataforma_leilao.app.dto.UserDTO;
import com.plataforma_leilao.app.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
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
    public LoginResponseDTO login(@RequestBody UserDTO dto) {
        return userService.login(dto.getEmail(), dto.getPassword());
    }
}

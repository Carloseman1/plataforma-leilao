package com.plataforma_leilao.app.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.plataforma_leilao.app.dto.UserDTO;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping("/cadastrar")
    public void cadastrar(@RequestBody UserDTO dto) {}

    @PostMapping("/login")
    public void login(@RequestBody UserDTO dto) {}
}
package com.plataforma_leilao.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plataforma_leilao.app.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
    @Override
    List<User> findAll();
}

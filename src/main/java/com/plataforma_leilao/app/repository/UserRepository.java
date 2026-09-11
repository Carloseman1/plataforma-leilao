package com.plataforma_leilao.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.plataforma_leilao.app.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}

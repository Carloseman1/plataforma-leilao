package com.plataforma_leilao.app.repository;

import com.plataforma_leilao.app.model.Leilao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeilaoRepository extends JpaRepository<Leilao, Long> {

    Optional<Leilao> findByUuid(UUID uuid);
}

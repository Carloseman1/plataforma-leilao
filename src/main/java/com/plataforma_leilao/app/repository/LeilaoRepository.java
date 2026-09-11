package com.plataforma_leilao.app.repository;

import com.plataforma_leilao.app.model.Leilao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeilaoRepository extends JpaRepository<Leilao, Long> {
}

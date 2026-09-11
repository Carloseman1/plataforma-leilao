package com.plataforma_leilao.app.repository;

import com.plataforma_leilao.app.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, Long> {
}

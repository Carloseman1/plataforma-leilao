package com.plataforma_leilao.app.repository;

import com.plataforma_leilao.app.model.GrupoPermissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoPermissaoRepository extends JpaRepository<GrupoPermissao, Long> {

    Optional<GrupoPermissao> findByNome(String nome);

    boolean existsByNome(String nome);
}

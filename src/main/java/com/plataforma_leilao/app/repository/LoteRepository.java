package com.plataforma_leilao.app.repository;

import com.plataforma_leilao.app.model.ELoteStatus;
import com.plataforma_leilao.app.model.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    Optional<Lote> findByUuid(UUID uuid);

    List<Lote> findByStatusAndAbertoEmNotNull(ELoteStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lote l where l.uuid = :uuid")
    Optional<Lote> travarPorUuid(@Param("uuid") UUID uuid);
}

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

    /** Lotes com o pregão aberto — é o que o martelo e o lance da casa vigiam. */
    List<Lote> findByStatusAndAbertoEmNotNull(ELoteStatus status);

    /**
     * Trava a linha do lote enquanto um lance é avaliado. A fila do Kafka já
     * entrega um lance de cada vez por lote; a trava cobre o resto — rebalance
     * de consumidor, reprocessamento e o modo direto, que não tem partição.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lote l where l.uuid = :uuid")
    Optional<Lote> travarPorUuid(@Param("uuid") UUID uuid);
}

package com.plataforma_leilao.app.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.plataforma_leilao.app.model.EStatusLance;
import com.plataforma_leilao.app.model.ETipoLance;
import com.plataforma_leilao.app.model.Lance;

public interface LanceRepository extends JpaRepository<Lance, Long> {

        Optional<Lance> findByUuid(UUID uuid);

        Optional<Lance> findTopByLoteIdAndStatusOrderByValorDesc(Long loteId, EStatusLance status);

        Optional<Lance> findTopByLoteIdAndTipoAndStatusOrderByValorDesc(
                        Long loteId, ETipoLance tipo, EStatusLance status);

        List<Lance> findByLoteIdOrderByCriadoEmDesc(Long loteId);

        @Query("""
                        select l from Lance l
                        where l.lote.leilao.uuid = :uuidLeilao
                                and l.status = com.plataforma_leilao.app.model.EStatusLance.INVALIDO
                        order by l.criadoEm desc
                        """)
        List<Lance> recusadosDoLeilao(@Param("uuidLeilao") UUID uuidLeilao, Pageable pagina);

        List<Lance> findByLoteIdAndStatusOrderByCriadoEmDesc(Long loteId, EStatusLance status);
}

package com.plataforma_leilao.app.repository;

import com.plataforma_leilao.app.model.Animal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    Optional<Animal> findByRegistro(String registro);

    Optional<Animal> findByUuid(UUID uuid);
}

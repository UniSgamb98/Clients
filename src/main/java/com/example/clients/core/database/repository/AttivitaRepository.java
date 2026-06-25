package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.Attivita;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttivitaRepository {
    void insert(Attivita attivita);

    void update(Attivita attivita);

    Optional<Attivita> findById(UUID id);

    List<Attivita> findAll();

    List<Attivita> findByStato(String stato);

    void deleteById(UUID id);
}

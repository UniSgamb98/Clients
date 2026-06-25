package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.TipoAttivita;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TipoAttivitaRepository {
    void insert(TipoAttivita tipoAttivita);

    void update(TipoAttivita tipoAttivita);

    Optional<TipoAttivita> findById(UUID id);

    List<TipoAttivita> findAll();

    List<TipoAttivita> findActive();

    void deleteById(UUID id);
}

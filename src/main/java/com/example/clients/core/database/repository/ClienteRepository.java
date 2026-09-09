package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

public interface ClienteRepository {
    void insert(Cliente cliente);

    void update(Cliente cliente);

    void updateStatoTrattativa(UUID clienteId, String statoTrattativa, LocalDateTime updatedAt);

    Optional<Cliente> findById(UUID id);

    List<Cliente> findAll();
}

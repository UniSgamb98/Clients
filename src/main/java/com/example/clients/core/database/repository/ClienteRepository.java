package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {
    void insert(Cliente cliente);

    void update(Cliente cliente);

    Optional<Cliente> findById(UUID id);

    List<Cliente> findAll();
}

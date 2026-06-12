package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.TelefonoCliente;

import java.util.List;
import java.util.UUID;

public interface TelefonoClienteRepository {
    void insertAll(List<TelefonoCliente> telefoni);

    void insert(TelefonoCliente telefono);

    void update(TelefonoCliente telefono);

    void deleteById(UUID id);

    List<TelefonoCliente> findByClienteId(UUID clienteId);
}

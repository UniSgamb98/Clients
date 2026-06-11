package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.TelefonoCliente;

import java.util.List;
import java.util.UUID;

public interface TelefonoClienteRepository {
    void insertAll(List<TelefonoCliente> telefoni);

    List<TelefonoCliente> findByClienteId(UUID clienteId);
}

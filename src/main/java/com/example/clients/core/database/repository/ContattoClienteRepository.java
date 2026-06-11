package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.ContattoCliente;

import java.util.List;
import java.util.UUID;

public interface ContattoClienteRepository {
    void insertAll(List<ContattoCliente> contatti);

    List<ContattoCliente> findByClienteId(UUID clienteId);
}

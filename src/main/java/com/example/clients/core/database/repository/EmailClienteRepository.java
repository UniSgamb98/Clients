package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.EmailCliente;

import java.util.List;
import java.util.UUID;

public interface EmailClienteRepository {
    void insertAll(List<EmailCliente> email);

    void insert(EmailCliente email);

    void update(EmailCliente email);

    void deleteById(UUID id);

    List<EmailCliente> findByClienteId(UUID clienteId);
}

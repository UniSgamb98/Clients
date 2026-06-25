package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.AttivitaCliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttivitaClienteRepository {
    void insert(AttivitaCliente attivitaCliente);

    void update(AttivitaCliente attivitaCliente);

    Optional<AttivitaCliente> findById(UUID id);

    Optional<AttivitaCliente> findByAttivitaIdAndClienteId(UUID attivitaId, UUID clienteId);

    List<AttivitaCliente> findByAttivitaId(UUID attivitaId);

    List<AttivitaCliente> findByClienteId(UUID clienteId);

    void deleteById(UUID id);
}

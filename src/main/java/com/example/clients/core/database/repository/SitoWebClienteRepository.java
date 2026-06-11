package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.SitoWebCliente;

import java.util.List;
import java.util.UUID;

public interface SitoWebClienteRepository {
    void insertAll(List<SitoWebCliente> sitiWeb);

    List<SitoWebCliente> findByClienteId(UUID clienteId);
}

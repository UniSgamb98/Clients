package com.example.clients.core.database.repository;

import com.example.clients.core.database.model.IndirizzoCliente;

import java.util.List;
import java.util.UUID;

public interface IndirizzoClienteRepository {
    void insertAll(List<IndirizzoCliente> indirizzi);

    List<IndirizzoCliente> findByClienteId(UUID clienteId);
}

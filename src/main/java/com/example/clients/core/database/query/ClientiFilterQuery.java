package com.example.clients.core.database.query;

import com.example.clients.core.database.query.record.OperatoreClienteFilterRecord;

import java.util.List;

public interface ClientiFilterQuery {
    List<OperatoreClienteFilterRecord> findOperatoriConClienti();

    List<String> findTipiCliente();

    List<String> findStatiTrattativa();
}

package com.example.clients.core.database.query;

import com.example.clients.core.database.query.result.OperatoreClienteFilterResult;

import java.util.List;

public interface ClientiFilterQuery {
    List<OperatoreClienteFilterResult> findOperatoriConClienti();

    List<String> findTipiCliente();

    List<String> findStatiTrattativa();
}

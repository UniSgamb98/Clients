package com.example.clients.feature.clienti.clienti.dto;

public enum SortColumn {
    NAME("C.RAGIONE_SOCIALE"),
    TYPE("C.TIPO_CLIENTE"),
    CONTACT("REFERENTE"),
    PHONE("TELEFONO"),
    EMAIL("EMAIL"),
    STATUS("C.STATO_TRATTATIVA");

    private final String sqlColumn;

    SortColumn(String sqlColumn) {
        this.sqlColumn = sqlColumn;
    }

    public String sqlColumn() {
        return sqlColumn;
    }
}

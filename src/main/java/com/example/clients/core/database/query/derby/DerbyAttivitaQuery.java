package com.example.clients.core.database.query.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.query.AttivitaQuery;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaClienteRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaDetailRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaListRecord;

import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DerbyAttivitaQuery implements AttivitaQuery {

    private final Database database;
    public DerbyAttivitaQuery(Database database) {
        this.database = database;
    }

    @Override
    public List<AttivitaListRecord> findAll() {
        String sql = "SELECT A.ID, A.TITOLO, T.NOME AS TIPO_ATTIVITA, A.PRIORITA, A.STATO, A.DATA_INIZIO, A.DATA_FINE, "
                + "COUNT(AC.ID) AS TOTALE_CLIENTI, "
                + "SUM(CASE WHEN AC.STATO = 'DA_FARE' THEN 1 ELSE 0 END) AS DA_FARE, "
                + "SUM(CASE WHEN AC.STATO = 'IN_CORSO' THEN 1 ELSE 0 END) AS IN_CORSO, "
                + "SUM(CASE WHEN AC.STATO = 'SOSPESO' THEN 1 ELSE 0 END) AS SOSPESI, "
                + "SUM(CASE WHEN AC.STATO = 'COMPLETATO' THEN 1 ELSE 0 END) AS COMPLETATI, "
                + "SUM(CASE WHEN AC.STATO = 'ANNULLATO' THEN 1 ELSE 0 END) AS ANNULLATI "
                + "FROM ATTIVITA A "
                + "LEFT JOIN TIPI_ATTIVITA T ON A.TIPO_ATTIVITA_ID = T.ID "
                + "LEFT JOIN ATTIVITA_CLIENTI AC ON A.ID = AC.ATTIVITA_ID "
                + "GROUP BY A.ID, A.TITOLO, T.NOME, A.PRIORITA, A.STATO, A.DATA_INIZIO, A.DATA_FINE "
                + "ORDER BY A.DATA_FINE, A.CREATED_AT DESC";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<AttivitaListRecord> attivita = new ArrayList<>();
            while (resultSet.next()) {
                attivita.add(new AttivitaListRecord(
                        getUuid(resultSet, "ID"),
                        valueOrEmpty(resultSet.getString("TITOLO")),
                        valueOrEmpty(resultSet.getString("TIPO_ATTIVITA")),
                        getInteger(resultSet, "PRIORITA"),
                        valueOrEmpty(resultSet.getString("STATO")),
                        getDate(resultSet, "DATA_INIZIO"),
                        getDate(resultSet, "DATA_FINE"),
                        resultSet.getInt("TOTALE_CLIENTI"),
                        resultSet.getInt("DA_FARE"),
                        resultSet.getInt("IN_CORSO"),
                        resultSet.getInt("SOSPESI"),
                        resultSet.getInt("COMPLETATI"),
                        resultSet.getInt("ANNULLATI")
                ));
            }
            return attivita;
        } catch (SQLException e) {
            throw new RuntimeException("Errore elenco attività.", e);
        }
    }

    @Override
    public Optional<AttivitaDetailRecord> findById(UUID attivitaId) {
        String sql = "SELECT A.ID, A.TITOLO, A.DESCRIZIONE, T.NOME AS TIPO_ATTIVITA, A.PRIORITA, A.STATO, A.DATA_INIZIO, A.DATA_FINE "
                + "FROM ATTIVITA A LEFT JOIN TIPI_ATTIVITA T ON A.TIPO_ATTIVITA_ID = T.ID WHERE A.ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, attivitaId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(new AttivitaDetailRecord(
                        getUuid(resultSet, "ID"),
                        valueOrEmpty(resultSet.getString("TITOLO")),
                        getClobText(resultSet, "DESCRIZIONE"),
                        valueOrEmpty(resultSet.getString("TIPO_ATTIVITA")),
                        getInteger(resultSet, "PRIORITA"),
                        valueOrEmpty(resultSet.getString("STATO")),
                        getDate(resultSet, "DATA_INIZIO"),
                        getDate(resultSet, "DATA_FINE"),
                        findClienti(attivitaId)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore dettaglio attività.", e);
        }
    }

    private List<AttivitaClienteRecord> findClienti(UUID attivitaId) throws SQLException {
        String sql = "SELECT AC.ID, AC.CLIENTE_ID, C.RAGIONE_SOCIALE, AC.STATO, AC.INTERAZIONE_ID, "
                + "I.DATA_CONTATTO, I.PROSSIMO_CONTATTO, N.TESTO "
                + "FROM ATTIVITA_CLIENTI AC "
                + "JOIN CLIENTI C ON AC.CLIENTE_ID = C.ID "
                + "LEFT JOIN INTERAZIONI I ON AC.INTERAZIONE_ID = I.ID "
                + "LEFT JOIN NOTE_CLIENTE N ON I.NOTA_ID = N.ID "
                + "WHERE AC.ATTIVITA_ID = ? ORDER BY C.RAGIONE_SOCIALE";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, attivitaId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AttivitaClienteRecord> clienti = new ArrayList<>();
                while (resultSet.next()) {
                    clienti.add(new AttivitaClienteRecord(
                            getUuid(resultSet, "ID"),
                            getUuid(resultSet, "CLIENTE_ID"),
                            valueOrEmpty(resultSet.getString("RAGIONE_SOCIALE")),
                            valueOrEmpty(resultSet.getString("STATO")),
                            getUuid(resultSet, "INTERAZIONE_ID"),
                            getDate(resultSet, "DATA_CONTATTO"),
                            getDate(resultSet, "PROSSIMO_CONTATTO"),
                            getClobText(resultSet, "TESTO")
                    ));
                }
                return clienti;
            }
        }
    }

    private UUID getUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private Integer getInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private LocalDate getDate(ResultSet resultSet, String column) throws SQLException {
        Date value = resultSet.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private String getClobText(ResultSet resultSet, String column) throws SQLException {
        try {
            Clob clob = resultSet.getClob(column);
            if (clob == null || clob.length() == 0) {
                return "";
            }
            return clob.getSubString(1, Math.toIntExact(clob.length()));
        } catch (SQLException e) {
            String value = resultSet.getString(column);
            return value == null ? "" : value;
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}

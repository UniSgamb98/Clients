package com.example.clients.core.database.repository.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.AttivitaCliente;
import com.example.clients.core.database.repository.AttivitaClienteRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DerbyAttivitaClienteRepository extends DerbyRepositorySupport implements AttivitaClienteRepository {

    public DerbyAttivitaClienteRepository(Database database) {
        super(database);
    }

    @Override
    public void insert(AttivitaCliente attivitaCliente) {
        String sql = "INSERT INTO ATTIVITA_CLIENTI (ID, ATTIVITA_ID, CLIENTE_ID, STATO, INTERAZIONE_ID, CREATED_AT, UPDATED_AT) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            bindAttivitaCliente(statement, attivitaCliente);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore inserimento cliente attività.", e);
        }
    }

    @Override
    public void update(AttivitaCliente attivitaCliente) {
        String sql = "UPDATE ATTIVITA_CLIENTI SET STATO = ?, INTERAZIONE_ID = ?, UPDATED_AT = ? WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, attivitaCliente.stato());
            setUuid(statement, 2, attivitaCliente.interazioneId());
            setTimestamp(statement, 3, attivitaCliente.updatedAt());
            setUuid(statement, 4, attivitaCliente.id());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore aggiornamento cliente attività.", e);
        }
    }

    @Override
    public Optional<AttivitaCliente> findById(UUID id) {
        String sql = "SELECT * FROM ATTIVITA_CLIENTI WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAttivitaCliente(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw repositoryException("Errore lettura cliente attività.", e);
        }
    }

    @Override
    public Optional<AttivitaCliente> findByAttivitaIdAndClienteId(UUID attivitaId, UUID clienteId) {
        String sql = "SELECT * FROM ATTIVITA_CLIENTI WHERE ATTIVITA_ID = ? AND CLIENTE_ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, attivitaId);
            setUuid(statement, 2, clienteId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAttivitaCliente(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw repositoryException("Errore lettura cliente attività per attività e cliente.", e);
        }
    }

    @Override
    public List<AttivitaCliente> findByAttivitaId(UUID attivitaId) {
        String sql = "SELECT * FROM ATTIVITA_CLIENTI WHERE ATTIVITA_ID = ? ORDER BY CREATED_AT";
        return findByUuid(sql, attivitaId, "Errore elenco clienti attività.");
    }

    @Override
    public List<AttivitaCliente> findByClienteId(UUID clienteId) {
        String sql = "SELECT * FROM ATTIVITA_CLIENTI WHERE CLIENTE_ID = ? ORDER BY CREATED_AT";
        return findByUuid(sql, clienteId, "Errore elenco attività cliente.");
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM ATTIVITA_CLIENTI WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore eliminazione cliente attività.", e);
        }
    }

    private List<AttivitaCliente> findByUuid(String sql, UUID id, String errorMessage) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<AttivitaCliente> clienti = new ArrayList<>();
                while (resultSet.next()) {
                    clienti.add(mapAttivitaCliente(resultSet));
                }
                return clienti;
            }
        } catch (SQLException e) {
            throw repositoryException(errorMessage, e);
        }
    }

    private void bindAttivitaCliente(PreparedStatement statement, AttivitaCliente attivitaCliente) throws SQLException {
        setUuid(statement, 1, attivitaCliente.id());
        setUuid(statement, 2, attivitaCliente.attivitaId());
        setUuid(statement, 3, attivitaCliente.clienteId());
        statement.setString(4, attivitaCliente.stato());
        setUuid(statement, 5, attivitaCliente.interazioneId());
        setTimestamp(statement, 6, attivitaCliente.createdAt());
        setTimestamp(statement, 7, attivitaCliente.updatedAt());
    }

    private AttivitaCliente mapAttivitaCliente(ResultSet resultSet) throws SQLException {
        return new AttivitaCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "ATTIVITA_ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                resultSet.getString("STATO"),
                getUuid(resultSet, "INTERAZIONE_ID"),
                getTimestamp(resultSet, "CREATED_AT"),
                getTimestamp(resultSet, "UPDATED_AT")
        );
    }
}

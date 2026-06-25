package com.example.clients.core.database.repository.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.Attivita;
import com.example.clients.core.database.repository.AttivitaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DerbyAttivitaRepository extends DerbyRepositorySupport implements AttivitaRepository {

    public DerbyAttivitaRepository(Database database) {
        super(database);
    }

    @Override
    public void insert(Attivita attivita) {
        String sql = "INSERT INTO ATTIVITA (ID, TITOLO, DESCRIZIONE, PRIORITA, STATO, DATA_INIZIO, DATA_FINE, TIPO_ATTIVITA_ID, CREATED_AT, UPDATED_AT) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            bindAttivita(statement, attivita);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore inserimento attività.", e);
        }
    }

    @Override
    public void update(Attivita attivita) {
        String sql = "UPDATE ATTIVITA SET TITOLO = ?, DESCRIZIONE = ?, PRIORITA = ?, STATO = ?, DATA_INIZIO = ?, DATA_FINE = ?, TIPO_ATTIVITA_ID = ?, UPDATED_AT = ? WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, attivita.titolo());
            statement.setString(2, attivita.descrizione());
            setInteger(statement, 3, attivita.priorita());
            statement.setString(4, attivita.stato());
            setDate(statement, 5, attivita.dataInizio());
            setDate(statement, 6, attivita.dataFine());
            setUuid(statement, 7, attivita.tipoAttivitaId());
            setTimestamp(statement, 8, attivita.updatedAt());
            setUuid(statement, 9, attivita.id());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore aggiornamento attività.", e);
        }
    }

    @Override
    public Optional<Attivita> findById(UUID id) {
        String sql = "SELECT * FROM ATTIVITA WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapAttivita(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw repositoryException("Errore lettura attività.", e);
        }
    }

    @Override
    public List<Attivita> findAll() {
        return findBySql("SELECT * FROM ATTIVITA ORDER BY DATA_FINE, CREATED_AT DESC");
    }

    @Override
    public List<Attivita> findByStato(String stato) {
        String sql = "SELECT * FROM ATTIVITA WHERE STATO = ? ORDER BY DATA_FINE, CREATED_AT DESC";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, stato);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapAttivitaList(resultSet);
            }
        } catch (SQLException e) {
            throw repositoryException("Errore elenco attività per stato.", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM ATTIVITA WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore eliminazione attività.", e);
        }
    }

    private List<Attivita> findBySql(String sql) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapAttivitaList(resultSet);
        } catch (SQLException e) {
            throw repositoryException("Errore elenco attività.", e);
        }
    }

    private List<Attivita> mapAttivitaList(ResultSet resultSet) throws SQLException {
        List<Attivita> attivita = new ArrayList<>();
        while (resultSet.next()) {
            attivita.add(mapAttivita(resultSet));
        }
        return attivita;
    }

    private void bindAttivita(PreparedStatement statement, Attivita attivita) throws SQLException {
        setUuid(statement, 1, attivita.id());
        statement.setString(2, attivita.titolo());
        statement.setString(3, attivita.descrizione());
        setInteger(statement, 4, attivita.priorita());
        statement.setString(5, attivita.stato());
        setDate(statement, 6, attivita.dataInizio());
        setDate(statement, 7, attivita.dataFine());
        setUuid(statement, 8, attivita.tipoAttivitaId());
        setTimestamp(statement, 9, attivita.createdAt());
        setTimestamp(statement, 10, attivita.updatedAt());
    }

    private Attivita mapAttivita(ResultSet resultSet) throws SQLException {
        return new Attivita(
                getUuid(resultSet, "ID"),
                resultSet.getString("TITOLO"),
                resultSet.getString("DESCRIZIONE"),
                getInteger(resultSet, "PRIORITA"),
                resultSet.getString("STATO"),
                getDate(resultSet, "DATA_INIZIO"),
                getDate(resultSet, "DATA_FINE"),
                getUuid(resultSet, "TIPO_ATTIVITA_ID"),
                getTimestamp(resultSet, "CREATED_AT"),
                getTimestamp(resultSet, "UPDATED_AT")
        );
    }
}

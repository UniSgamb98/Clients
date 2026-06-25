package com.example.clients.core.database.repository.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.TipoAttivita;
import com.example.clients.core.database.repository.TipoAttivitaRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DerbyTipoAttivitaRepository extends DerbyRepositorySupport implements TipoAttivitaRepository {

    public DerbyTipoAttivitaRepository(Database database) {
        super(database);
    }

    @Override
    public void insert(TipoAttivita tipoAttivita) {
        String sql = "INSERT INTO TIPI_ATTIVITA (ID, NOME, ORDINE, ATTIVO) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            bindTipoAttivita(statement, tipoAttivita);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore inserimento tipo attività.", e);
        }
    }

    @Override
    public void update(TipoAttivita tipoAttivita) {
        String sql = "UPDATE TIPI_ATTIVITA SET NOME = ?, ORDINE = ?, ATTIVO = ? WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, tipoAttivita.nome());
            setInteger(statement, 2, tipoAttivita.ordine());
            statement.setInt(3, tipoAttivita.attivo() ? 1 : 0);
            setUuid(statement, 4, tipoAttivita.id());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore aggiornamento tipo attività.", e);
        }
    }

    @Override
    public Optional<TipoAttivita> findById(UUID id) {
        String sql = "SELECT * FROM TIPI_ATTIVITA WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTipoAttivita(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw repositoryException("Errore lettura tipo attività.", e);
        }
    }

    @Override
    public List<TipoAttivita> findAll() {
        return findBySql("SELECT * FROM TIPI_ATTIVITA ORDER BY ORDINE, NOME");
    }

    @Override
    public List<TipoAttivita> findActive() {
        return findBySql("SELECT * FROM TIPI_ATTIVITA WHERE ATTIVO = 1 ORDER BY ORDINE, NOME");
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM TIPI_ATTIVITA WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore eliminazione tipo attività.", e);
        }
    }

    private List<TipoAttivita> findBySql(String sql) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<TipoAttivita> tipi = new ArrayList<>();
            while (resultSet.next()) {
                tipi.add(mapTipoAttivita(resultSet));
            }
            return tipi;
        } catch (SQLException e) {
            throw repositoryException("Errore elenco tipi attività.", e);
        }
    }

    private void bindTipoAttivita(PreparedStatement statement, TipoAttivita tipoAttivita) throws SQLException {
        setUuid(statement, 1, tipoAttivita.id());
        statement.setString(2, tipoAttivita.nome());
        setInteger(statement, 3, tipoAttivita.ordine());
        statement.setInt(4, tipoAttivita.attivo() ? 1 : 0);
    }

    private TipoAttivita mapTipoAttivita(ResultSet resultSet) throws SQLException {
        return new TipoAttivita(
                getUuid(resultSet, "ID"),
                resultSet.getString("NOME"),
                getInteger(resultSet, "ORDINE"),
                resultSet.getInt("ATTIVO") == 1
        );
    }
}

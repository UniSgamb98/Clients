package com.example.clients.core.database.repository.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.repository.ClienteRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class DerbyClienteRepository extends DerbyRepositorySupport implements ClienteRepository {

    public DerbyClienteRepository(Database database) {
        super(database);
    }

    @Override
    public void insert(Cliente cliente) {
        String sql = "INSERT INTO CLIENTI (ID, RAGIONE_SOCIALE, TIPO_CLIENTE, STATO_TRATTATIVA, PARTITA_IVA, CODICE_FISCALE, ACQUISIZIONE, OPERATORE_ID, CREATED_AT, UPDATED_AT) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            bindCliente(statement, cliente);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore inserimento cliente.", e);
        }
    }

    @Override
    public void update(Cliente cliente) {
        String sql = "UPDATE CLIENTI SET RAGIONE_SOCIALE = ?, TIPO_CLIENTE = ?, STATO_TRATTATIVA = ?, PARTITA_IVA = ?, CODICE_FISCALE = ?, "
                + "ACQUISIZIONE = ?, OPERATORE_ID = ?, UPDATED_AT = ? WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, cliente.ragioneSociale());
            statement.setString(2, cliente.tipoCliente());
            statement.setString(3, cliente.statoTrattativa());
            statement.setString(4, cliente.partitaIva());
            statement.setString(5, cliente.codiceFiscale());
            setDate(statement, 6, cliente.acquisizione());
            setUuid(statement, 7, cliente.operatoreId());
            setTimestamp(statement, 8, cliente.updatedAt());
            setUuid(statement, 9, cliente.id());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw repositoryException("Errore aggiornamento cliente.", e);
        }
    }

    @Override
    public Optional<Cliente> findById(UUID id) {
        String sql = "SELECT * FROM CLIENTI WHERE ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCliente(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw repositoryException("Errore lettura cliente.", e);
        }
    }

    @Override
    public List<Cliente> findAll() {
        String sql = "SELECT * FROM CLIENTI ORDER BY RAGIONE_SOCIALE";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Cliente> clienti = new ArrayList<>();
            while (resultSet.next()) {
                clienti.add(mapCliente(resultSet));
            }
            return clienti;
        } catch (SQLException e) {
            throw repositoryException("Errore elenco clienti.", e);
        }
    }

    private void bindCliente(PreparedStatement statement, Cliente cliente) throws SQLException {
        setUuid(statement, 1, cliente.id());
        statement.setString(2, cliente.ragioneSociale());
        statement.setString(3, cliente.tipoCliente());
        statement.setString(4, cliente.statoTrattativa());
        statement.setString(5, cliente.partitaIva());
        statement.setString(6, cliente.codiceFiscale());
        setDate(statement, 7, cliente.acquisizione());
        setUuid(statement, 8, cliente.operatoreId());
        setTimestamp(statement, 9, cliente.createdAt());
        setTimestamp(statement, 10, cliente.updatedAt());
    }

    private Cliente mapCliente(ResultSet resultSet) throws SQLException {
        return new Cliente(
                getUuid(resultSet, "ID"),
                resultSet.getString("RAGIONE_SOCIALE"),
                resultSet.getString("TIPO_CLIENTE"),
                resultSet.getString("STATO_TRATTATIVA"),
                resultSet.getString("PARTITA_IVA"),
                resultSet.getString("CODICE_FISCALE"),
                getDate(resultSet, "ACQUISIZIONE"),
                getUuid(resultSet, "OPERATORE_ID"),
                getTimestamp(resultSet, "CREATED_AT"),
                getTimestamp(resultSet, "UPDATED_AT")
        );
    }
}

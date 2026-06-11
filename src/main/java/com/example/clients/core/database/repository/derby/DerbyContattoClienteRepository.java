package com.example.clients.core.database.repository.derby;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.ContattoCliente;
import com.example.clients.core.database.repository.ContattoClienteRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DerbyContattoClienteRepository extends DerbyRepositorySupport implements ContattoClienteRepository {

    public DerbyContattoClienteRepository(Database database) {
        super(database);
    }

    @Override
    public void insertAll(List<ContattoCliente> contatti) {
        String sql = "INSERT INTO CONTATTI_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES (?, ?, ?)";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            for (ContattoCliente contatto : contatti) {
                setUuid(statement, 1, contatto.id());
                setUuid(statement, 2, contatto.clienteId());
                statement.setString(3, contatto.descrizione());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw repositoryException("Errore inserimento contatti cliente.", e);
        }
    }

    @Override
    public List<ContattoCliente> findByClienteId(UUID clienteId) {
        String sql = "SELECT * FROM CONTATTI_CLIENTE WHERE CLIENTE_ID = ?";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            setUuid(statement, 1, clienteId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ContattoCliente> contatti = new ArrayList<>();
                while (resultSet.next()) {
                    contatti.add(new ContattoCliente(
                            getUuid(resultSet, "ID"),
                            getUuid(resultSet, "CLIENTE_ID"),
                            resultSet.getString("DESCRIZIONE")
                    ));
                }
                return contatti;
            }
        } catch (SQLException e) {
            throw repositoryException("Errore lettura contatti cliente.", e);
        }
    }
}

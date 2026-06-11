package com.example.clients.core.database.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.core.database.repository.ClienteRepository;
import com.example.clients.core.database.repository.ContattoClienteRepository;
import com.example.clients.core.database.repository.EmailClienteRepository;
import com.example.clients.core.database.repository.IndirizzoClienteRepository;
import com.example.clients.core.database.repository.SitoWebClienteRepository;
import com.example.clients.core.database.repository.TelefonoClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyContattoClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyEmailClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyIndirizzoClienteRepository;
import com.example.clients.core.database.repository.derby.DerbySitoWebClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyTelefonoClienteRepository;
import com.example.clients.core.database.model.ClienteAggregate;

import java.sql.Connection;
import java.sql.SQLException;

public class ClientePersistenceService {

    private final Database database;
    private final SchemaInitializer schemaInitializer;
    private final ClienteRepository clienteRepository;
    private final IndirizzoClienteRepository indirizzoRepository;
    private final SitoWebClienteRepository sitoWebRepository;
    private final ContattoClienteRepository contattoRepository;
    private final TelefonoClienteRepository telefonoRepository;
    private final EmailClienteRepository emailRepository;

    public ClientePersistenceService() {
        this(new Database());
    }

    public ClientePersistenceService(Database database) {
        this(
                database,
                new SchemaInitializer(database),
                new DerbyClienteRepository(database),
                new DerbyIndirizzoClienteRepository(database),
                new DerbySitoWebClienteRepository(database),
                new DerbyContattoClienteRepository(database),
                new DerbyTelefonoClienteRepository(database),
                new DerbyEmailClienteRepository(database)
        );
    }

    public ClientePersistenceService(
            Database database,
            SchemaInitializer schemaInitializer,
            ClienteRepository clienteRepository,
            IndirizzoClienteRepository indirizzoRepository,
            SitoWebClienteRepository sitoWebRepository,
            ContattoClienteRepository contattoRepository,
            TelefonoClienteRepository telefonoRepository,
            EmailClienteRepository emailRepository
    ) {
        this.database = database;
        this.schemaInitializer = schemaInitializer;
        this.clienteRepository = clienteRepository;
        this.indirizzoRepository = indirizzoRepository;
        this.sitoWebRepository = sitoWebRepository;
        this.contattoRepository = contattoRepository;
        this.telefonoRepository = telefonoRepository;
        this.emailRepository = emailRepository;
    }

    public void saveNuovoCliente(ClienteAggregate draft) {
        schemaInitializer.initialize();

        Connection connection = database.getConnection();
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            clienteRepository.insert(draft.cliente());
            indirizzoRepository.insertAll(draft.indirizzi());
            sitoWebRepository.insertAll(draft.sitiWeb());
            contattoRepository.insertAll(draft.contatti());
            telefonoRepository.insertAll(draft.telefoni());
            emailRepository.insertAll(draft.email());

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(connection);
            throw new RuntimeException("Errore salvataggio nuovo cliente.", e);
        } finally {
            restoreAutoCommit(connection, previousAutoCommit);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new RuntimeException("Errore rollback salvataggio cliente.", e);
        }
    }

    private void restoreAutoCommit(Connection connection, boolean previousAutoCommit) {
        try {
            connection.setAutoCommit(previousAutoCommit);
        } catch (SQLException e) {
            throw new RuntimeException("Errore ripristino auto-commit.", e);
        }
    }
}

package com.example.clients.core.database.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.model.ContattoCliente;
import com.example.clients.core.database.model.EmailCliente;
import com.example.clients.core.database.model.IndirizzoCliente;
import com.example.clients.core.database.model.Interazione;
import com.example.clients.core.database.model.NotaCliente;
import com.example.clients.core.database.model.SitoWebCliente;
import com.example.clients.core.database.model.TelefonoCliente;
import com.example.clients.core.database.repository.ClientePreferitoRepository;
import com.example.clients.core.database.repository.ClienteRepository;
import com.example.clients.core.database.repository.ContattoClienteRepository;
import com.example.clients.core.database.repository.EmailClienteRepository;
import com.example.clients.core.database.repository.IndirizzoClienteRepository;
import com.example.clients.core.database.repository.InterazioneRepository;
import com.example.clients.core.database.repository.NotaClienteRepository;
import com.example.clients.core.database.repository.SitoWebClienteRepository;
import com.example.clients.core.database.repository.TelefonoClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyClientePreferitoRepository;
import com.example.clients.core.database.repository.derby.DerbyClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyContattoClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyEmailClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyIndirizzoClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyInterazioneRepository;
import com.example.clients.core.database.repository.derby.DerbyNotaClienteRepository;
import com.example.clients.core.database.repository.derby.DerbySitoWebClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyTelefonoClienteRepository;
import com.example.clients.core.database.model.ClienteAggregate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClientePersistenceService {

    private final Database database;
    private final SchemaInitializer schemaInitializer;
    private final ClienteRepository clienteRepository;
    private final IndirizzoClienteRepository indirizzoRepository;
    private final SitoWebClienteRepository sitoWebRepository;
    private final ContattoClienteRepository contattoRepository;
    private final TelefonoClienteRepository telefonoRepository;
    private final EmailClienteRepository emailRepository;
    private final NotaClienteRepository notaRepository;
    private final InterazioneRepository interazioneRepository;
    private final ClientePreferitoRepository clientePreferitoRepository;

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
                new DerbyEmailClienteRepository(database),
                new DerbyNotaClienteRepository(database),
                new DerbyInterazioneRepository(database),
                new DerbyClientePreferitoRepository(database)
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
            EmailClienteRepository emailRepository,
            NotaClienteRepository notaRepository,
            InterazioneRepository interazioneRepository,
            ClientePreferitoRepository clientePreferitoRepository
    ) {
        this.database = database;
        this.schemaInitializer = schemaInitializer;
        this.clienteRepository = clienteRepository;
        this.indirizzoRepository = indirizzoRepository;
        this.sitoWebRepository = sitoWebRepository;
        this.contattoRepository = contattoRepository;
        this.telefonoRepository = telefonoRepository;
        this.emailRepository = emailRepository;
        this.notaRepository = notaRepository;
        this.interazioneRepository = interazioneRepository;
        this.clientePreferitoRepository = clientePreferitoRepository;
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


    public void updateClienteProfile(
            Cliente cliente,
            List<IndirizzoCliente> indirizzi,
            List<SitoWebCliente> sitiWeb,
            List<ContattoCliente> contatti,
            List<TelefonoCliente> telefoni,
            List<EmailCliente> email,
            List<NotaCliente> note,
            List<Interazione> interazioni
    ) {
        schemaInitializer.initialize();

        Connection connection = database.getConnection();
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            clienteRepository.update(cliente);
            syncIndirizzi(cliente.id(), indirizzi);
            syncSitiWeb(cliente.id(), sitiWeb);
            syncContatti(cliente.id(), contatti);
            syncTelefoni(cliente.id(), telefoni);
            syncEmail(cliente.id(), email);
            for (NotaCliente nota : note) {
                notaRepository.update(nota);
            }
            for (Interazione interazione : interazioni) {
                interazioneRepository.update(interazione);
            }

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(connection);
            throw new RuntimeException("Errore aggiornamento scheda cliente.", e);
        } finally {
            restoreAutoCommit(connection, previousAutoCommit);
        }
    }

    private void syncTelefoni(UUID clienteId, List<TelefonoCliente> desired) {
        List<TelefonoCliente> genericTelefoni = telefonoRepository.findByClienteId(clienteId).stream()
                .filter(telefono -> telefono.contattoId() == null)
                .toList();
        syncById(genericTelefoni, desired, TelefonoCliente::id, telefonoRepository::insert, telefonoRepository::update, telefonoRepository::deleteById);
    }

    private void syncEmail(UUID clienteId, List<EmailCliente> desired) {
        List<EmailCliente> genericEmail = emailRepository.findByClienteId(clienteId).stream()
                .filter(emailCliente -> emailCliente.contattoId() == null)
                .toList();
        syncById(genericEmail, desired, EmailCliente::id, emailRepository::insert, emailRepository::update, emailRepository::deleteById);
    }

    private void syncSitiWeb(UUID clienteId, List<SitoWebCliente> desired) {
        syncById(sitoWebRepository.findByClienteId(clienteId), desired, SitoWebCliente::id, sitoWebRepository::insert, sitoWebRepository::update, sitoWebRepository::deleteById);
    }

    private void syncContatti(UUID clienteId, List<ContattoCliente> desired) {
        syncById(contattoRepository.findByClienteId(clienteId), desired, ContattoCliente::id, contattoRepository::insert, contattoRepository::update, contattoRepository::deleteById);
    }

    private void syncIndirizzi(UUID clienteId, List<IndirizzoCliente> desired) {
        syncById(indirizzoRepository.findByClienteId(clienteId), desired, IndirizzoCliente::id, indirizzoRepository::insert, indirizzoRepository::update, indirizzoRepository::deleteById);
    }

    private <T> void syncById(
            List<T> existing,
            List<T> desired,
            Function<T, UUID> idExtractor,
            Consumer<T> insert,
            Consumer<T> update,
            Consumer<UUID> delete
    ) {
        Map<UUID, T> existingById = existing.stream()
                .collect(Collectors.toMap(idExtractor, Function.identity()));
        Set<UUID> desiredIds = desired.stream()
                .map(idExtractor)
                .collect(Collectors.toSet());

        for (T item : desired) {
            UUID id = idExtractor.apply(item);
            if (existingById.containsKey(id)) {
                update.accept(item);
            } else {
                insert.accept(item);
            }
        }

        for (UUID existingId : existingById.keySet()) {
            if (!desiredIds.contains(existingId)) {
                delete.accept(existingId);
            }
        }
    }

    public void addNota(NotaCliente nota) {
        schemaInitializer.initialize();
        notaRepository.insert(nota);
    }

    public void addChiamata(NotaCliente nota, Interazione interazione) {
        schemaInitializer.initialize();

        Connection connection = database.getConnection();
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            if (nota != null) {
                notaRepository.insert(nota);
            }
            interazioneRepository.insert(interazione);

            connection.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(connection);
            throw new RuntimeException("Errore salvataggio chiamata cliente.", e);
        } finally {
            restoreAutoCommit(connection, previousAutoCommit);
        }
    }

    public boolean togglePreferito(UUID operatoreId, UUID clienteId) {
        schemaInitializer.initialize();

        boolean alreadyFavorite = clientePreferitoRepository.exists(operatoreId, clienteId);
        if (alreadyFavorite) {
            clientePreferitoRepository.remove(operatoreId, clienteId);
            return false;
        }

        clientePreferitoRepository.add(operatoreId, clienteId);
        return true;
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

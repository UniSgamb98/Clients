package com.example.clients.feature.clienti.repository;

import com.example.clients.feature.clienti.model.AttivitaCliente;
import com.example.clients.feature.clienti.model.Cliente;
import com.example.clients.feature.clienti.model.ContattoCliente;
import com.example.clients.feature.clienti.model.EmailCliente;
import com.example.clients.feature.clienti.model.IndirizzoCliente;
import com.example.clients.feature.clienti.model.NotaCliente;
import com.example.clients.feature.clienti.model.Operatore;
import com.example.clients.feature.clienti.model.TelefonoCliente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientiRepository {

    void ensureSchema() throws SQLException;

    void saveCliente(Cliente cliente) throws SQLException;

    void updateCliente(Cliente cliente) throws SQLException;

    Optional<Cliente> findClienteById(UUID id) throws SQLException;

    List<Cliente> findAllClienti() throws SQLException;

    void deleteCliente(UUID id) throws SQLException;

    void saveContatto(ContattoCliente contatto) throws SQLException;

    List<ContattoCliente> findContattiByClienteId(UUID clienteId) throws SQLException;

    void saveIndirizzo(IndirizzoCliente indirizzo) throws SQLException;

    List<IndirizzoCliente> findIndirizziByClienteId(UUID clienteId) throws SQLException;

    void saveTelefono(TelefonoCliente telefono) throws SQLException;

    List<TelefonoCliente> findTelefoniByClienteId(UUID clienteId) throws SQLException;

    List<TelefonoCliente> findTelefoniByContattoId(UUID contattoId) throws SQLException;

    void saveEmail(EmailCliente email) throws SQLException;

    List<EmailCliente> findEmailByClienteId(UUID clienteId) throws SQLException;

    List<EmailCliente> findEmailByContattoId(UUID contattoId) throws SQLException;

    void saveAttivita(AttivitaCliente attivita) throws SQLException;

    List<AttivitaCliente> findAttivitaByClienteId(UUID clienteId) throws SQLException;

    void saveNota(NotaCliente nota) throws SQLException;

    List<NotaCliente> findNoteByClienteId(UUID clienteId) throws SQLException;

    void saveOperatore(Operatore operatore) throws SQLException;

    Optional<Operatore> findOperatoreById(UUID id) throws SQLException;
}

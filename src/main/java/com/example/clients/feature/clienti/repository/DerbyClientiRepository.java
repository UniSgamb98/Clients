package com.example.clients.feature.clienti.repository;

import com.example.clients.core.database.Database;
import com.example.clients.feature.clienti.model.AttivitaCliente;
import com.example.clients.feature.clienti.model.Cliente;
import com.example.clients.feature.clienti.model.ContattoCliente;
import com.example.clients.feature.clienti.model.EmailCliente;
import com.example.clients.feature.clienti.model.IndirizzoCliente;
import com.example.clients.feature.clienti.model.NotaCliente;
import com.example.clients.feature.clienti.model.Operatore;
import com.example.clients.feature.clienti.model.TelefonoCliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DerbyClientiRepository implements ClientiRepository {

    private static final String SCHEMA_RESOURCE = "/db/clienti_schema.sql";

    private final Database database;

    public DerbyClientiRepository(Database database) {
        this.database = database;
    }

    @Override
    public void ensureSchema() throws SQLException {
        List<String> statements = readSchemaStatements();

        try (Statement statement = connection().createStatement()) {
            for (String sql : statements) {
                try {
                    statement.execute(sql);
                } catch (SQLException e) {
                    if (!isAlreadyCreatedError(e)) {
                        throw e;
                    }
                }
            }
        }
    }

    @Override
    public void saveCliente(Cliente cliente) throws SQLException {
        String sql = """
                INSERT INTO CLIENTI (
                    ID, RAGIONE_SOCIALE, TIPO_CLIENTE, INTERESSAMENTO, PARTITA_IVA, CODICE_FISCALE,
                    SITO_WEB, COINVOLGIMENTO, CHECKPOINT_STEP, ACQUISIZIONE, VOLTE_CONTATTATI,
                    ULTIMA_CHIAMATA, PROSSIMA_CHIAMATA, OPERATORE_ID, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setClienteFields(statement, cliente);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateCliente(Cliente cliente) throws SQLException {
        String sql = """
                UPDATE CLIENTI SET
                    RAGIONE_SOCIALE = ?, TIPO_CLIENTE = ?, INTERESSAMENTO = ?, PARTITA_IVA = ?, CODICE_FISCALE = ?,
                    SITO_WEB = ?, COINVOLGIMENTO = ?, CHECKPOINT_STEP = ?, ACQUISIZIONE = ?, VOLTE_CONTATTATI = ?,
                    ULTIMA_CHIAMATA = ?, PROSSIMA_CHIAMATA = ?, OPERATORE_ID = ?, UPDATED_AT = CURRENT_TIMESTAMP
                WHERE ID = ?
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            statement.setString(1, cliente.ragioneSociale());
            statement.setString(2, cliente.tipoCliente());
            statement.setString(3, cliente.interessamento());
            statement.setString(4, cliente.partitaIva());
            statement.setString(5, cliente.codiceFiscale());
            statement.setString(6, cliente.sitoWeb());
            statement.setDouble(7, cliente.coinvolgimento());
            statement.setInt(8, cliente.checkpoint());
            setDate(statement, 9, cliente.acquisizione());
            statement.setInt(10, cliente.volteContattati());
            setDate(statement, 11, cliente.ultimaChiamata());
            setDate(statement, 12, cliente.prossimaChiamata());
            setUuid(statement, 13, cliente.operatoreId());
            setUuid(statement, 14, cliente.id());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Cliente> findClienteById(UUID id) throws SQLException {
        String sql = "SELECT * FROM CLIENTI WHERE ID = ?";

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCliente(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<Cliente> findAllClienti() throws SQLException {
        String sql = "SELECT * FROM CLIENTI ORDER BY RAGIONE_SOCIALE";
        List<Cliente> clienti = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clienti.add(mapCliente(resultSet));
            }
        }

        return clienti;
    }

    @Override
    public void deleteCliente(UUID id) throws SQLException {
        String sql = "DELETE FROM CLIENTI WHERE ID = ?";

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void saveContatto(ContattoCliente contatto) throws SQLException {
        String sql = """
                INSERT INTO CONTATTI_CLIENTE (
                    ID, CLIENTE_ID, NOME, COGNOME, RUOLO, PRINCIPALE, NOTE, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, contatto.id());
            setUuid(statement, 2, contatto.clienteId());
            statement.setString(3, contatto.nome());
            statement.setString(4, contatto.cognome());
            statement.setString(5, contatto.ruolo());
            statement.setInt(6, toSmallInt(contatto.principale()));
            statement.setString(7, contatto.note());
            setTimestamp(statement, 8, contatto.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public List<ContattoCliente> findContattiByClienteId(UUID clienteId) throws SQLException {
        String sql = "SELECT * FROM CONTATTI_CLIENTE WHERE CLIENTE_ID = ? ORDER BY PRINCIPALE DESC, COGNOME, NOME";
        List<ContattoCliente> contatti = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, clienteId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    contatti.add(mapContatto(resultSet));
                }
            }
        }

        return contatti;
    }

    @Override
    public void saveIndirizzo(IndirizzoCliente indirizzo) throws SQLException {
        String sql = """
                INSERT INTO INDIRIZZI_CLIENTE (
                    ID, CLIENTE_ID, PAESE, REGIONE, PROVINCIA, CITTA, INDIRIZZO,
                    NUMERO_CIVICO, CAP, PRINCIPALE, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, indirizzo.id());
            setUuid(statement, 2, indirizzo.clienteId());
            statement.setString(3, indirizzo.paese());
            statement.setString(4, indirizzo.regione());
            statement.setString(5, indirizzo.provincia());
            statement.setString(6, indirizzo.citta());
            statement.setString(7, indirizzo.indirizzo());
            statement.setString(8, indirizzo.numeroCivico());
            statement.setString(9, indirizzo.cap());
            statement.setInt(10, toSmallInt(indirizzo.principale()));
            setTimestamp(statement, 11, indirizzo.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public List<IndirizzoCliente> findIndirizziByClienteId(UUID clienteId) throws SQLException {
        String sql = "SELECT * FROM INDIRIZZI_CLIENTE WHERE CLIENTE_ID = ? ORDER BY PRINCIPALE DESC, CITTA, INDIRIZZO";
        List<IndirizzoCliente> indirizzi = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, clienteId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    indirizzi.add(mapIndirizzo(resultSet));
                }
            }
        }

        return indirizzi;
    }

    @Override
    public void saveTelefono(TelefonoCliente telefono) throws SQLException {
        String sql = """
                INSERT INTO TELEFONI_CLIENTE (
                    ID, CLIENTE_ID, CONTATTO_ID, NUMERO, TIPO, PRINCIPALE, DESCRIZIONE, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, telefono.id());
            setUuid(statement, 2, telefono.clienteId());
            setUuid(statement, 3, telefono.contattoId());
            statement.setString(4, telefono.numero());
            statement.setString(5, telefono.tipo());
            statement.setInt(6, toSmallInt(telefono.principale()));
            statement.setString(7, telefono.descrizione());
            setTimestamp(statement, 8, telefono.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public List<TelefonoCliente> findTelefoniByClienteId(UUID clienteId) throws SQLException {
        return findTelefoniByOwner("CLIENTE_ID", clienteId);
    }

    @Override
    public List<TelefonoCliente> findTelefoniByContattoId(UUID contattoId) throws SQLException {
        return findTelefoniByOwner("CONTATTO_ID", contattoId);
    }

    @Override
    public void saveEmail(EmailCliente email) throws SQLException {
        String sql = """
                INSERT INTO EMAIL_CLIENTE (
                    ID, CLIENTE_ID, CONTATTO_ID, EMAIL, TIPO, PRINCIPALE, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, email.id());
            setUuid(statement, 2, email.clienteId());
            setUuid(statement, 3, email.contattoId());
            statement.setString(4, email.email());
            statement.setString(5, email.tipo());
            statement.setInt(6, toSmallInt(email.principale()));
            setTimestamp(statement, 7, email.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public List<EmailCliente> findEmailByClienteId(UUID clienteId) throws SQLException {
        return findEmailByOwner("CLIENTE_ID", clienteId);
    }

    @Override
    public List<EmailCliente> findEmailByContattoId(UUID contattoId) throws SQLException {
        return findEmailByOwner("CONTATTO_ID", contattoId);
    }


    @Override
    public void saveAttivita(AttivitaCliente attivita) throws SQLException {
        String sql = """
                INSERT INTO ATTIVITA_CLIENTE (
                    ID, CLIENTE_ID, CONTATTO_ID, OPERATORE_ID, TIPO, DATA_ATTIVITA,
                    ESITO, PROSSIMA_ATTIVITA, NOTE, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, attivita.id());
            setUuid(statement, 2, attivita.clienteId());
            setUuid(statement, 3, attivita.contattoId());
            setUuid(statement, 4, attivita.operatoreId());
            statement.setString(5, attivita.tipo());
            setDate(statement, 6, attivita.dataAttivita());
            statement.setString(7, attivita.esito());
            setDate(statement, 8, attivita.prossimaAttivita());
            statement.setString(9, attivita.note());
            setTimestamp(statement, 10, attivita.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public List<AttivitaCliente> findAttivitaByClienteId(UUID clienteId) throws SQLException {
        String sql = "SELECT * FROM ATTIVITA_CLIENTE WHERE CLIENTE_ID = ? ORDER BY DATA_ATTIVITA DESC, CREATED_AT DESC";
        List<AttivitaCliente> attivita = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, clienteId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    attivita.add(mapAttivita(resultSet));
                }
            }
        }

        return attivita;
    }

    @Override
    public void saveNota(NotaCliente nota) throws SQLException {
        String sql = """
                INSERT INTO NOTE_CLIENTE (
                    ID, CLIENTE_ID, OPERATORE_ID, TESTO, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, nota.id());
            setUuid(statement, 2, nota.clienteId());
            setUuid(statement, 3, nota.operatoreId());
            statement.setString(4, nota.testo());
            setTimestamp(statement, 5, nota.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public List<NotaCliente> findNoteByClienteId(UUID clienteId) throws SQLException {
        String sql = "SELECT * FROM NOTE_CLIENTE WHERE CLIENTE_ID = ? ORDER BY CREATED_AT DESC";
        List<NotaCliente> note = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, clienteId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    note.add(mapNota(resultSet));
                }
            }
        }

        return note;
    }

    @Override
    public void saveOperatore(Operatore operatore) throws SQLException {
        String sql = """
                INSERT INTO OPERATORI (
                    ID, NOME, COGNOME, USERNAME, ATTIVO, UPDATED_AT
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, operatore.id());
            statement.setString(2, operatore.nome());
            statement.setString(3, operatore.cognome());
            statement.setString(4, operatore.username());
            statement.setInt(5, toSmallInt(operatore.attivo()));
            setTimestamp(statement, 6, operatore.updatedAt());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<Operatore> findOperatoreById(UUID id) throws SQLException {
        String sql = "SELECT * FROM OPERATORI WHERE ID = ?";

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapOperatore(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    private Connection connection() {
        return database.getConnection();
    }

    private List<TelefonoCliente> findTelefoniByOwner(String ownerColumn, UUID ownerId) throws SQLException {
        String sql = "SELECT * FROM TELEFONI_CLIENTE WHERE " + ownerColumn + " = ? ORDER BY PRINCIPALE DESC, TIPO, NUMERO";
        List<TelefonoCliente> telefoni = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, ownerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    telefoni.add(mapTelefono(resultSet));
                }
            }
        }

        return telefoni;
    }

    private List<EmailCliente> findEmailByOwner(String ownerColumn, UUID ownerId) throws SQLException {
        String sql = "SELECT * FROM EMAIL_CLIENTE WHERE " + ownerColumn + " = ? ORDER BY PRINCIPALE DESC, TIPO, EMAIL";
        List<EmailCliente> emails = new ArrayList<>();

        try (PreparedStatement statement = connection().prepareStatement(sql)) {
            setUuid(statement, 1, ownerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    emails.add(mapEmail(resultSet));
                }
            }
        }

        return emails;
    }

    private void setClienteFields(PreparedStatement statement, Cliente cliente) throws SQLException {
        setUuid(statement, 1, cliente.id());
        statement.setString(2, cliente.ragioneSociale());
        statement.setString(3, cliente.tipoCliente());
        statement.setString(4, cliente.interessamento());
        statement.setString(5, cliente.partitaIva());
        statement.setString(6, cliente.codiceFiscale());
        statement.setString(7, cliente.sitoWeb());
        statement.setDouble(8, cliente.coinvolgimento());
        statement.setInt(9, cliente.checkpoint());
        setDate(statement, 10, cliente.acquisizione());
        statement.setInt(11, cliente.volteContattati());
        setDate(statement, 12, cliente.ultimaChiamata());
        setDate(statement, 13, cliente.prossimaChiamata());
        setUuid(statement, 14, cliente.operatoreId());
        setTimestamp(statement, 15, cliente.updatedAt());
    }

    private Cliente mapCliente(ResultSet resultSet) throws SQLException {
        return new Cliente(
                getUuid(resultSet, "ID"),
                resultSet.getString("RAGIONE_SOCIALE"),
                resultSet.getString("TIPO_CLIENTE"),
                resultSet.getString("INTERESSAMENTO"),
                resultSet.getString("PARTITA_IVA"),
                resultSet.getString("CODICE_FISCALE"),
                resultSet.getString("SITO_WEB"),
                resultSet.getDouble("COINVOLGIMENTO"),
                resultSet.getInt("CHECKPOINT_STEP"),
                getLocalDate(resultSet, "ACQUISIZIONE"),
                resultSet.getInt("VOLTE_CONTATTATI"),
                getLocalDate(resultSet, "ULTIMA_CHIAMATA"),
                getLocalDate(resultSet, "PROSSIMA_CHIAMATA"),
                getUuid(resultSet, "OPERATORE_ID"),
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private ContattoCliente mapContatto(ResultSet resultSet) throws SQLException {
        return new ContattoCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                resultSet.getString("NOME"),
                resultSet.getString("COGNOME"),
                resultSet.getString("RUOLO"),
                resultSet.getInt("PRINCIPALE") == 1,
                resultSet.getString("NOTE"),
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private IndirizzoCliente mapIndirizzo(ResultSet resultSet) throws SQLException {
        return new IndirizzoCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                resultSet.getString("PAESE"),
                resultSet.getString("REGIONE"),
                resultSet.getString("PROVINCIA"),
                resultSet.getString("CITTA"),
                resultSet.getString("INDIRIZZO"),
                resultSet.getString("NUMERO_CIVICO"),
                resultSet.getString("CAP"),
                resultSet.getInt("PRINCIPALE") == 1,
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private TelefonoCliente mapTelefono(ResultSet resultSet) throws SQLException {
        return new TelefonoCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                getUuid(resultSet, "CONTATTO_ID"),
                resultSet.getString("NUMERO"),
                resultSet.getString("TIPO"),
                resultSet.getInt("PRINCIPALE") == 1,
                resultSet.getString("DESCRIZIONE"),
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private EmailCliente mapEmail(ResultSet resultSet) throws SQLException {
        return new EmailCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                getUuid(resultSet, "CONTATTO_ID"),
                resultSet.getString("EMAIL"),
                resultSet.getString("TIPO"),
                resultSet.getInt("PRINCIPALE") == 1,
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }


    private AttivitaCliente mapAttivita(ResultSet resultSet) throws SQLException {
        return new AttivitaCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                getUuid(resultSet, "CONTATTO_ID"),
                getUuid(resultSet, "OPERATORE_ID"),
                resultSet.getString("TIPO"),
                getLocalDate(resultSet, "DATA_ATTIVITA"),
                resultSet.getString("ESITO"),
                getLocalDate(resultSet, "PROSSIMA_ATTIVITA"),
                resultSet.getString("NOTE"),
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private NotaCliente mapNota(ResultSet resultSet) throws SQLException {
        return new NotaCliente(
                getUuid(resultSet, "ID"),
                getUuid(resultSet, "CLIENTE_ID"),
                getUuid(resultSet, "OPERATORE_ID"),
                resultSet.getString("TESTO"),
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private Operatore mapOperatore(ResultSet resultSet) throws SQLException {
        return new Operatore(
                getUuid(resultSet, "ID"),
                resultSet.getString("NOME"),
                resultSet.getString("COGNOME"),
                resultSet.getString("USERNAME"),
                resultSet.getInt("ATTIVO") == 1,
                getLocalDateTime(resultSet, "CREATED_AT"),
                getLocalDateTime(resultSet, "UPDATED_AT")
        );
    }

    private List<String> readSchemaStatements() throws SQLException {
        try (InputStream inputStream = DerbyClientiRepository.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (inputStream == null) {
                throw new SQLException("Schema resource non trovato: " + SCHEMA_RESOURCE);
            }

            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String withoutComment = line.split("--", 2)[0].trim();
                    if (!withoutComment.isEmpty()) {
                        sql.append(withoutComment).append('\n');
                    }
                }
            }

            List<String> statements = new ArrayList<>();
            for (String statement : sql.toString().split(";")) {
                String trimmedStatement = statement.trim();
                if (!trimmedStatement.isEmpty()) {
                    statements.add(trimmedStatement);
                }
            }
            return statements;
        } catch (IOException e) {
            throw new SQLException("Errore lettura schema clienti.", e);
        }
    }

    private boolean isAlreadyCreatedError(SQLException e) {
        String sqlState = e.getSQLState();
        return "X0Y32".equals(sqlState) || "X0Y68".equals(sqlState);
    }

    private void setUuid(PreparedStatement statement, int index, UUID value) throws SQLException {
        statement.setString(index, value == null ? null : value.toString());
    }

    private UUID getUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private void setDate(PreparedStatement statement, int index, LocalDate value) throws SQLException {
        statement.setDate(index, value == null ? null : Date.valueOf(value));
    }

    private LocalDate getLocalDate(ResultSet resultSet, String column) throws SQLException {
        Date value = resultSet.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private void setTimestamp(PreparedStatement statement, int index, LocalDateTime value) throws SQLException {
        statement.setTimestamp(index, value == null ? null : Timestamp.valueOf(value));
    }

    private LocalDateTime getLocalDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? null : value.toLocalDateTime();
    }

    private int toSmallInt(boolean value) {
        return value ? 1 : 0;
    }
}

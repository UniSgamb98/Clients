package com.example.clients.core.database;

import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.EditProfileDraft;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoClienteEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoClienteItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FresatoreClienteEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FresatoreClienteItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.clients.core.database.service.CurrentOperatoreService.DEFAULT_OPERATORE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedaClienteServiceResourcesIntegrationTest {

    private static Database database;

    @BeforeAll
    static void startDatabase() throws SQLException {
        database = new Database(DriverManager.getConnection("jdbc:derby:memory:forni-verification-" + UUID.randomUUID() + ";create=true"));
        new SchemaInitializer(database).initialize();
        insertDefaultOperatore();
    }

    @AfterAll
    static void stopDatabase() {
        if (database != null) {
            database.stop();
        }
    }

    @Test
    void reusesAnExistingCatalogCombination() throws SQLException {
        UUID clienteId = insertCliente("Cliente combinazione esistente");
        UUID existingFornoId = insertForno("Pressatura", "2020", "Marca esistente", "Modello esistente");
        SchedaClienteService service = loadService(clienteId);

        List<FornoClienteItem> saved = service.saveForniEdit(List.of(input("Pressatura", "2020", "Marca esistente", "Modello esistente", "Nota")));

        assertEquals(1, saved.size());
        assertEquals(existingFornoId, saved.getFirst().fornoId());
        assertEquals(1, countForni("Marca esistente", "Modello esistente"));
    }

    @Test
    void createsANewCatalogCombination() throws SQLException {
        UUID clienteId = insertCliente("Cliente nuovo forno");
        SchedaClienteService service = loadService(clienteId);

        List<FornoClienteItem> saved = service.saveForniEdit(List.of(input("Microonde", "2025", "Nuova marca", "Nuovo modello", "Prima installazione")));

        assertEquals(1, saved.size());
        assertEquals(1, countForni("Nuova marca", "Nuovo modello"));
        assertEquals("Prima installazione", saved.getFirst().nota());
    }

    @Test
    void updatesTheAssociationNote() {
        UUID clienteId = insertCliente("Cliente modifica nota");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(input("Tecnologia nota", "2021", "Marca nota", "Modello nota", "Nota originale")));
        FornoClienteEditInput existing = service.startForniEdit().getFirst();

        List<FornoClienteItem> saved = service.saveForniEdit(List.of(new FornoClienteEditInput(
                existing.id(), existing.fornoId(), existing.tecnologia(), existing.anno(), existing.marca(), existing.modello(), "Nota aggiornata")));

        assertEquals("Nota aggiornata", saved.getFirst().nota());
        assertEquals(1, countAssociations(clienteId));
    }

    @Test
    void addsAndRemovesMultipleForni() {
        UUID clienteId = insertCliente("Cliente più forni");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(
                input("Tecnologia A", "2018", "Marca A", "Modello A", ""),
                input("Tecnologia B", "2019", "Marca B", "Modello B", ""),
                input("Tecnologia C", "2020", "Marca C", "Modello C", "")));
        List<FornoClienteEditInput> current = service.startForniEdit();

        List<FornoClienteItem> saved = service.saveForniEdit(List.of(current.get(0), current.get(2)));

        assertEquals(2, saved.size());
        assertEquals(2, countAssociations(clienteId));
        assertFalse(saved.stream().anyMatch(forno -> "Marca B".equals(forno.marca())));
    }

    @Test
    void cancelDoesNotPersistDraftChanges() {
        UUID clienteId = insertCliente("Cliente annullamento");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(input("Tecnologia annulla", "2017", "Marca annulla", "Modello annulla", "Originale")));
        List<FornoClienteEditInput> draft = service.startForniEdit();
        FornoClienteEditInput original = draft.getFirst();
        FornoClienteEditInput changed = new FornoClienteEditInput(original.id(), original.fornoId(), original.tecnologia(), original.anno(), original.marca(), original.modello(), "Non salvata");

        List<FornoClienteItem> restored = service.cancelForniEdit();

        assertEquals("Originale", restored.getFirst().nota());
        assertEquals("Non salvata", changed.nota());
        assertEquals("Originale", associationNote(clienteId));
    }

    @Test
    void databaseErrorRollsBackCatalogAndAssociations() throws SQLException {
        UUID clienteId = insertCliente("Cliente rollback");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(input("Tecnologia stabile", "2016", "Marca stabile", "Modello stabile", "Nota stabile")));

        RuntimeException error = assertThrows(RuntimeException.class, () -> service.saveForniEdit(List.of(
                input("Tecnologia errore", "2026", "Marca rollback", "Modello rollback", "x".repeat(501)))));

        assertTrue(error.getMessage().contains("Salvataggio forni"));
        assertEquals(1, countAssociations(clienteId));
        assertEquals("Nota stabile", associationNote(clienteId));
        assertEquals(0, countForni("Marca rollback", "Modello rollback"));
    }

    @Test
    void editorCanBeOpenedAndCancelledRepeatedly() {
        UUID clienteId = insertCliente("Cliente aperture ripetute");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(input("Tecnologia ripetuta", "2022", "Marca ripetuta", "Modello ripetuto", "")));

        for (int attempt = 0; attempt < 5; attempt++) {
            assertEquals(1, service.startForniEdit().size());
            assertEquals(1, service.cancelForniEdit().size());
        }

        assertEquals(1, countAssociations(clienteId));
    }

    @Test
    void savingForniPreservesOtherClientData() throws SQLException {
        UUID clienteId = insertCliente("Cliente dati invariati");
        insertTelefono(clienteId, "+39 0123456789");
        SchedaClienteService service = loadService(clienteId);

        service.saveForniEdit(List.of(input("Tecnologia conservazione", "2024", "Marca conservazione", "Modello conservazione", "")));

        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "SELECT RAGIONE_SOCIALE, TIPO_CLIENTE, STATO_TRATTATIVA, PARTITA_IVA, CODICE_FISCALE, ACQUISIZIONE FROM CLIENTI WHERE ID = ?")) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("Cliente dati invariati", resultSet.getString("RAGIONE_SOCIALE"));
                assertEquals("Laboratorio", resultSet.getString("TIPO_CLIENTE"));
                assertEquals("Attivo", resultSet.getString("STATO_TRATTATIVA"));
                assertEquals("IT12345678901", resultSet.getString("PARTITA_IVA"));
                assertEquals("RSSMRA80A01H501U", resultSet.getString("CODICE_FISCALE"));
                assertEquals(Date.valueOf(LocalDate.of(2020, 1, 15)), resultSet.getDate("ACQUISIZIONE"));
            }
        }
        assertEquals(1, countTelefoni(clienteId));
    }

    @Test
    void reusesAnExistingFresatoreCatalogCombination() throws SQLException {
        UUID clienteId = insertCliente("Cliente fresatore esistente");
        UUID existingFresatoreId = insertFresatore("Marca fresatore esistente", "Modello fresatore esistente");
        SchedaClienteService service = loadService(clienteId);

        List<FresatoreClienteItem> saved = service.saveFresatoriEdit(List.of(fresatoreInput(
                "Marca fresatore esistente", "Modello fresatore esistente", "Nota fresatore")));

        assertEquals(1, saved.size());
        assertEquals(existingFresatoreId, saved.getFirst().fresatoreId());
        assertEquals(1, countFresatori("Marca fresatore esistente", "Modello fresatore esistente"));
    }

    @Test
    void createsANewFresatoreAndUpdatesTheAssociationNote() throws SQLException {
        UUID clienteId = insertCliente("Cliente nuovo fresatore");
        SchedaClienteService service = loadService(clienteId);
        service.saveFresatoriEdit(List.of(fresatoreInput("Nuova marca fresatore", "Nuovo modello fresatore", "Nota originale")));
        FresatoreClienteEditInput existing = service.startFresatoriEdit().getFirst();

        List<FresatoreClienteItem> saved = service.saveFresatoriEdit(List.of(new FresatoreClienteEditInput(
                existing.id(), existing.fresatoreId(), existing.marca(), existing.modello(), "Nota aggiornata")));

        assertEquals(1, countFresatori("Nuova marca fresatore", "Nuovo modello fresatore"));
        assertEquals("Nota aggiornata", saved.getFirst().nota());
        assertEquals(1, countFresatoreAssociations(clienteId));
    }

    @Test
    void addsAndRemovesMultipleFresatori() {
        UUID clienteId = insertCliente("Cliente più fresatori");
        SchedaClienteService service = loadService(clienteId);
        service.saveFresatoriEdit(List.of(
                fresatoreInput("Marca fresatore A", "Modello fresatore A", ""),
                fresatoreInput("Marca fresatore B", "Modello fresatore B", ""),
                fresatoreInput("Marca fresatore C", "Modello fresatore C", "")));
        List<FresatoreClienteEditInput> current = service.startFresatoriEdit();

        List<FresatoreClienteItem> saved = service.saveFresatoriEdit(List.of(current.get(0), current.get(2)));

        assertEquals(2, saved.size());
        assertEquals(2, countFresatoreAssociations(clienteId));
        assertFalse(saved.stream().anyMatch(fresatore -> "Marca fresatore B".equals(fresatore.marca())));
    }

    @Test
    void fresatoreEditorCanBeCancelledAndReopened() {
        UUID clienteId = insertCliente("Cliente annullamento fresatore");
        SchedaClienteService service = loadService(clienteId);
        service.saveFresatoriEdit(List.of(fresatoreInput("Marca annulla fresatore", "Modello annulla fresatore", "Originale")));

        for (int attempt = 0; attempt < 5; attempt++) {
            FresatoreClienteEditInput draft = service.startFresatoriEdit().getFirst();
            assertEquals("Originale", draft.nota());
            assertEquals("Originale", service.cancelFresatoriEdit().getFirst().nota());
        }

        assertEquals("Originale", fresatoreAssociationNote(clienteId));
    }

    @Test
    void fresatoreDatabaseErrorRollsBackCatalogAndAssociations() throws SQLException {
        UUID clienteId = insertCliente("Cliente rollback fresatore");
        SchedaClienteService service = loadService(clienteId);
        service.saveFresatoriEdit(List.of(fresatoreInput("Marca stabile fresatore", "Modello stabile fresatore", "Nota stabile")));

        RuntimeException error = assertThrows(RuntimeException.class, () -> service.saveFresatoriEdit(List.of(
                fresatoreInput("Marca rollback fresatore", "Modello rollback fresatore", "x".repeat(501)))));

        assertTrue(error.getMessage().contains("Salvataggio fresatori"));
        assertEquals(1, countFresatoreAssociations(clienteId));
        assertEquals("Nota stabile", fresatoreAssociationNote(clienteId));
        assertEquals(0, countFresatori("Marca rollback fresatore", "Modello rollback fresatore"));
    }

    @Test
    void savingFresatoriPreservesClientDataAndForni() throws SQLException {
        UUID clienteId = insertCliente("Cliente conservazione fresatori");
        insertTelefono(clienteId, "+39 0987654321");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(input("Tecnologia conservata", "2023", "Marca forno conservata", "Modello forno conservato", "")));

        service.saveFresatoriEdit(List.of(fresatoreInput("Marca conservazione fresatore", "Modello conservazione fresatore", "")));

        assertEquals(1, countAssociations(clienteId));
        assertEquals(1, countTelefoni(clienteId));
        assertEquals("Cliente conservazione fresatori", clientName(clienteId));
    }

    @Test
    void globalProfileEditDoesNotSaveOrRemoveResources() {
        UUID clienteId = insertCliente("Cliente modifica globale");
        SchedaClienteService service = loadService(clienteId);
        service.saveForniEdit(List.of(input("Tecnologia globale", "2022", "Marca forno globale", "Modello forno globale", "Nota forno")));
        service.saveFresatoriEdit(List.of(fresatoreInput("Marca fresatore globale", "Modello fresatore globale", "Nota fresatore")));
        EditProfileDraft current = service.startEdit();
        EditProfileDraft updated = new EditProfileDraft(
                "Cliente modifica globale aggiornato",
                current.tipoCliente(),
                current.statoTrattativa(),
                current.coinvolgimento(),
                current.partitaIva(),
                current.codiceFiscale(),
                current.acquisizione(),
                current.telefoni(),
                current.email(),
                current.sitiWeb(),
                current.indirizzi(),
                current.contatti(),
                current.interazioni());

        SchedaClienteService.ClienteProfile saved = service.saveEdit(updated);

        assertEquals("Cliente modifica globale aggiornato", saved.ragioneSociale());
        assertEquals(1, saved.forni().size());
        assertEquals("Nota forno", saved.forni().getFirst().nota());
        assertEquals(1, saved.fresatori().size());
        assertEquals("Nota fresatore", saved.fresatori().getFirst().nota());
        assertEquals(1, countAssociations(clienteId));
        assertEquals(1, countFresatoreAssociations(clienteId));
    }

    private SchedaClienteService loadService(UUID clienteId) {
        SchedaClienteService service = new SchedaClienteService(database);
        service.loadProfile(clienteId);
        return service;
    }

    private static void insertDefaultOperatore() throws SQLException {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "INSERT INTO OPERATORI (ID, NOME, COGNOME, USERNAME) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, DEFAULT_OPERATORE_ID.toString());
            statement.setString(2, "Operatore");
            statement.setString(3, "Test");
            statement.setString(4, "operatore-test");
            statement.executeUpdate();
        }
    }

    private UUID insertCliente(String ragioneSociale) {
        UUID id = UUID.randomUUID();
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "INSERT INTO CLIENTI (ID, RAGIONE_SOCIALE, TIPO_CLIENTE, STATO_TRATTATIVA, COINVOLGIMENTO, PARTITA_IVA, CODICE_FISCALE, ACQUISIZIONE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, id.toString());
            statement.setString(2, ragioneSociale);
            statement.setString(3, "Laboratorio");
            statement.setString(4, "Attivo");
            statement.setInt(5, 4);
            statement.setString(6, "IT12345678901");
            statement.setString(7, "RSSMRA80A01H501U");
            statement.setDate(8, Date.valueOf(LocalDate.of(2020, 1, 15)));
            statement.executeUpdate();
            return id;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private UUID insertForno(String tecnologia, String anno, String marca, String modello) throws SQLException {
        UUID id = UUID.randomUUID();
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "INSERT INTO FORNI (ID, TECNOLOGIA, ANNO, MARCA, MODELLO) VALUES (?, ?, ?, ?, ?)")) {
            statement.setString(1, id.toString());
            statement.setString(2, tecnologia);
            statement.setString(3, anno);
            statement.setString(4, marca);
            statement.setString(5, modello);
            statement.executeUpdate();
        }
        return id;
    }

    private UUID insertFresatore(String marca, String modello) throws SQLException {
        UUID id = UUID.randomUUID();
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "INSERT INTO FRESATORI (ID, MARCA, MODELLO) VALUES (?, ?, ?)")) {
            statement.setString(1, id.toString());
            statement.setString(2, marca);
            statement.setString(3, modello);
            statement.executeUpdate();
        }
        return id;
    }

    private void insertTelefono(UUID clienteId, String value) throws SQLException {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "INSERT INTO TELEFONI_CLIENTE (ID, CLIENTE_ID, DESCRIZIONE) VALUES (?, ?, ?)")) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, clienteId.toString());
            statement.setString(3, value);
            statement.executeUpdate();
        }
    }

    private FornoClienteEditInput input(String tecnologia, String anno, String marca, String modello, String nota) {
        return new FornoClienteEditInput(null, null, tecnologia, anno, marca, modello, nota);
    }

    private FresatoreClienteEditInput fresatoreInput(String marca, String modello, String nota) {
        return new FresatoreClienteEditInput(null, null, marca, modello, nota);
    }

    private int countForni(String marca, String modello) throws SQLException {
        return count("SELECT COUNT(*) FROM FORNI WHERE MARCA = ? AND MODELLO = ?", marca, modello);
    }

    private int countAssociations(UUID clienteId) {
        try {
            return count("SELECT COUNT(*) FROM CLIENTI_FORNI WHERE CLIENTE_ID = ?", clienteId.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int countFresatori(String marca, String modello) throws SQLException {
        return count("SELECT COUNT(*) FROM FRESATORI WHERE MARCA = ? AND MODELLO = ?", marca, modello);
    }

    private int countFresatoreAssociations(UUID clienteId) {
        try {
            return count("SELECT COUNT(*) FROM CLIENTI_FRESATORI WHERE CLIENTE_ID = ?", clienteId.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int countTelefoni(UUID clienteId) throws SQLException {
        return count("SELECT COUNT(*) FROM TELEFONI_CLIENTE WHERE CLIENTE_ID = ?", clienteId.toString());
    }

    private int count(String sql, String... parameters) throws SQLException {
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            for (int index = 0; index < parameters.length; index++) {
                statement.setString(index + 1, parameters[index]);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private String associationNote(UUID clienteId) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "SELECT NOTA FROM CLIENTI_FORNI WHERE CLIENTE_ID = ?")) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString("NOTA");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String fresatoreAssociationNote(UUID clienteId) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "SELECT NOTA FROM CLIENTI_FRESATORI WHERE CLIENTE_ID = ?")) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString("NOTA");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String clientName(UUID clienteId) throws SQLException {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                "SELECT RAGIONE_SOCIALE FROM CLIENTI WHERE ID = ?")) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString("RAGIONE_SOCIALE");
            }
        }
    }
}

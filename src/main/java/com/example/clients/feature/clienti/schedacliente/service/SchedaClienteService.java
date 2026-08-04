package com.example.clients.feature.clienti.schedacliente.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.SchemaInitializer;
import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.model.ContattoCliente;
import com.example.clients.core.database.model.EmailCliente;
import com.example.clients.core.database.model.IndirizzoCliente;
import com.example.clients.core.database.model.Interazione;
import com.example.clients.core.database.model.SitoWebCliente;
import com.example.clients.core.database.model.TelefonoCliente;
import com.example.clients.core.database.query.ClienteProfileQuery;
import com.example.clients.core.database.query.StatoTrattativaQuery;
import com.example.clients.core.database.query.TipoClienteQuery;
import com.example.clients.core.database.query.ClienteProfileQuery.AddressRecord;
import com.example.clients.core.database.query.ClienteProfileQuery.ClienteProfileRecord;
import com.example.clients.core.database.query.ClienteProfileQuery.ContactRecord;
import com.example.clients.core.database.query.ClienteProfileQuery.TimelineRecord;
import com.example.clients.core.database.query.ClienteProfileQuery.ValueRecord;
import com.example.clients.core.database.query.derby.DerbyClienteProfileQuery;
import com.example.clients.core.database.query.derby.DerbyStatoTrattativaQuery;
import com.example.clients.core.database.query.derby.DerbyTipoClienteQuery;
import com.example.clients.core.database.service.ClientePersistenceService;
import com.example.clients.core.database.service.CurrentOperatoreService;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchedaClienteService {

    private final ClienteProfileQuery profileQuery;
    private final ClientePersistenceService persistenceService;
    private final CurrentOperatoreService currentOperatoreService;
    private final TipoClienteQuery tipoClienteQuery;
    private final StatoTrattativaQuery statoTrattativaQuery;
    private final Database database;
    private final SchemaInitializer schemaInitializer;
    private ClienteProfile currentProfile;
    private EditProfileDraft editingDraft;
    private UUID currentClienteId;
    private TimelineFilter currentFilter = TimelineFilter.ALL;

    public SchedaClienteService(Database database) {
        this(new DerbyClienteProfileQuery(database), new ClientePersistenceService(database), new CurrentOperatoreService(), new DerbyTipoClienteQuery(database), new DerbyStatoTrattativaQuery(database), database);
    }

    public SchedaClienteService(
            ClienteProfileQuery profileQuery,
            ClientePersistenceService persistenceService,
            CurrentOperatoreService currentOperatoreService
    ) {
        this(profileQuery, persistenceService, currentOperatoreService, null, null);
    }

    public SchedaClienteService(
            ClienteProfileQuery profileQuery,
            ClientePersistenceService persistenceService,
            CurrentOperatoreService currentOperatoreService,
            TipoClienteQuery tipoClienteQuery
    ) {
        this(profileQuery, persistenceService, currentOperatoreService, tipoClienteQuery, null);
    }

    public SchedaClienteService(
            ClienteProfileQuery profileQuery,
            ClientePersistenceService persistenceService,
            CurrentOperatoreService currentOperatoreService,
            TipoClienteQuery tipoClienteQuery,
            StatoTrattativaQuery statoTrattativaQuery
    ) {
        this(profileQuery, persistenceService, currentOperatoreService, tipoClienteQuery, statoTrattativaQuery, null);
    }

    private SchedaClienteService(
            ClienteProfileQuery profileQuery,
            ClientePersistenceService persistenceService,
            CurrentOperatoreService currentOperatoreService,
            TipoClienteQuery tipoClienteQuery,
            StatoTrattativaQuery statoTrattativaQuery,
            Database database
    ) {
        this.profileQuery = profileQuery;
        this.persistenceService = persistenceService;
        this.currentOperatoreService = currentOperatoreService;
        this.tipoClienteQuery = tipoClienteQuery;
        this.statoTrattativaQuery = statoTrattativaQuery;
        this.database = database;
        this.schemaInitializer = database == null ? null : new SchemaInitializer(database);
    }

    public List<String> getTipiCliente() {
        if (tipoClienteQuery == null) {
            return List.of();
        }

        return tipoClienteQuery.findAll().stream()
                .map(TipoClienteQuery.TipoClienteRecord::nome)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    public List<String> getStatiTrattativa() {
        if (statoTrattativaQuery == null) {
            return List.of();
        }

        return statoTrattativaQuery.findAll().stream()
                .map(StatoTrattativaQuery.StatoTrattativaRecord::nome)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    public ClienteProfile loadProfile(UUID clienteId) {
        currentClienteId = clienteId;
        currentProfile = clienteId == null
                ? emptyProfile()
                : profileQuery.findById(clienteId, currentOperatoreService.currentOperatoreId())
                .map(this::toClienteProfile)
                .orElseGet(this::emptyProfile);
        editingDraft = null;
        currentFilter = TimelineFilter.ALL;
        return filteredProfile();
    }

    private ClienteProfile toClienteProfile(ClienteProfileRecord record) {
        return new ClienteProfile(
                record.clienteId(),
                record.ragioneSociale(),
                record.tipoCliente(),
                record.statoTrattativa(),
                record.coinvolgimento(),
                record.partitaIva(),
                record.codiceFiscale(),
                record.acquisizione(),
                record.favorite(),
                toValueItems(record.telefoni()),
                toValueItems(record.email()),
                toValueItems(record.sitiWeb()),
                toAddressItems(record.indirizzi()),
                toContactItems(record.contatti()),
                findClienteForni(record.clienteId()),
                findClienteFresatori(record.clienteId()),
                record.timeline().stream()
                        .map(this::toInteractionPreview)
                        .toList()
        );
    }


    public List<FornoCatalogItem> getForniCatalog() {
        if (database == null) {
            return List.of();
        }
        initializeSchema();
        String sql = "SELECT ID, TECNOLOGIA, ANNO, MARCA, MODELLO FROM FORNI ORDER BY MARCA, MODELLO, TECNOLOGIA, ANNO";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<FornoCatalogItem> values = new ArrayList<>();
            while (resultSet.next()) {
                values.add(new FornoCatalogItem(
                        getUuid(resultSet, "ID"),
                        cleanResult(resultSet.getString("TECNOLOGIA")),
                        cleanResult(resultSet.getString("ANNO")),
                        cleanResult(resultSet.getString("MARCA")),
                        cleanResult(resultSet.getString("MODELLO"))));
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException("Caricamento catalogo forni non riuscito.", e);
        }
    }

    public List<FresatoreCatalogItem> getFresatoriCatalog() {
        if (database == null) {
            return List.of();
        }
        initializeSchema();
        String sql = "SELECT ID, MARCA, MODELLO FROM FRESATORI ORDER BY MARCA, MODELLO";
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<FresatoreCatalogItem> values = new ArrayList<>();
            while (resultSet.next()) {
                values.add(new FresatoreCatalogItem(
                        getUuid(resultSet, "ID"),
                        cleanResult(resultSet.getString("MARCA")),
                        cleanResult(resultSet.getString("MODELLO"))));
            }
            return values;
        } catch (SQLException e) {
            throw new RuntimeException("Caricamento catalogo fresatori non riuscito.", e);
        }
    }

    private List<FornoClienteItem> findClienteForni(UUID clienteId) {
        if (database == null || clienteId == null) {
            return List.of();
        }
        initializeSchema();
        String sql = """
                SELECT CF.ID AS CLIENTE_FORNO_ID, F.ID AS FORNO_ID, F.TECNOLOGIA, F.ANNO, F.MARCA, F.MODELLO, CF.NOTA
                FROM CLIENTI_FORNI CF
                JOIN FORNI F ON F.ID = CF.FORNO_ID
                WHERE CF.CLIENTE_ID = ?
                ORDER BY F.MARCA, F.MODELLO, F.TECNOLOGIA, F.ANNO
                """;
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<FornoClienteItem> values = new ArrayList<>();
                while (resultSet.next()) {
                    values.add(new FornoClienteItem(
                            getUuid(resultSet, "CLIENTE_FORNO_ID"),
                            getUuid(resultSet, "FORNO_ID"),
                            cleanResult(resultSet.getString("TECNOLOGIA")),
                            cleanResult(resultSet.getString("ANNO")),
                            cleanResult(resultSet.getString("MARCA")),
                            cleanResult(resultSet.getString("MODELLO")),
                            cleanResult(resultSet.getString("NOTA"))));
                }
                return values;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Caricamento forni cliente non riuscito.", e);
        }
    }

    private void saveClienteForni(List<FornoClienteEditInput> forni) {
        if (database == null || currentClienteId == null) {
            return;
        }
        initializeSchema();
        try (PreparedStatement delete = database.getConnection().prepareStatement("DELETE FROM CLIENTI_FORNI WHERE CLIENTE_ID = ?");
             PreparedStatement insert = database.getConnection().prepareStatement("INSERT INTO CLIENTI_FORNI (ID, CLIENTE_ID, FORNO_ID, NOTA, UPDATED_AT) VALUES (?, ?, ?, ?, ?)")) {
            delete.setString(1, currentClienteId.toString());
            delete.executeUpdate();
            LocalDateTime now = LocalDateTime.now();
            for (FornoClienteEditInput forno : forni) {
                FornoClienteEditInput cleanForno = cleanForno(forno);
                if (!hasFornoData(cleanForno)) {
                    continue;
                }
                UUID fornoId = findOrCreateForno(cleanForno);
                insert.setString(1, idOrNew(cleanForno.id()).toString());
                insert.setString(2, currentClienteId.toString());
                insert.setString(3, fornoId.toString());
                insert.setString(4, nullableClean(cleanForno.nota()));
                insert.setTimestamp(5, Timestamp.valueOf(now));
                insert.addBatch();
            }
            insert.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Salvataggio forni cliente non riuscito.", e);
        }
    }

    private UUID findOrCreateForno(FornoClienteEditInput forno) throws SQLException {
        String findSql = "SELECT ID FROM FORNI WHERE TECNOLOGIA = ? AND COALESCE(ANNO, '') = ? AND MARCA = ? AND MODELLO = ?";
        try (PreparedStatement find = database.getConnection().prepareStatement(findSql)) {
            bindFornoIdentity(find, forno);
            try (ResultSet resultSet = find.executeQuery()) {
                if (resultSet.next()) {
                    return getUuid(resultSet, "ID");
                }
            }
        }

        UUID fornoId = UUID.randomUUID();
        try (PreparedStatement insert = database.getConnection().prepareStatement("INSERT INTO FORNI (ID, TECNOLOGIA, ANNO, MARCA, MODELLO) VALUES (?, ?, ?, ?, ?)")) {
            insert.setString(1, fornoId.toString());
            bindFornoIdentity(insert, forno, 2);
            insert.executeUpdate();
        }
        return fornoId;
    }

    private void bindFornoIdentity(PreparedStatement statement, FornoClienteEditInput forno) throws SQLException {
        bindFornoIdentity(statement, forno, 1);
    }

    private void bindFornoIdentity(PreparedStatement statement, FornoClienteEditInput forno, int startIndex) throws SQLException {
        statement.setString(startIndex, forno.tecnologia());
        statement.setString(startIndex + 1, forno.anno());
        statement.setString(startIndex + 2, forno.marca());
        statement.setString(startIndex + 3, forno.modello());
    }

    private FornoClienteEditInput cleanForno(FornoClienteEditInput forno) {
        return new FornoClienteEditInput(
                forno.id(),
                forno.fornoId(),
                normalize(forno.tecnologia()),
                normalize(forno.anno()),
                normalize(forno.marca()),
                normalize(forno.modello()),
                normalize(forno.nota()));
    }

    private boolean hasFornoData(FornoClienteEditInput forno) {
        return !forno.tecnologia().isBlank()
                || !forno.anno().isBlank()
                || !forno.marca().isBlank()
                || !forno.modello().isBlank();
    }

    private List<FresatoreClienteItem> findClienteFresatori(UUID clienteId) {
        if (database == null || clienteId == null) {
            return List.of();
        }
        initializeSchema();
        String sql = """
                SELECT CF.ID AS CLIENTE_FRESATORE_ID, F.ID AS FRESATORE_ID, F.MARCA, F.MODELLO, CF.NOTA
                FROM CLIENTI_FRESATORI CF
                JOIN FRESATORI F ON F.ID = CF.FRESATORE_ID
                WHERE CF.CLIENTE_ID = ?
                ORDER BY F.MARCA, F.MODELLO
                """;
        try (PreparedStatement statement = database.getConnection().prepareStatement(sql)) {
            statement.setString(1, clienteId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<FresatoreClienteItem> values = new ArrayList<>();
                while (resultSet.next()) {
                    values.add(new FresatoreClienteItem(
                            getUuid(resultSet, "CLIENTE_FRESATORE_ID"),
                            getUuid(resultSet, "FRESATORE_ID"),
                            cleanResult(resultSet.getString("MARCA")),
                            cleanResult(resultSet.getString("MODELLO")),
                            cleanResult(resultSet.getString("NOTA"))));
                }
                return values;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Caricamento fresatori cliente non riuscito.", e);
        }
    }

    private void saveClienteFresatori(List<FresatoreClienteEditInput> fresatori) {
        if (database == null || currentClienteId == null) {
            return;
        }
        initializeSchema();
        try (PreparedStatement delete = database.getConnection().prepareStatement("DELETE FROM CLIENTI_FRESATORI WHERE CLIENTE_ID = ?");
             PreparedStatement insert = database.getConnection().prepareStatement("INSERT INTO CLIENTI_FRESATORI (ID, CLIENTE_ID, FRESATORE_ID, NOTA, UPDATED_AT) VALUES (?, ?, ?, ?, ?)")) {
            delete.setString(1, currentClienteId.toString());
            delete.executeUpdate();
            LocalDateTime now = LocalDateTime.now();
            for (FresatoreClienteEditInput fresatore : fresatori) {
                FresatoreClienteEditInput cleanFresatore = cleanFresatore(fresatore);
                if (!hasFresatoreData(cleanFresatore)) {
                    continue;
                }
                UUID fresatoreId = findOrCreateFresatore(cleanFresatore);
                insert.setString(1, idOrNew(cleanFresatore.id()).toString());
                insert.setString(2, currentClienteId.toString());
                insert.setString(3, fresatoreId.toString());
                insert.setString(4, nullableClean(cleanFresatore.nota()));
                insert.setTimestamp(5, Timestamp.valueOf(now));
                insert.addBatch();
            }
            insert.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Salvataggio fresatori cliente non riuscito.", e);
        }
    }

    private UUID findOrCreateFresatore(FresatoreClienteEditInput fresatore) throws SQLException {
        String findSql = "SELECT ID FROM FRESATORI WHERE MARCA = ? AND MODELLO = ?";
        try (PreparedStatement find = database.getConnection().prepareStatement(findSql)) {
            bindFresatoreIdentity(find, fresatore);
            try (ResultSet resultSet = find.executeQuery()) {
                if (resultSet.next()) {
                    return getUuid(resultSet, "ID");
                }
            }
        }

        UUID fresatoreId = UUID.randomUUID();
        try (PreparedStatement insert = database.getConnection().prepareStatement("INSERT INTO FRESATORI (ID, MARCA, MODELLO) VALUES (?, ?, ?)")) {
            insert.setString(1, fresatoreId.toString());
            bindFresatoreIdentity(insert, fresatore, 2);
            insert.executeUpdate();
        }
        return fresatoreId;
    }

    private void bindFresatoreIdentity(PreparedStatement statement, FresatoreClienteEditInput fresatore) throws SQLException {
        bindFresatoreIdentity(statement, fresatore, 1);
    }

    private void bindFresatoreIdentity(PreparedStatement statement, FresatoreClienteEditInput fresatore, int startIndex) throws SQLException {
        statement.setString(startIndex, fresatore.marca());
        statement.setString(startIndex + 1, fresatore.modello());
    }

    private FresatoreClienteEditInput cleanFresatore(FresatoreClienteEditInput fresatore) {
        return new FresatoreClienteEditInput(
                fresatore.id(),
                fresatore.fresatoreId(),
                normalize(fresatore.marca()),
                normalize(fresatore.modello()),
                normalize(fresatore.nota()));
    }

    private boolean hasFresatoreData(FresatoreClienteEditInput fresatore) {
        return !fresatore.marca().isBlank()
                || !fresatore.modello().isBlank();
    }

    private UUID getUuid(ResultSet resultSet, String column) throws SQLException {
        String value = resultSet.getString(column);
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private String cleanResult(String value) {
        return value == null ? "" : value;
    }

    private void initializeSchema() {
        if (schemaInitializer != null) {
            schemaInitializer.initialize();
        }
    }

    private List<ValueItem> toValueItems(List<ValueRecord> values) {
        return values.stream()
                .map(value -> new ValueItem(value.id(), value.value()))
                .toList();
    }

    private List<AddressItem> toAddressItems(List<AddressRecord> values) {
        return values.stream()
                .map(value -> new AddressItem(
                        value.id(),
                        value.paese(),
                        value.regione(),
                        value.provincia(),
                        value.citta(),
                        value.indirizzo(),
                        value.numeroCivico(),
                        value.cap(),
                        value.principale()))
                .toList();
    }

    private List<ContactItem> toContactItems(List<ContactRecord> values) {
        return values.stream()
                .map(value -> new ContactItem(value.id(), value.descrizione(), toValueItems(value.telefoni()), toValueItems(value.email())))
                .toList();
    }

    private InteractionPreview toInteractionPreview(TimelineRecord record) {
        InteractionType type = record.type() == ClienteProfileQuery.TimelineType.CHIAMATA
                ? InteractionType.CHIAMATA
                : InteractionType.NOTA;
        return new InteractionPreview(record.interazioneId(), record.data(), type, record.prossimoContatto(), record.testo());
    }

    private ClienteProfile emptyProfile() {
        return new ClienteProfile(
                currentClienteId,
                "Cliente non trovato",
                "",
                "",
                null,
                "",
                "",
                null,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public ClienteProfile toggleFavorite() {
        ensureProfileLoaded();
        if (currentClienteId == null) {
            return filteredProfile();
        }

        persistenceService.togglePreferito(currentOperatoreService.currentOperatoreId(), currentClienteId);
        return loadProfile(currentClienteId);
    }

    public ClienteProfile updateCoinvolgimento(Integer coinvolgimento) {
        ensureProfileLoaded();
        if (currentClienteId == null) {
            return filteredProfile();
        }

        Integer cleanCoinvolgimento = cleanCoinvolgimento(coinvolgimento);
        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = new Cliente(
                currentClienteId,
                nullableClean(currentProfile.ragioneSociale()),
                nullableClean(currentProfile.tipoCliente()),
                nullableClean(currentProfile.statoTrattativa()),
                cleanCoinvolgimento,
                nullableClean(currentProfile.partitaIva()),
                nullableClean(currentProfile.codiceFiscale()),
                currentProfile.acquisizione(),
                currentOperatoreService.currentOperatoreId(),
                null,
                now
        );
        persistenceService.updateCliente(cliente);
        currentProfile = currentProfile.withCoinvolgimento(cleanCoinvolgimento);
        return filteredProfile();
    }

    public ClienteProfile setTimelineFilter(TimelineFilter filter) {
        ensureProfileLoaded();
        currentFilter = filter == null ? TimelineFilter.ALL : filter;
        return filteredProfile();
    }

    public EditProfileDraft startEdit() {
        ensureProfileLoaded();
        currentFilter = TimelineFilter.ALL;
        editingDraft = EditProfileDraft.from(currentProfile);
        return editingDraft;
    }

    public List<FornoClienteEditInput> startForniEdit() {
        ensureProfileLoaded();
        return currentProfile.forni().stream()
                .map(FornoClienteEditInput::from)
                .toList();
    }

    public List<FornoClienteItem> cancelForniEdit() {
        ensureProfileLoaded();
        return currentProfile.forni();
    }

    public List<FornoClienteItem> saveForniEdit(List<FornoClienteEditInput> forni) {
        ensureProfileLoaded();
        saveClienteForni(forni == null ? List.of() : forni);
        List<FornoClienteItem> savedForni = findClienteForni(currentClienteId);
        currentProfile = currentProfile.withForni(savedForni);
        return savedForni;
    }

    public ClienteProfile cancelEdit() {
        ensureProfileLoaded();
        editingDraft = null;
        return filteredProfile();
    }

    public ClienteProfile saveEdit(EditProfileDraft draft) {
        ensureProfileLoaded();
        if (currentClienteId == null) {
            return filteredProfile();
        }

        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = new Cliente(
                currentClienteId,
                nullableClean(draft.ragioneSociale()),
                nullableClean(draft.tipoCliente()),
                nullableClean(draft.statoTrattativa()),
                cleanCoinvolgimento(draft.coinvolgimento()),
                nullableClean(draft.partitaIva()),
                nullableClean(draft.codiceFiscale()),
                draft.acquisizione(),
                currentOperatoreService.currentOperatoreId(),
                null,
                now
        );

        ContactModels contactModels = toContactModels(draft.contatti());
        persistenceService.updateClienteProfile(
                cliente,
                toIndirizzi(draft.indirizzi(), now),
                toSitiWeb(draft.sitiWeb()),
                contactModels.contatti(),
                combine(toTelefoni(draft.telefoni(), null), contactModels.telefoni()),
                combine(toEmail(draft.email(), null), contactModels.email()),
                toInterazioneUpdates(draft.interazioni(), now)
        );
        saveClienteForni(draft.forni());
        saveClienteFresatori(draft.fresatori());

        editingDraft = null;
        currentFilter = TimelineFilter.ALL;
        return loadProfile(currentClienteId);
    }


    private List<TelefonoCliente> toTelefoni(List<ValueEditInput> values, UUID contattoId) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new TelefonoCliente(value.id(), currentClienteId, contattoId, value.value()))
                .toList();
    }

    private List<EmailCliente> toEmail(List<ValueEditInput> values, UUID contattoId) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new EmailCliente(value.id(), currentClienteId, contattoId, value.value()))
                .toList();
    }

    private List<SitoWebCliente> toSitiWeb(List<ValueEditInput> values) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new SitoWebCliente(value.id(), currentClienteId, value.value()))
                .toList();
    }

    private ContactModels toContactModels(List<ContactEditInput> values) {
        List<ContattoCliente> contatti = new ArrayList<>();
        List<TelefonoCliente> telefoni = new ArrayList<>();
        List<EmailCliente> email = new ArrayList<>();
        for (ContactEditInput value : values) {
            UUID contattoId = idOrNew(value.id());
            String descrizione = nullableClean(value.descrizione());
            List<TelefonoCliente> telefoniContatto = toTelefoni(value.telefoni(), contattoId);
            List<EmailCliente> emailContatto = toEmail(value.email(), contattoId);
            if (descrizione == null && telefoniContatto.isEmpty() && emailContatto.isEmpty()) {
                continue;
            }

            contatti.add(new ContattoCliente(contattoId, currentClienteId, descrizione));
            telefoni.addAll(telefoniContatto);
            email.addAll(emailContatto);
        }
        return new ContactModels(contatti, telefoni, email);
    }

    private List<IndirizzoCliente> toIndirizzi(List<AddressEditInput> values, LocalDateTime now) {
        return values.stream()
                .map(value -> new AddressEditInput(
                        idOrNew(value.id()),
                        nullableClean(value.paese()),
                        nullableClean(value.regione()),
                        nullableClean(value.provincia()),
                        nullableClean(value.citta()),
                        nullableClean(value.indirizzo()),
                        nullableClean(value.numeroCivico()),
                        nullableClean(value.cap()),
                        value.principale()))
                .filter(this::hasAddressData)
                .map(value -> new IndirizzoCliente(
                        value.id(),
                        currentClienteId,
                        value.paese(),
                        value.regione(),
                        value.provincia(),
                        value.citta(),
                        value.indirizzo(),
                        value.numeroCivico(),
                        value.cap(),
                        value.principale(),
                        now,
                        now))
                .toList();
    }

    private boolean hasAddressData(AddressEditInput value) {
        return value.paese() != null
                || value.regione() != null
                || value.provincia() != null
                || value.citta() != null
                || value.indirizzo() != null
                || value.numeroCivico() != null
                || value.cap() != null;
    }

    private <T> List<T> combine(List<T> first, List<T> second) {
        List<T> values = new ArrayList<>(first);
        values.addAll(second);
        return values;
    }


    private List<Interazione> toInterazioneUpdates(List<InteractionEditInput> interactions, LocalDateTime now) {
        return interactions.stream()
                .filter(interaction -> interaction.interazioneId() != null)
                .map(interaction -> new Interazione(
                        interaction.interazioneId(),
                        currentClienteId,
                        currentOperatoreService.currentOperatoreId(),
                        interaction.type().name(),
                        null,
                        interaction.data(),
                        interaction.prossimoContatto(),
                        BigDecimal.ZERO,
                        normalize(interaction.testo()),
                        null,
                        now
                ))
                .toList();
    }

    private UUID idOrNew(UUID id) {
        return id == null ? UUID.randomUUID() : id;
    }

    public ClienteProfile addNota(String testo) {
        ensureProfileLoaded();
        if (currentClienteId == null || testo == null || testo.isBlank()) {
            return filteredProfile();
        }

        LocalDateTime now = LocalDateTime.now();
        Interazione interazione = new Interazione(
                UUID.randomUUID(),
                currentClienteId,
                currentOperatoreService.currentOperatoreId(),
                InteractionType.NOTA.name(),
                null,
                LocalDate.now(),
                null,
                BigDecimal.ZERO,
                testo.trim(),
                now,
                null
        );
        persistenceService.addInterazione(interazione);
        return loadProfile(currentClienteId);
    }

    public ClienteProfile addChiamata(String testo, LocalDate prossimoContatto) {
        ensureProfileLoaded();
        if (currentClienteId == null) {
            return filteredProfile();
        }

        LocalDateTime now = LocalDateTime.now();
        Interazione interazione = new Interazione(
                UUID.randomUUID(),
                currentClienteId,
                currentOperatoreService.currentOperatoreId(),
                InteractionType.CHIAMATA.name(),
                null,
                LocalDate.now(),
                prossimoContatto,
                BigDecimal.ZERO,
                nullableClean(testo),
                now,
                null
        );
        persistenceService.addInterazione(interazione);
        return loadProfile(currentClienteId);
    }

    private void addInteraction(InteractionPreview interaction) {
        List<InteractionPreview> interazioni = new ArrayList<>(currentProfile.interazioni());
        interazioni.add(0, interaction);
        currentProfile = currentProfile.withInterazioni(interazioni);
    }

    private ClienteProfile filteredProfile() {
        if (currentFilter == TimelineFilter.ALL) {
            return currentProfile;
        }

        List<InteractionPreview> filteredInteractions = currentProfile.interazioni().stream()
                .filter(interaction -> currentFilter.matches(interaction.type()))
                .toList();
        return currentProfile.withInterazioni(filteredInteractions);
    }

    private void ensureProfileLoaded() {
        if (currentProfile == null) {
            loadProfile(currentClienteId);
        }
    }

    private List<ValueItem> cleanValueItems(List<ValueEditInput> values) {
        return values.stream()
                .map(value -> new ValueItem(value.id(), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .toList();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer cleanCoinvolgimento(Integer value) {
        if (value == null || value < 1 || value > 5) {
            return null;
        }
        return value;
    }

    private String nullableClean(String value) {
        String cleanValue = normalize(value);
        return cleanValue.isBlank() ? null : cleanValue;
    }

    public enum TimelineFilter {
        ALL,
        NOTES,
        CALLS;

        private boolean matches(InteractionType type) {
            return this == ALL
                    || (this == NOTES && type == InteractionType.NOTA)
                    || (this == CALLS && type == InteractionType.CHIAMATA);
        }
    }

    public enum InteractionType {
        NOTA("Nota"),
        CHIAMATA("Chiamata");

        private final String label;

        InteractionType(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public record ClienteProfile(
            UUID clienteId,
            String ragioneSociale,
            String tipoCliente,
            String statoTrattativa,
            Integer coinvolgimento,
            String partitaIva,
            String codiceFiscale,
            LocalDate acquisizione,
            boolean favorite,
            List<ValueItem> telefoni,
            List<ValueItem> email,
            List<ValueItem> sitiWeb,
            List<AddressItem> indirizzi,
            List<ContactItem> contatti,
            List<FornoClienteItem> forni,
            List<FresatoreClienteItem> fresatori,
            List<InteractionPreview> interazioni
    ) {
        public ClienteProfile {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
            sitiWeb = List.copyOf(sitiWeb);
            indirizzi = List.copyOf(indirizzi);
            contatti = List.copyOf(contatti);
            forni = List.copyOf(forni);
            fresatori = List.copyOf(fresatori);
            interazioni = List.copyOf(interazioni);
        }

        private ClienteProfile withCoinvolgimento(Integer coinvolgimento) {
            return new ClienteProfile(clienteId, ragioneSociale, tipoCliente, statoTrattativa, coinvolgimento, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, forni, fresatori, interazioni);
        }

        private ClienteProfile withFavorite(boolean favorite) {
            return new ClienteProfile(clienteId, ragioneSociale, tipoCliente, statoTrattativa, coinvolgimento, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, forni, fresatori, interazioni);
        }

        private ClienteProfile withInterazioni(List<InteractionPreview> interazioni) {
            return new ClienteProfile(clienteId, ragioneSociale, tipoCliente, statoTrattativa, coinvolgimento, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, forni, fresatori, interazioni);
        }

        private ClienteProfile withForni(List<FornoClienteItem> forni) {
            return new ClienteProfile(clienteId, ragioneSociale, tipoCliente, statoTrattativa, coinvolgimento, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, forni, fresatori, interazioni);
        }
    }

    public record EditProfileDraft(
            String ragioneSociale,
            String tipoCliente,
            String statoTrattativa,
            Integer coinvolgimento,
            String partitaIva,
            String codiceFiscale,
            LocalDate acquisizione,
            List<ValueEditInput> telefoni,
            List<ValueEditInput> email,
            List<ValueEditInput> sitiWeb,
            List<AddressEditInput> indirizzi,
            List<ContactEditInput> contatti,
            List<FornoClienteEditInput> forni,
            List<FresatoreClienteEditInput> fresatori,
            List<InteractionEditInput> interazioni
    ) {
        public EditProfileDraft {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
            sitiWeb = List.copyOf(sitiWeb);
            indirizzi = List.copyOf(indirizzi);
            contatti = List.copyOf(contatti);
            forni = List.copyOf(forni);
            fresatori = List.copyOf(fresatori);
            interazioni = List.copyOf(interazioni);
        }


        private static List<ValueEditInput> toEditInputs(List<ValueItem> values) {
            return values.stream()
                    .map(value -> new ValueEditInput(value.id(), value.value()))
                    .toList();
        }

        private static List<AddressEditInput> toAddressEditInputs(List<AddressItem> values) {
            return values.stream()
                    .map(value -> new AddressEditInput(
                            value.id(),
                            value.paese(),
                            value.regione(),
                            value.provincia(),
                            value.citta(),
                            value.indirizzo(),
                            value.numeroCivico(),
                            value.cap(),
                            value.principale()))
                    .toList();
        }

        private static List<ContactEditInput> toContactEditInputs(List<ContactItem> values) {
            return values.stream()
                    .map(value -> new ContactEditInput(value.id(), value.descrizione(), toEditInputs(value.telefoni()), toEditInputs(value.email())))
                    .toList();
        }

        private static EditProfileDraft from(ClienteProfile profile) {
            return new EditProfileDraft(
                    profile.ragioneSociale(),
                    profile.tipoCliente(),
                    profile.statoTrattativa(),
                    profile.coinvolgimento(),
                    profile.partitaIva(),
                    profile.codiceFiscale(),
                    profile.acquisizione(),
                    toEditInputs(profile.telefoni()),
                    toEditInputs(profile.email()),
                    toEditInputs(profile.sitiWeb()),
                    toAddressEditInputs(profile.indirizzi()),
                    toContactEditInputs(profile.contatti()),
                    profile.forni().stream()
                            .map(FornoClienteEditInput::from)
                            .toList(),
                    profile.fresatori().stream()
                            .map(FresatoreClienteEditInput::from)
                            .toList(),
                    profile.interazioni().stream()
                            .map(InteractionEditInput::from)
                            .toList()
            );
        }
    }

    public record FornoCatalogItem(UUID fornoId, String tecnologia, String anno, String marca, String modello) {
    }

    public record FornoClienteItem(UUID id, UUID fornoId, String tecnologia, String anno, String marca, String modello, String nota) {
    }

    public record FornoClienteEditInput(UUID id, UUID fornoId, String tecnologia, String anno, String marca, String modello, String nota) {
        private static FornoClienteEditInput from(FornoClienteItem item) {
            return new FornoClienteEditInput(item.id(), item.fornoId(), item.tecnologia(), item.anno(), item.marca(), item.modello(), item.nota());
        }
    }

    public record FresatoreCatalogItem(UUID fresatoreId, String marca, String modello) {
    }

    public record FresatoreClienteItem(UUID id, UUID fresatoreId, String marca, String modello, String nota) {
    }

    public record FresatoreClienteEditInput(UUID id, UUID fresatoreId, String marca, String modello, String nota) {
        private static FresatoreClienteEditInput from(FresatoreClienteItem item) {
            return new FresatoreClienteEditInput(item.id(), item.fresatoreId(), item.marca(), item.modello(), item.nota());
        }
    }

    public record ValueItem(UUID id, String value) {
    }

    public record AddressItem(
            UUID id,
            String paese,
            String regione,
            String provincia,
            String citta,
            String indirizzo,
            String numeroCivico,
            String cap,
            boolean principale
    ) {
    }

    public record ContactItem(UUID id, String descrizione, List<ValueItem> telefoni, List<ValueItem> email) {
        public ContactItem {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
        }
    }

    public record ValueEditInput(UUID id, String value) {
    }

    public record AddressEditInput(
            UUID id,
            String paese,
            String regione,
            String provincia,
            String citta,
            String indirizzo,
            String numeroCivico,
            String cap,
            boolean principale
    ) {
    }

    public record ContactEditInput(UUID id, String descrizione, List<ValueEditInput> telefoni, List<ValueEditInput> email) {
        public ContactEditInput {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
        }
    }

    private record ContactModels(List<ContattoCliente> contatti, List<TelefonoCliente> telefoni, List<EmailCliente> email) {
    }

    public record InteractionEditInput(UUID interazioneId, LocalDate data, InteractionType type, LocalDate prossimoContatto, String testo) {
        private static InteractionEditInput from(InteractionPreview interaction) {
            return new InteractionEditInput(interaction.interazioneId(), interaction.data(), interaction.type(), interaction.prossimoContatto(), interaction.testo());
        }
    }

    public record InteractionPreview(UUID interazioneId, LocalDate data, InteractionType type, LocalDate prossimoContatto, String testo) {
    }
}

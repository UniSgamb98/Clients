package com.example.clients.feature.clienti.schedacliente.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.model.ContattoCliente;
import com.example.clients.core.database.model.EmailCliente;
import com.example.clients.core.database.model.IndirizzoCliente;
import com.example.clients.core.database.model.Interazione;
import com.example.clients.core.database.model.NotaCliente;
import com.example.clients.core.database.model.SitoWebCliente;
import com.example.clients.core.database.model.TelefonoCliente;
import com.example.clients.core.database.query.ClienteProfileQuery;
import com.example.clients.core.database.query.ClienteProfileQuery.ClienteProfileRecord;
import com.example.clients.core.database.query.ClienteProfileQuery.TimelineRecord;
import com.example.clients.core.database.query.ClienteProfileQuery.ValueRecord;
import com.example.clients.core.database.query.derby.DerbyClienteProfileQuery;
import com.example.clients.core.database.service.ClientePersistenceService;
import com.example.clients.core.database.service.CurrentOperatoreService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SchedaClienteService {

    private final ClienteProfileQuery profileQuery;
    private final ClientePersistenceService persistenceService;
    private final CurrentOperatoreService currentOperatoreService;
    private ClienteProfile currentProfile;
    private EditProfileDraft editingDraft;
    private UUID currentClienteId;
    private TimelineFilter currentFilter = TimelineFilter.ALL;

    public SchedaClienteService() {
        this(new Database());
    }

    public SchedaClienteService(Database database) {
        this(new DerbyClienteProfileQuery(database), new ClientePersistenceService(database), new CurrentOperatoreService());
    }

    public SchedaClienteService(
            ClienteProfileQuery profileQuery,
            ClientePersistenceService persistenceService,
            CurrentOperatoreService currentOperatoreService
    ) {
        this.profileQuery = profileQuery;
        this.persistenceService = persistenceService;
        this.currentOperatoreService = currentOperatoreService;
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
                record.partitaIva(),
                record.codiceFiscale(),
                record.acquisizione(),
                record.favorite(),
                toValueItems(record.telefoni()),
                toValueItems(record.email()),
                toValueItems(record.sitiWeb()),
                toValueItems(record.indirizzi()),
                toValueItems(record.contatti()),
                record.timeline().stream()
                        .map(this::toInteractionPreview)
                        .toList()
        );
    }


    private List<ValueItem> toValueItems(List<ValueRecord> values) {
        return values.stream()
                .map(value -> new ValueItem(value.id(), value.value()))
                .toList();
    }

    private InteractionPreview toInteractionPreview(TimelineRecord record) {
        InteractionType type = record.type() == ClienteProfileQuery.TimelineType.CHIAMATA
                ? InteractionType.CHIAMATA
                : InteractionType.NOTA;
        return new InteractionPreview(record.notaId(), record.interazioneId(), record.data(), type, record.prossimoContatto(), record.testo());
    }

    private ClienteProfile emptyProfile() {
        return new ClienteProfile(
                currentClienteId,
                "Cliente non trovato",
                "",
                "",
                "",
                "",
                null,
                false,
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
                nullableClean(draft.partitaIva()),
                nullableClean(draft.codiceFiscale()),
                draft.acquisizione(),
                currentOperatoreService.currentOperatoreId(),
                null,
                now
        );

        persistenceService.updateClienteProfile(
                cliente,
                toIndirizzi(draft.indirizzi(), now),
                toSitiWeb(draft.sitiWeb()),
                toContatti(draft.contatti()),
                toTelefoni(draft.telefoni()),
                toEmail(draft.email()),
                toNoteUpdates(draft.interazioni(), now),
                toInterazioneUpdates(draft.interazioni(), now)
        );

        editingDraft = null;
        currentFilter = TimelineFilter.ALL;
        return loadProfile(currentClienteId);
    }


    private List<TelefonoCliente> toTelefoni(List<ValueEditInput> values) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new TelefonoCliente(value.id(), currentClienteId, null, value.value()))
                .toList();
    }

    private List<EmailCliente> toEmail(List<ValueEditInput> values) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new EmailCliente(value.id(), currentClienteId, null, value.value()))
                .toList();
    }

    private List<SitoWebCliente> toSitiWeb(List<ValueEditInput> values) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new SitoWebCliente(value.id(), currentClienteId, value.value()))
                .toList();
    }

    private List<ContattoCliente> toContatti(List<ValueEditInput> values) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new ContattoCliente(value.id(), currentClienteId, value.value()))
                .toList();
    }

    private List<IndirizzoCliente> toIndirizzi(List<ValueEditInput> values, LocalDateTime now) {
        return values.stream()
                .map(value -> new ValueItem(idOrNew(value.id()), normalize(value.value())))
                .filter(value -> !value.value().isBlank())
                .map(value -> new IndirizzoCliente(value.id(), currentClienteId, null, null, null, null, value.value(), null, null, false, now, now))
                .toList();
    }

    private List<NotaCliente> toNoteUpdates(List<InteractionEditInput> interactions, LocalDateTime now) {
        return interactions.stream()
                .filter(interaction -> interaction.notaId() != null)
                .map(interaction -> new NotaCliente(
                        interaction.notaId(),
                        currentClienteId,
                        currentOperatoreService.currentOperatoreId(),
                        normalize(interaction.testo()),
                        null,
                        now
                ))
                .filter(nota -> !nota.testo().isBlank())
                .toList();
    }

    private List<Interazione> toInterazioneUpdates(List<InteractionEditInput> interactions, LocalDateTime now) {
        return interactions.stream()
                .filter(interaction -> interaction.interazioneId() != null)
                .map(interaction -> new Interazione(
                        interaction.interazioneId(),
                        currentClienteId,
                        currentOperatoreService.currentOperatoreId(),
                        interaction.notaId(),
                        interaction.data(),
                        interaction.prossimoContatto(),
                        BigDecimal.ZERO,
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
        NotaCliente nota = new NotaCliente(
                UUID.randomUUID(),
                currentClienteId,
                currentOperatoreService.currentOperatoreId(),
                testo.trim(),
                now,
                null
        );
        persistenceService.addNota(nota);
        return loadProfile(currentClienteId);
    }

    public ClienteProfile addChiamata(String testo, LocalDate prossimoContatto) {
        ensureProfileLoaded();
        if (currentClienteId == null) {
            return filteredProfile();
        }

        LocalDateTime now = LocalDateTime.now();
        NotaCliente nota = null;
        if (testo != null && !testo.isBlank()) {
            nota = new NotaCliente(
                    UUID.randomUUID(),
                    currentClienteId,
                    currentOperatoreService.currentOperatoreId(),
                    testo.trim(),
                    now,
                    null
            );
        }

        Interazione interazione = new Interazione(
                UUID.randomUUID(),
                currentClienteId,
                currentOperatoreService.currentOperatoreId(),
                nota == null ? null : nota.id(),
                LocalDate.now(),
                prossimoContatto,
                BigDecimal.ZERO,
                now,
                null
        );
        persistenceService.addChiamata(nota, interazione);
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
            String partitaIva,
            String codiceFiscale,
            LocalDate acquisizione,
            boolean favorite,
            List<ValueItem> telefoni,
            List<ValueItem> email,
            List<ValueItem> sitiWeb,
            List<ValueItem> indirizzi,
            List<ValueItem> contatti,
            List<InteractionPreview> interazioni
    ) {
        public ClienteProfile {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
            sitiWeb = List.copyOf(sitiWeb);
            indirizzi = List.copyOf(indirizzi);
            contatti = List.copyOf(contatti);
            interazioni = List.copyOf(interazioni);
        }

        private ClienteProfile withFavorite(boolean favorite) {
            return new ClienteProfile(clienteId, ragioneSociale, tipoCliente, statoTrattativa, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, interazioni);
        }

        private ClienteProfile withInterazioni(List<InteractionPreview> interazioni) {
            return new ClienteProfile(clienteId, ragioneSociale, tipoCliente, statoTrattativa, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, interazioni);
        }
    }

    public record EditProfileDraft(
            String ragioneSociale,
            String tipoCliente,
            String statoTrattativa,
            String partitaIva,
            String codiceFiscale,
            LocalDate acquisizione,
            List<ValueEditInput> telefoni,
            List<ValueEditInput> email,
            List<ValueEditInput> sitiWeb,
            List<ValueEditInput> indirizzi,
            List<ValueEditInput> contatti,
            List<InteractionEditInput> interazioni
    ) {
        public EditProfileDraft {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
            sitiWeb = List.copyOf(sitiWeb);
            indirizzi = List.copyOf(indirizzi);
            contatti = List.copyOf(contatti);
            interazioni = List.copyOf(interazioni);
        }


        private static List<ValueEditInput> toEditInputs(List<ValueItem> values) {
            return values.stream()
                    .map(value -> new ValueEditInput(value.id(), value.value()))
                    .toList();
        }

        private static EditProfileDraft from(ClienteProfile profile) {
            return new EditProfileDraft(
                    profile.ragioneSociale(),
                    profile.tipoCliente(),
                    profile.statoTrattativa(),
                    profile.partitaIva(),
                    profile.codiceFiscale(),
                    profile.acquisizione(),
                    toEditInputs(profile.telefoni()),
                    toEditInputs(profile.email()),
                    toEditInputs(profile.sitiWeb()),
                    toEditInputs(profile.indirizzi()),
                    toEditInputs(profile.contatti()),
                    profile.interazioni().stream()
                            .map(InteractionEditInput::from)
                            .toList()
            );
        }
    }

    public record ValueItem(UUID id, String value) {
    }

    public record ValueEditInput(UUID id, String value) {
    }

    public record InteractionEditInput(UUID notaId, UUID interazioneId, LocalDate data, InteractionType type, LocalDate prossimoContatto, String testo) {
        private static InteractionEditInput from(InteractionPreview interaction) {
            return new InteractionEditInput(interaction.notaId(), interaction.interazioneId(), interaction.data(), interaction.type(), interaction.prossimoContatto(), interaction.testo());
        }
    }

    public record InteractionPreview(UUID notaId, UUID interazioneId, LocalDate data, InteractionType type, LocalDate prossimoContatto, String testo) {
    }
}

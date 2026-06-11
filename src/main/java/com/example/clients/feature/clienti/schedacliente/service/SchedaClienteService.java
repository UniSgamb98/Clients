package com.example.clients.feature.clienti.schedacliente.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SchedaClienteService {

    private final SchedaClientePersistenceService persistenceService;
    private ClienteProfile currentProfile;
    private TimelineFilter currentFilter = TimelineFilter.ALL;

    public SchedaClienteService() {
        this(new SchedaClientePersistenceService());
    }

    public SchedaClienteService(SchedaClientePersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public ClienteProfile loadProfile(String clienteName) {
        String name = clienteName == null || clienteName.isBlank() ? "Rossi S.r.l." : clienteName;
        currentProfile = new ClienteProfile(
                name,
                "Azienda",
                "Attivo",
                "IT12345678901",
                "RSSSRL80A01H501Z",
                LocalDate.of(2026, 6, 1),
                false,
                new ArrayList<>(List.of("02 123456", "333 4455667")),
                new ArrayList<>(List.of("info@rossi.it", "amministrazione@rossi.it")),
                new ArrayList<>(List.of("www.rossi.it")),
                new ArrayList<>(List.of("Sede principale · Milano, Via Roma 12 · 20100")),
                new ArrayList<>(List.of("Mario Rossi · 333 4455667 · mario@rossi.it")),
                new ArrayList<>(List.of(
                        new InteractionPreview(LocalDate.now().minusDays(1), InteractionType.NOTA, null,
                                "Cliente interessato a ricevere un preventivo aggiornato."),
                        new InteractionPreview(LocalDate.now().minusDays(7), InteractionType.CHIAMATA, LocalDate.now().plusDays(3),
                                "Primo contatto telefonico con il referente amministrativo.")
                ))
        );
        currentFilter = TimelineFilter.ALL;
        return filteredProfile();
    }

    public ClienteProfile toggleFavorite() {
        ensureProfileLoaded();
        currentProfile = currentProfile.withFavorite(!currentProfile.favorite());
        return filteredProfile();
    }

    public ClienteProfile setTimelineFilter(TimelineFilter filter) {
        ensureProfileLoaded();
        currentFilter = filter == null ? TimelineFilter.ALL : filter;
        return filteredProfile();
    }

    public ClienteProfile addNota(String testo) {
        ensureProfileLoaded();
        if (testo == null || testo.isBlank()) {
            return filteredProfile();
        }

        addInteraction(persistenceService.salvaNota(testo));
        return filteredProfile();
    }

    public ClienteProfile addChiamata(String testo, LocalDate prossimoContatto) {
        ensureProfileLoaded();
        addInteraction(persistenceService.salvaChiamata(testo, prossimoContatto));
        return filteredProfile();
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
            loadProfile(null);
        }
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
            String ragioneSociale,
            String tipoCliente,
            String statoTrattativa,
            String partitaIva,
            String codiceFiscale,
            LocalDate acquisizione,
            boolean favorite,
            List<String> telefoni,
            List<String> email,
            List<String> sitiWeb,
            List<String> indirizzi,
            List<String> contatti,
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
            return new ClienteProfile(ragioneSociale, tipoCliente, statoTrattativa, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, interazioni);
        }

        private ClienteProfile withInterazioni(List<InteractionPreview> interazioni) {
            return new ClienteProfile(ragioneSociale, tipoCliente, statoTrattativa, partitaIva, codiceFiscale, acquisizione,
                    favorite, telefoni, email, sitiWeb, indirizzi, contatti, interazioni);
        }
    }

    public record InteractionPreview(LocalDate data, InteractionType type, LocalDate prossimoContatto, String testo) {
    }
}

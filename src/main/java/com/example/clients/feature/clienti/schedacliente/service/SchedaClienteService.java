package com.example.clients.feature.clienti.schedacliente.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SchedaClienteService {

    private ClienteProfile currentProfile;

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
                        new InteractionPreview(LocalDate.now().minusDays(1), "Nota", "Cliente interessato a ricevere un preventivo aggiornato."),
                        new InteractionPreview(LocalDate.now().minusDays(7), "Chiamata", "Primo contatto telefonico con il referente amministrativo.")
                ))
        );
        return currentProfile;
    }

    public ClienteProfile toggleFavorite() {
        ensureProfileLoaded();
        currentProfile = currentProfile.withFavorite(!currentProfile.favorite());
        return currentProfile;
    }

    public ClienteProfile addAnnotazione(String testo) {
        ensureProfileLoaded();
        if (testo == null || testo.isBlank()) {
            return currentProfile;
        }

        List<InteractionPreview> interazioni = new ArrayList<>(currentProfile.interazioni());
        interazioni.add(0, new InteractionPreview(LocalDate.now(), "Annotazione", testo.trim()));
        currentProfile = currentProfile.withInterazioni(interazioni);
        return currentProfile;
    }

    private void ensureProfileLoaded() {
        if (currentProfile == null) {
            loadProfile(null);
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

    public record InteractionPreview(LocalDate data, String titolo, String testo) {
    }
}

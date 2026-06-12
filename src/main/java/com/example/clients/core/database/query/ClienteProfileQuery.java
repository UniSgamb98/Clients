package com.example.clients.core.database.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteProfileQuery {
    Optional<ClienteProfileRecord> findById(UUID clienteId);

    enum TimelineType {
        NOTA,
        CHIAMATA
    }

    record ClienteProfileRecord(
            UUID clienteId,
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
            List<TimelineRecord> timeline
    ) {
        public ClienteProfileRecord {
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
            sitiWeb = List.copyOf(sitiWeb);
            indirizzi = List.copyOf(indirizzi);
            contatti = List.copyOf(contatti);
            timeline = List.copyOf(timeline);
        }
    }

    record TimelineRecord(
            LocalDate data,
            TimelineType type,
            LocalDate prossimoContatto,
            String testo
    ) {
    }
}

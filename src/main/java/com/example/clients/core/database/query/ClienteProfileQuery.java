package com.example.clients.core.database.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteProfileQuery {
    Optional<ClienteProfileRecord> findById(UUID clienteId, UUID operatoreId);

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
            List<ValueRecord> telefoni,
            List<ValueRecord> email,
            List<ValueRecord> sitiWeb,
            List<ValueRecord> indirizzi,
            List<ValueRecord> contatti,
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

    record ValueRecord(UUID id, String value) {
    }

    record TimelineRecord(
            UUID notaId,
            UUID interazioneId,
            LocalDate data,
            TimelineType type,
            LocalDate prossimoContatto,
            String testo
    ) {
    }
}

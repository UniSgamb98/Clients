package com.example.clients.core.database.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttivitaQuery {
    List<AttivitaListRecord> findAll();

    Optional<AttivitaDetailRecord> findById(UUID attivitaId);

    record AttivitaListRecord(
            UUID id,
            String titolo,
            String tipoAttivita,
            Integer priorita,
            String stato,
            LocalDate dataInizio,
            LocalDate dataFine,
            int totaleClienti,
            int daFare,
            int inCorso,
            int sospesi,
            int completati,
            int annullati
    ) {
    }

    record AttivitaDetailRecord(
            UUID id,
            String titolo,
            String descrizione,
            String tipoAttivita,
            Integer priorita,
            String stato,
            LocalDate dataInizio,
            LocalDate dataFine,
            List<AttivitaClienteRecord> clienti
    ) {
        public AttivitaDetailRecord {
            clienti = List.copyOf(clienti);
        }
    }

    record AttivitaClienteRecord(
            UUID id,
            UUID clienteId,
            String ragioneSociale,
            String stato,
            UUID interazioneId,
            LocalDate dataUltimoContatto,
            LocalDate prossimoContatto,
            String testoUltimaInterazione
    ) {
    }
}

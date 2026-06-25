package com.example.clients.feature.attivita.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.Attivita;
import com.example.clients.core.database.model.AttivitaCliente;
import com.example.clients.core.database.model.Interazione;
import com.example.clients.core.database.model.NotaCliente;
import com.example.clients.core.database.model.TipoAttivita;
import com.example.clients.core.database.query.AttivitaQuery;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaDetailRecord;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaListRecord;
import com.example.clients.core.database.query.derby.DerbyAttivitaQuery;
import com.example.clients.core.database.repository.AttivitaClienteRepository;
import com.example.clients.core.database.repository.AttivitaRepository;
import com.example.clients.core.database.repository.TipoAttivitaRepository;
import com.example.clients.core.database.repository.derby.DerbyAttivitaClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyAttivitaRepository;
import com.example.clients.core.database.repository.derby.DerbyTipoAttivitaRepository;
import com.example.clients.core.database.service.ClientePersistenceService;
import com.example.clients.core.database.service.CurrentOperatoreService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AttivitaService {

    public static final String STATO_ATTIVITA_BOZZA = "BOZZA";
    public static final String STATO_CLIENTE_DA_FARE = "DA_FARE";
    public static final String STATO_CLIENTE_IN_CORSO = "IN_CORSO";
    public static final String STATO_CLIENTE_SOSPESO = "SOSPESO";
    public static final String STATO_CLIENTE_COMPLETATO = "COMPLETATO";
    public static final String STATO_CLIENTE_ANNULLATO = "ANNULLATO";

    private final AttivitaRepository attivitaRepository;
    private final AttivitaClienteRepository attivitaClienteRepository;
    private final AttivitaQuery attivitaQuery;
    private final TipoAttivitaRepository tipoAttivitaRepository;
    private final ClientePersistenceService clientePersistenceService;
    private final CurrentOperatoreService currentOperatoreService;

    public AttivitaService(Database database) {
        this(
                new DerbyAttivitaRepository(database),
                new DerbyAttivitaClienteRepository(database),
                new DerbyAttivitaQuery(database),
                new DerbyTipoAttivitaRepository(database),
                new ClientePersistenceService(database),
                new CurrentOperatoreService()
        );
    }

    public AttivitaService(
            AttivitaRepository attivitaRepository,
            AttivitaClienteRepository attivitaClienteRepository,
            AttivitaQuery attivitaQuery,
            TipoAttivitaRepository tipoAttivitaRepository,
            ClientePersistenceService clientePersistenceService,
            CurrentOperatoreService currentOperatoreService
    ) {
        this.attivitaRepository = attivitaRepository;
        this.attivitaClienteRepository = attivitaClienteRepository;
        this.attivitaQuery = attivitaQuery;
        this.tipoAttivitaRepository = tipoAttivitaRepository;
        this.clientePersistenceService = clientePersistenceService;
        this.currentOperatoreService = currentOperatoreService;
    }

    public List<TipoAttivitaOption> listaTipiAttivita() {
        return tipoAttivitaRepository.findActive().stream()
                .map(tipo -> new TipoAttivitaOption(tipo.id(), tipo.nome()))
                .toList();
    }

    public AttivitaDetailRecord creaAttivita(AttivitaCreateInput input) {
        Objects.requireNonNull(input, "input");
        UUID attivitaId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Attivita attivita = new Attivita(
                attivitaId,
                requireText(input.titolo(), "titolo"),
                clean(input.descrizione()),
                cleanPriorita(input.priorita()),
                cleanStatoAttivita(input.stato()),
                input.dataInizio(),
                input.dataFine(),
                input.tipoAttivitaId(),
                now,
                now
        );
        attivitaRepository.insert(attivita);
        aggiungiClienti(attivitaId, input.clientiId());
        return dettaglioAttivita(attivitaId);
    }

    public List<AttivitaListRecord> listaAttivita() {
        return attivitaQuery.findAll();
    }

    public AttivitaDetailRecord dettaglioAttivita(UUID attivitaId) {
        return attivitaQuery.findById(attivitaId)
                .orElseThrow(() -> new IllegalArgumentException("Attività non trovata."));
    }

    public void aggiungiClienti(UUID attivitaId, List<UUID> clientiId) {
        Objects.requireNonNull(attivitaId, "attivitaId");
        LocalDateTime now = LocalDateTime.now();
        clientiId.stream()
                .filter(Objects::nonNull)
                .distinct()
                .filter(clienteId -> attivitaClienteRepository.findByAttivitaIdAndClienteId(attivitaId, clienteId).isEmpty())
                .map(clienteId -> new AttivitaCliente(
                        UUID.randomUUID(),
                        attivitaId,
                        clienteId,
                        STATO_CLIENTE_DA_FARE,
                        null,
                        now,
                        now
                ))
                .forEach(attivitaClienteRepository::insert);
    }

    public void aggiornaStatoCliente(UUID attivitaId, UUID clienteId, String stato, UUID interazioneId) {
        AttivitaCliente attivitaCliente = attivitaClienteRepository.findByAttivitaIdAndClienteId(attivitaId, clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non collegato all'attività selezionata."));
        attivitaClienteRepository.update(new AttivitaCliente(
                attivitaCliente.id(),
                attivitaCliente.attivitaId(),
                attivitaCliente.clienteId(),
                cleanStatoCliente(stato),
                interazioneId,
                attivitaCliente.createdAt(),
                LocalDateTime.now()
        ));
    }

    public void registraChiamataAttivita(ChiamataAttivitaInput input) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(input.clienteId(), "clienteId");
        Objects.requireNonNull(input.attivitaId(), "attivitaId");
        String statoCliente = cleanStatoCliente(input.statoCliente());
        LocalDateTime now = LocalDateTime.now();
        NotaCliente nota = null;
        if (input.testo() != null && !input.testo().isBlank()) {
            nota = new NotaCliente(
                    UUID.randomUUID(),
                    input.clienteId(),
                    currentOperatoreService.currentOperatoreId(),
                    input.testo().trim(),
                    now,
                    null
            );
        }
        Interazione interazione = new Interazione(
                UUID.randomUUID(),
                input.clienteId(),
                currentOperatoreService.currentOperatoreId(),
                nota == null ? null : nota.id(),
                input.attivitaId(),
                LocalDate.now(),
                input.prossimoContatto(),
                BigDecimal.ZERO,
                now,
                null
        );
        clientePersistenceService.addChiamataAttivita(nota, interazione, statoCliente);
    }

    private String requireText(String value, String fieldName) {
        String cleanValue = clean(value);
        if (cleanValue.isBlank()) {
            throw new IllegalArgumentException("Il campo " + fieldName + " è obbligatorio.");
        }
        return cleanValue;
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private Integer cleanPriorita(Integer priorita) {
        if (priorita == null) {
            return 2;
        }
        if (priorita < 1 || priorita > 3) {
            throw new IllegalArgumentException("La priorità deve essere compresa tra 1 e 3.");
        }
        return priorita;
    }

    private String cleanStatoAttivita(String stato) {
        String cleanStato = clean(stato);
        return cleanStato.isBlank() ? STATO_ATTIVITA_BOZZA : cleanStato;
    }

    private String cleanStatoCliente(String stato) {
        String cleanStato = requireText(stato, "stato");
        if (STATO_CLIENTE_DA_FARE.equals(cleanStato)
                || STATO_CLIENTE_IN_CORSO.equals(cleanStato)
                || STATO_CLIENTE_SOSPESO.equals(cleanStato)
                || STATO_CLIENTE_COMPLETATO.equals(cleanStato)
                || STATO_CLIENTE_ANNULLATO.equals(cleanStato)) {
            return cleanStato;
        }
        throw new IllegalArgumentException("Stato cliente attività non valido.");
    }

    public record TipoAttivitaOption(UUID id, String nome) {
        public TipoAttivitaOption(TipoAttivita tipoAttivita) {
            this(tipoAttivita.id(), tipoAttivita.nome());
        }
    }

    public record AttivitaCreateInput(
            String titolo,
            String descrizione,
            Integer priorita,
            String stato,
            LocalDate dataInizio,
            LocalDate dataFine,
            UUID tipoAttivitaId,
            List<UUID> clientiId
    ) {
        public AttivitaCreateInput {
            clientiId = clientiId == null ? List.of() : List.copyOf(clientiId);
        }
    }

    public record ChiamataAttivitaInput(
            UUID clienteId,
            UUID attivitaId,
            String statoCliente,
            String testo,
            LocalDate prossimoContatto
    ) {
    }
}

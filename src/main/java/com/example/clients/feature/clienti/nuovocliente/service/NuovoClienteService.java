package com.example.clients.feature.clienti.nuovocliente.service;

import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.model.ClienteAggregate;
import com.example.clients.core.database.model.ContattoCliente;
import com.example.clients.core.database.model.EmailCliente;
import com.example.clients.core.database.model.IndirizzoCliente;
import com.example.clients.core.database.model.SitoWebCliente;
import com.example.clients.core.database.model.TelefonoCliente;
import com.example.clients.core.database.service.ClientePersistenceService;
import com.example.clients.core.database.service.CurrentOperatoreService;
import com.example.clients.feature.clienti.nuovocliente.dto.ContattoClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.EmailClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.IndirizzoClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.NuovoClienteRequest;
import com.example.clients.feature.clienti.nuovocliente.dto.SitoWebClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.TelefonoClienteInput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class NuovoClienteService {

    private final ClientePersistenceService persistenceService;
    private final CurrentOperatoreService currentOperatoreService;
    private NuovoClienteDraft lastPreparedDraft;

    public NuovoClienteService(ClientePersistenceService persistenceService) {
        this(persistenceService, new CurrentOperatoreService());
    }

    public NuovoClienteService(ClientePersistenceService persistenceService, CurrentOperatoreService currentOperatoreService) {
        this.persistenceService = persistenceService;
        this.currentOperatoreService = currentOperatoreService;
    }

    public NuovoClienteDraft saveCliente(NuovoClienteRequest request) {
        NuovoClienteDraft draft = prepareCliente(request);
        persistenceService.saveNuovoCliente(draft.toAggregate());
        return draft;
    }

    public NuovoClienteDraft prepareCliente(NuovoClienteRequest request) {
        UUID clienteId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = new Cliente(
                clienteId,
                clean(request.cliente().ragioneSociale()),
                clean(request.cliente().tipoCliente()),
                clean(request.cliente().statoTrattativa()),
                clean(request.cliente().partitaIva()),
                clean(request.cliente().codiceFiscale()),
                request.cliente().acquisizione(),
                currentOperatoreService.currentOperatoreId(),
                now,
                null
        );

        List<ContattoCliente> contatti = new ArrayList<>();
        List<TelefonoCliente> telefoniCollegati = new ArrayList<>();
        List<EmailCliente> emailCollegate = new ArrayList<>();
        Set<String> telefoniUsatiDaiContatti = new HashSet<>();
        Set<String> emailUsateDaiContatti = new HashSet<>();

        for (ContattoClienteInput input : request.contatti()) {
            String descrizione = clean(input.descrizione());
            List<String> telefoniContatto = cleanTelefonoInput(input.telefoni());
            List<String> emailContatto = cleanEmailInput(input.email());
            if (descrizione == null && telefoniContatto.isEmpty() && emailContatto.isEmpty()) {
                continue;
            }

            UUID contattoId = UUID.randomUUID();
            contatti.add(new ContattoCliente(contattoId, clienteId, descrizione));
            for (String telefono : telefoniContatto) {
                telefoniUsatiDaiContatti.add(key(telefono));
                telefoniCollegati.add(new TelefonoCliente(UUID.randomUUID(), clienteId, contattoId, telefono));
            }
            for (String email : emailContatto) {
                emailUsateDaiContatti.add(key(email));
                emailCollegate.add(new EmailCliente(UUID.randomUUID(), clienteId, contattoId, email));
            }
        }

        List<IndirizzoCliente> indirizzi = createIndirizzi(clienteId, request.indirizzi(), now);
        List<SitoWebCliente> sitiWeb = createSitiWeb(clienteId, request.sitiWeb());
        List<TelefonoCliente> telefoni = new ArrayList<>(telefoniCollegati);
        Set<String> telefoniGenerici = new HashSet<>(telefoniUsatiDaiContatti);
        request.telefoni().stream()
                .map(TelefonoClienteInput::descrizione)
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .filter(value -> telefoniGenerici.add(key(value)))
                .map(value -> new TelefonoCliente(UUID.randomUUID(), clienteId, null, value))
                .forEach(telefoni::add);
        List<EmailCliente> email = new ArrayList<>(emailCollegate);
        Set<String> emailGeneriche = new HashSet<>(emailUsateDaiContatti);
        request.email().stream()
                .map(EmailClienteInput::descrizione)
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .filter(value -> emailGeneriche.add(key(value)))
                .map(value -> new EmailCliente(UUID.randomUUID(), clienteId, null, value))
                .forEach(email::add);

        lastPreparedDraft = new NuovoClienteDraft(cliente, indirizzi, sitiWeb, telefoni, email, contatti);
        return lastPreparedDraft;
    }

    public Optional<NuovoClienteDraft> getLastPreparedDraft() {
        return Optional.ofNullable(lastPreparedDraft);
    }

    private static List<IndirizzoCliente> createIndirizzi(
            UUID clienteId,
            List<IndirizzoClienteInput> input,
            LocalDateTime now
    ) {
        return input.stream()
                .filter(NuovoClienteService::hasAddressData)
                .map(indirizzo -> new IndirizzoCliente(
                        UUID.randomUUID(),
                        clienteId,
                        clean(indirizzo.paese()),
                        clean(indirizzo.regione()),
                        clean(indirizzo.provincia()),
                        clean(indirizzo.citta()),
                        clean(indirizzo.indirizzo()),
                        clean(indirizzo.numeroCivico()),
                        clean(indirizzo.cap()),
                        indirizzo.principale(),
                        now,
                        null
                ))
                .toList();
    }

    private static boolean hasAddressData(IndirizzoClienteInput input) {
        return clean(input.paese()) != null
                || clean(input.regione()) != null
                || clean(input.provincia()) != null
                || clean(input.citta()) != null
                || clean(input.indirizzo()) != null
                || clean(input.numeroCivico()) != null
                || clean(input.cap()) != null;
    }

    private static List<SitoWebCliente> createSitiWeb(UUID clienteId, List<SitoWebClienteInput> input) {
        return input.stream()
                .map(SitoWebClienteInput::descrizione)
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .distinct()
                .map(value -> new SitoWebCliente(UUID.randomUUID(), clienteId, value))
                .toList();
    }

    private static List<String> cleanTelefonoInput(List<TelefonoClienteInput> input) {
        return input.stream()
                .map(TelefonoClienteInput::descrizione)
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private static List<String> cleanEmailInput(List<EmailClienteInput> input) {
        return input.stream()
                .map(EmailClienteInput::descrizione)
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    private static String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String key(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public record NuovoClienteDraft(
            Cliente cliente,
            List<IndirizzoCliente> indirizzi,
            List<SitoWebCliente> sitiWeb,
            List<TelefonoCliente> telefoni,
            List<EmailCliente> email,
            List<ContattoCliente> contatti
    ) {
        public NuovoClienteDraft {
            indirizzi = List.copyOf(indirizzi);
            sitiWeb = List.copyOf(sitiWeb);
            telefoni = List.copyOf(telefoni);
            email = List.copyOf(email);
            contatti = List.copyOf(contatti);
        }

        private ClienteAggregate toAggregate() {
            return new ClienteAggregate(cliente, indirizzi, sitiWeb, telefoni, email, contatti);
        }
    }
}

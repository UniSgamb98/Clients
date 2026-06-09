package com.example.clients.feature.clienti.nuovocliente.service;

import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.model.ContattoCliente;
import com.example.clients.core.database.model.EmailCliente;
import com.example.clients.core.database.model.IndirizzoCliente;
import com.example.clients.core.database.model.SitoWebCliente;
import com.example.clients.core.database.model.TelefonoCliente;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class NuovoClienteService {

    private NuovoClienteDraft lastPreparedDraft;

    public NuovoClienteDraft saveCliente(NuovoClienteFormData formData) {
        return prepareCliente(formData);
    }

    public NuovoClienteDraft prepareCliente(NuovoClienteFormData formData) {
        UUID clienteId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Cliente cliente = new Cliente(
                clienteId,
                clean(formData.ragioneSociale()),
                clean(formData.tipoCliente()),
                clean(formData.statoTrattativa()),
                clean(formData.partitaIva()),
                clean(formData.codiceFiscale()),
                parseDate(formData.acquisizione()),
                null,
                now,
                null
        );

        List<ContattoCliente> contatti = new ArrayList<>();
        List<TelefonoCliente> telefoniCollegati = new ArrayList<>();
        List<EmailCliente> emailCollegate = new ArrayList<>();
        Set<String> telefoniUsatiDaiContatti = new HashSet<>();
        Set<String> emailUsateDaiContatti = new HashSet<>();

        for (ContattoInput input : formData.contatti()) {
            String descrizione = clean(input.descrizione());
            String telefono = clean(input.telefono());
            String email = clean(input.email());
            if (descrizione == null && telefono == null && email == null) {
                continue;
            }

            UUID contattoId = UUID.randomUUID();
            contatti.add(new ContattoCliente(contattoId, clienteId, descrizione));
            if (telefono != null) {
                telefoniUsatiDaiContatti.add(key(telefono));
                telefoniCollegati.add(new TelefonoCliente(UUID.randomUUID(), clienteId, contattoId, telefono));
            }
            if (email != null) {
                emailUsateDaiContatti.add(key(email));
                emailCollegate.add(new EmailCliente(UUID.randomUUID(), clienteId, contattoId, email));
            }
        }

        List<IndirizzoCliente> indirizzi = createIndirizzi(clienteId, formData, now);
        List<SitoWebCliente> sitiWeb = formData.sitiWeb().stream()
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .map(value -> new SitoWebCliente(UUID.randomUUID(), clienteId, value))
                .toList();
        List<TelefonoCliente> telefoni = new ArrayList<>(telefoniCollegati);
        formData.telefoni().stream()
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .filter(value -> !telefoniUsatiDaiContatti.contains(key(value)))
                .map(value -> new TelefonoCliente(UUID.randomUUID(), clienteId, null, value))
                .forEach(telefoni::add);
        List<EmailCliente> email = new ArrayList<>(emailCollegate);
        formData.email().stream()
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .filter(value -> !emailUsateDaiContatti.contains(key(value)))
                .map(value -> new EmailCliente(UUID.randomUUID(), clienteId, null, value))
                .forEach(email::add);

        lastPreparedDraft = new NuovoClienteDraft(cliente, indirizzi, sitiWeb, telefoni, email, contatti);
        return lastPreparedDraft;
    }

    public Optional<NuovoClienteDraft> getLastPreparedDraft() {
        return Optional.ofNullable(lastPreparedDraft);
    }

    private static List<IndirizzoCliente> createIndirizzi(UUID clienteId, NuovoClienteFormData formData, LocalDateTime now) {
        List<IndirizzoCliente> indirizzi = new ArrayList<>();
        if (hasPrimaryAddress(formData)) {
            indirizzi.add(new IndirizzoCliente(
                    UUID.randomUUID(),
                    clienteId,
                    clean(formData.paese()),
                    clean(formData.regione()),
                    clean(formData.provincia()),
                    clean(formData.citta()),
                    clean(formData.indirizzo()),
                    clean(formData.numeroCivico()),
                    clean(formData.cap()),
                    true,
                    now,
                    null
            ));
        }
        formData.altriIndirizzi().stream()
                .map(NuovoClienteService::clean)
                .filter(value -> value != null)
                .map(value -> new IndirizzoCliente(UUID.randomUUID(), clienteId, null, null, null, null, value, null, null, false, now, null))
                .forEach(indirizzi::add);
        return indirizzi;
    }

    private static boolean hasPrimaryAddress(NuovoClienteFormData formData) {
        return clean(formData.paese()) != null
                || clean(formData.regione()) != null
                || clean(formData.provincia()) != null
                || clean(formData.citta()) != null
                || clean(formData.indirizzo()) != null
                || clean(formData.numeroCivico()) != null
                || clean(formData.cap()) != null;
    }

    private static LocalDate parseDate(String value) {
        String cleaned = clean(value);
        if (cleaned == null) {
            return null;
        }
        return LocalDate.parse(cleaned);
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

    public record NuovoClienteFormData(
            String ragioneSociale,
            String tipoCliente,
            String statoTrattativa,
            String partitaIva,
            String codiceFiscale,
            String acquisizione,
            String operatore,
            String paese,
            String regione,
            String provincia,
            String citta,
            String indirizzo,
            String numeroCivico,
            String cap,
            List<String> altriIndirizzi,
            List<String> sitiWeb,
            List<String> email,
            List<String> telefoni,
            List<ContattoInput> contatti
    ) {
        public NuovoClienteFormData {
            altriIndirizzi = List.copyOf(altriIndirizzi);
            sitiWeb = List.copyOf(sitiWeb);
            email = List.copyOf(email);
            telefoni = List.copyOf(telefoni);
            contatti = List.copyOf(contatti);
        }
    }

    public record ContattoInput(String descrizione, String telefono, String email) {
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
    }
}

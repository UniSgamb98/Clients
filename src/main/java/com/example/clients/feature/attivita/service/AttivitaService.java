package com.example.clients.feature.attivita.service;

import com.example.clients.core.database.Database;
import com.example.clients.core.database.model.Attivita;
import com.example.clients.core.database.model.AttivitaCliente;
import com.example.clients.core.database.model.Cliente;
import com.example.clients.core.database.repository.AttivitaClienteRepository;
import com.example.clients.core.database.repository.AttivitaRepository;
import com.example.clients.core.database.repository.ClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyAttivitaClienteRepository;
import com.example.clients.core.database.repository.derby.DerbyAttivitaRepository;
import com.example.clients.core.database.repository.derby.DerbyClienteRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AttivitaService {

    private final AttivitaRepository attivitaRepository;
    private final AttivitaClienteRepository attivitaClienteRepository;
    private final ClienteRepository clienteRepository;

    public AttivitaService(Database database) {
        this(
                new DerbyAttivitaRepository(database),
                new DerbyAttivitaClienteRepository(database),
                new DerbyClienteRepository(database)
        );
    }

    public AttivitaService(
            AttivitaRepository attivitaRepository,
            AttivitaClienteRepository attivitaClienteRepository,
            ClienteRepository clienteRepository
    ) {
        this.attivitaRepository = attivitaRepository;
        this.attivitaClienteRepository = attivitaClienteRepository;
        this.clienteRepository = clienteRepository;
    }

    public AttivitaWorkspace loadWorkspace() {
        List<AttivitaItem> attivita = attivitaRepository.findAll().stream()
                .map(this::toAttivitaItem)
                .toList();
        List<ClienteItem> clienti = clienteRepository.findAll().stream()
                .map(this::toClienteItem)
                .toList();
        return new AttivitaWorkspace(attivita, clienti);
    }

    public List<ClienteItem> loadClientiAttivita(UUID attivitaId, List<ClienteItem> clienti) {
        if (attivitaId == null) {
            return List.of();
        }

        Set<UUID> clientiIds = attivitaClienteRepository.findByAttivitaId(attivitaId).stream()
                .map(AttivitaCliente::clienteId)
                .collect(Collectors.toSet());
        Map<UUID, ClienteItem> clientiById = clienti.stream()
                .collect(Collectors.toMap(ClienteItem::id, Function.identity()));
        return clientiIds.stream()
                .map(clientiById::get)
                .filter(cliente -> cliente != null)
                .sorted((first, second) -> first.ragioneSociale().compareToIgnoreCase(second.ragioneSociale()))
                .toList();
    }

    private AttivitaItem toAttivitaItem(Attivita attivita) {
        return new AttivitaItem(
                attivita.id(),
                valueOrDefault(attivita.titolo(), "Attività senza titolo"),
                valueOrDefault(attivita.stato(), ""),
                attivita.dataFine()
        );
    }

    private ClienteItem toClienteItem(Cliente cliente) {
        return new ClienteItem(
                cliente.id(),
                valueOrDefault(cliente.ragioneSociale(), "Cliente senza nome"),
                valueOrDefault(cliente.statoTrattativa(), "")
        );
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    public record AttivitaWorkspace(List<AttivitaItem> attivita, List<ClienteItem> clienti) {
        public AttivitaWorkspace {
            attivita = List.copyOf(attivita);
            clienti = List.copyOf(clienti);
        }
    }

    public record AttivitaItem(UUID id, String titolo, String stato, LocalDate dataFine) {
        @Override
        public String toString() {
            String dueDate = dataFine == null ? "" : " · entro " + dataFine;
            String status = stato == null || stato.isBlank() ? "" : " · " + stato;
            return titolo + status + dueDate;
        }
    }

    public record ClienteItem(UUID id, String ragioneSociale, String statoTrattativa) {
        @Override
        public String toString() {
            return statoTrattativa == null || statoTrattativa.isBlank()
                    ? ragioneSociale
                    : ragioneSociale + " · " + statoTrattativa;
        }
    }
}

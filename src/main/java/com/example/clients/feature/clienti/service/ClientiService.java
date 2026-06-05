package com.example.clients.feature.clienti.service;

import java.util.List;

public class ClientiService {

    public List<ClientePreview> getClientiPreview() {
        return List.of(
                new ClientePreview("Rossi S.r.l.", "Azienda", "Mario Rossi", "02 123456", "info@rossi.it", "Attivo"),
                new ClientePreview("Bianchi Studio", "Studio", "Laura Bianchi", "011 987654", "laura@bianchi.it", "Prospect"),
                new ClientePreview("Verdi Impianti", "Azienda", "Giulia Verdi", "049 445566", "amministrazione@verdi.it", "Attivo"),
                new ClientePreview("Neri Consulting", "Partner", "Paolo Neri", "051 778899", "paolo@nericonsulting.it", "Inattivo")
        );
    }

    public record ClientePreview(
            String name,
            String type,
            String contact,
            String phone,
            String email,
            String status
    ) {
    }
}

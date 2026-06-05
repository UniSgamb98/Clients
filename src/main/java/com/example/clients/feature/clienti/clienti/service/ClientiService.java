package com.example.clients.feature.clienti.clienti.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ClientiService {

    private SortColumn lastSortColumn;
    private boolean ascending = true;

    public List<ClientePreview> getClientiPreview() {
        return new ArrayList<>(getMockClienti());
    }

    public List<ClientePreview> sortClientiBy(SortColumn sortColumn) {
        if (sortColumn == lastSortColumn) {
            ascending = !ascending;
        } else {
            lastSortColumn = sortColumn;
            ascending = true;
        }

        Comparator<ClientePreview> comparator = Comparator.comparing(
                sortColumn.getValueExtractor(),
                String.CASE_INSENSITIVE_ORDER
        );

        if (!ascending) {
            comparator = comparator.reversed();
        }

        return getMockClienti().stream()
                .sorted(comparator)
                .toList();
    }

    private List<ClientePreview> getMockClienti() {
        return List.of(
                new ClientePreview("Rossi S.r.l.", "Azienda", "Mario Rossi", "02 123456", "info@rossi.it", "Attivo"),
                new ClientePreview("Bianchi Studio", "Studio", "Laura Bianchi", "011 987654", "laura@bianchi.it", "Prospect"),
                new ClientePreview("Verdi Impianti", "Azienda", "Giulia Verdi", "049 445566", "amministrazione@verdi.it", "Attivo"),
                new ClientePreview("Neri Consulting", "Partner", "Paolo Neri", "051 778899", "paolo@nericonsulting.it", "Inattivo"),
                new ClientePreview("Blu Service", "Azienda", "Elena Costa", "02 334455", "elena@bluservice.it", "Attivo"),
                new ClientePreview("Studio Gamma", "Studio", "Andrea Galli", "011 445566", "andrea@studiogamma.it", "Prospect"),
                new ClientePreview("Alfa Retail", "Azienda", "Sara Ferri", "049 667788", "sara@alfaretail.it", "Attivo"),
                new ClientePreview("Beta Logistica", "Azienda", "Luca Romano", "051 889900", "luca@betalogistica.it", "Attivo"),
                new ClientePreview("Delta Design", "Partner", "Marta Villa", "06 112233", "marta@deltadesign.it", "Prospect"),
                new ClientePreview("Eco Energia", "Azienda", "Franco Riva", "045 998877", "franco@ecoenergia.it", "Attivo"),
                new ClientePreview("Futura Labs", "Startup", "Chiara Ricci", "02 776655", "chiara@futuralabs.it", "Prospect"),
                new ClientePreview("Hotel Aurora", "Azienda", "Davide Serra", "041 554433", "direzione@hotelaurora.it", "Attivo"),
                new ClientePreview("Idea Formazione", "Ente", "Silvia Conte", "011 223344", "silvia@ideaformazione.it", "Inattivo"),
                new ClientePreview("Jolly Market", "Azienda", "Roberto Greco", "080 776611", "roberto@jollymarket.it", "Attivo"),
                new ClientePreview("Kappa Medical", "Azienda", "Valentina Sala", "02 665544", "valentina@kappamedical.it", "Attivo"),
                new ClientePreview("Linea Casa", "Retail", "Marco Fontana", "030 443322", "marco@lineacasa.it", "Prospect"),
                new ClientePreview("Mondo Verde", "Azienda", "Irene Barbieri", "055 112244", "irene@mondoverde.it", "Attivo"),
                new ClientePreview("Nord Tech", "Partner", "Giorgio Fabbri", "0461 334455", "giorgio@nordtech.it", "Attivo"),
                new ClientePreview("Omega Finance", "Azienda", "Claudia Martini", "02 881122", "claudia@omegafinance.it", "Prospect"),
                new ClientePreview("Punto Salute", "Studio", "Federico Amato", "081 667788", "federico@puntosalute.it", "Attivo"),
                new ClientePreview("Quick Office", "Fornitore", "Anna Lombardi", "0521 778899", "anna@quickoffice.it", "Inattivo"),
                new ClientePreview("Rete Sicura", "Azienda", "Matteo Moretti", "010 334411", "matteo@retesicura.it", "Attivo"),
                new ClientePreview("Sole Viaggi", "Azienda", "Noemi Longo", "070 998811", "noemi@soleviaggi.it", "Prospect"),
                new ClientePreview("Tekno Point", "Partner", "Alessandro Rizzi", "02 119900", "alessandro@teknopoint.it", "Attivo")
        );
    }

    public enum SortColumn {
        NAME(ClientePreview::name),
        TYPE(ClientePreview::type),
        CONTACT(ClientePreview::contact),
        PHONE(ClientePreview::phone),
        EMAIL(ClientePreview::email),
        STATUS(ClientePreview::status);

        private final Function<ClientePreview, String> valueExtractor;

        SortColumn(Function<ClientePreview, String> valueExtractor) {
            this.valueExtractor = valueExtractor;
        }

        private Function<ClientePreview, String> getValueExtractor() {
            return valueExtractor;
        }
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

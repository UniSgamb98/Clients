package com.example.clients.feature.clienti.schedacliente.service;

import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionPreview;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionType;

import java.time.LocalDate;

public class SchedaClientePersistenceService {

    public InteractionPreview salvaNota(String testo) {
        return new InteractionPreview(LocalDate.now(), InteractionType.NOTA, null, testo.trim());
    }

    public InteractionPreview salvaChiamata(String testo, LocalDate prossimoContatto) {
        String descrizione = testo == null || testo.isBlank() ? "Chiamata registrata." : testo.trim();
        return new InteractionPreview(LocalDate.now(), InteractionType.CHIAMATA, prossimoContatto, descrizione);
    }
}

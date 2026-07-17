package com.example.clients.feature.impostazioni.controller;

import com.example.clients.feature.impostazioni.service.ImpostazioniService;
import com.example.clients.feature.impostazioni.view.ImpostazioniView;

public class ImpostazioniController {

    private final ImpostazioniView view;
    private final ImpostazioniService service;

    public ImpostazioniController(ImpostazioniView view, ImpostazioniService service) {
        this.view = view;
        this.service = service;
    }

    public ImpostazioniView getView() {
        return view;
    }

    public ImpostazioniService getService() {
        return service;
    }
}

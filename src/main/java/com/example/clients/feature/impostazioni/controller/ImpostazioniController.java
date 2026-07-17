package com.example.clients.feature.impostazioni.controller;

import com.example.clients.feature.impostazioni.service.ImpostazioniService;
import com.example.clients.feature.impostazioni.view.ImpostazioniView;
import com.example.clients.core.async.AsyncLoader;

public class ImpostazioniController {

    private final ImpostazioniView view;
    private final ImpostazioniService service;

    public ImpostazioniController(ImpostazioniView view, ImpostazioniService service) {
        this.view = view;
        this.service = service;
        view.onForniEditChanged(editing -> {
            if (!editing) {
                AsyncLoader.run(() -> { service.saveForni(view.getForni()); return true; }, ignored -> { }, error -> view.showForniSaveError());
            }
        });
        view.onAddForno(view::addEmptyForno);
        AsyncLoader.run(service::getForni, view::setForni, error -> view.setForni(java.util.List.of()));
    }

    public ImpostazioniView getView() {
        return view;
    }

    public ImpostazioniService getService() {
        return service;
    }
}

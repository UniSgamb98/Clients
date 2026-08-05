package com.example.clients.feature.impostazioni.controller;

import com.example.clients.feature.impostazioni.service.ImpostazioniService;
import com.example.clients.feature.impostazioni.view.ImpostazioniView;
import com.example.clients.core.async.AsyncLoader;
import com.example.clients.feature.impostazioni.view.ImpostazioniEditorSection;
import java.util.List;

public class ImpostazioniController {

    private final ImpostazioniView view;
    private final ImpostazioniService service;

    public ImpostazioniController(ImpostazioniView view, ImpostazioniService service) {
        this.view = view;
        this.service = service;
        configure("MATERIALI_DI_CONSUMO", List.of("MATERIALE", "MARCHIO", "MODELLO"));
        configure("CANALI_DI_ACQUISTO", List.of("MODALITA", "NOTA"));
        configure("FRESATORI", List.of("MARCA", "MODELLO"));
        configure("FORNI", List.of("TECNOLOGIA", "ANNO", "MARCA", "MODELLO"));
    }

    private void configure(String table, List<String> columns) {
        ImpostazioniEditorSection editor = view.getEditor(table);
        editor.onAdd(editor::addEmpty);
        editor.onEditChanged(editing -> { if (!editing) AsyncLoader.run(() -> { service.saveVoci(table, columns, editor.getVoci()); return true; }, ignored -> { }, error -> { }); });
        AsyncLoader.run(() -> service.getVoci(table, columns), editor::setVoci, error -> editor.setVoci(List.of()));
    }

    public ImpostazioniView getView() {
        return view;
    }

    public ImpostazioniService getService() {
        return service;
    }
}

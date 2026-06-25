package com.example.clients.feature.attivita.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaListRecord;
import com.example.clients.feature.attivita.service.AttivitaService;
import com.example.clients.feature.attivita.view.AttivitaView;

import java.util.List;
import java.util.UUID;

public class AttivitaController {

    private final AttivitaView view;
    private final AttivitaService service;
    private long loadVersion;
    private long detailVersion;

    public AttivitaController(AttivitaView view, AttivitaService service) {
        this.view = view;
        this.service = service;
        configureActions();
    }

    private void configureActions() {
        view.getNewActivityButton().setOnAction(event -> view.showDetailError("La creazione guidata attività sarà il prossimo step."));
    }

    public void loadAttivitaAsync() {
        long version = ++loadVersion;
        view.showLoading();
        AsyncLoader.run(
                service::listaAttivita,
                activities -> {
                    if (version == loadVersion) {
                        renderActivities(activities);
                    }
                },
                error -> {
                    if (version == loadVersion) {
                        view.showError("Caricamento attività non riuscito.");
                    }
                }
        );
    }

    private void renderActivities(List<AttivitaListRecord> activities) {
        if (activities.isEmpty()) {
            view.showEmpty();
            return;
        }
        view.clearActivities();
        for (AttivitaListRecord activity : activities) {
            view.addActivityRow(activity, this::loadDetailAsync);
        }
    }

    private void loadDetailAsync(UUID attivitaId) {
        long version = ++detailVersion;
        view.showDetailLoading();
        AsyncLoader.run(
                () -> service.dettaglioAttivita(attivitaId),
                detail -> {
                    if (version == detailVersion) {
                        view.showDetail(detail);
                    }
                },
                error -> {
                    if (version == detailVersion) {
                        view.showDetailError("Dettaglio attività non disponibile.");
                    }
                }
        );
    }
}

package com.example.clients.feature.attivita.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.core.database.query.AttivitaQuery.AttivitaListRecord;
import com.example.clients.feature.attivita.service.AttivitaService;
import com.example.clients.feature.attivita.service.AttivitaService.AttivitaCreateInput;
import com.example.clients.feature.attivita.view.AttivitaView;
import com.example.clients.feature.attivita.view.AttivitaView.TipoAttivitaOption;

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
        view.getNewActivityButton().setOnAction(event -> showCreateForm());
        view.getCancelActivityButton().setOnAction(event -> view.showDetailPlaceholder());
        view.getSaveActivityButton().setOnAction(event -> createActivityAsync());
    }

    public void showCreateForm() {
        detailVersion++;
        view.showCreateForm();
    }

    public void loadAttivitaAsync() {
        loadTipoAttivitaOptionsAsync();
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

    private void loadTipoAttivitaOptionsAsync() {
        AsyncLoader.run(
                service::listaTipiAttivita,
                options -> view.setTipoAttivitaOptions(options.stream()
                        .map(option -> new TipoAttivitaOption(option.id(), option.nome()))
                        .toList()),
                error -> view.setTipoAttivitaOptions(List.of())
        );
    }

    private void createActivityAsync() {
        AttivitaCreateInput input = new AttivitaCreateInput(
                view.getActivityTitle(),
                view.getActivityDescription(),
                view.getSelectedPriority(),
                view.getSelectedState(),
                view.getStartDate(),
                view.getEndDate(),
                view.getSelectedTypeId(),
                List.of()
        );
        AsyncLoader.run(
                () -> service.creaAttivita(input),
                detail -> {
                    loadAttivitaAsync();
                    view.showDetail(detail);
                },
                error -> view.showFormError(error.getMessage())
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

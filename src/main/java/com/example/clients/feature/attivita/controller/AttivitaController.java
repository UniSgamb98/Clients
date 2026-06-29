package com.example.clients.feature.attivita.controller;

import com.example.clients.feature.attivita.service.AttivitaService;
import com.example.clients.feature.attivita.service.AttivitaService.AttivitaItem;
import com.example.clients.feature.attivita.service.AttivitaService.AttivitaWorkspace;
import com.example.clients.feature.attivita.service.AttivitaService.ClienteItem;
import com.example.clients.feature.attivita.view.AttivitaView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AttivitaController {

    private final AttivitaView view;
    private final AttivitaService service;
    private List<ClienteItem> allClients = List.of();
    private final ObservableList<ClienteItem> selectedClients = FXCollections.observableArrayList();
    private final ObservableList<ClienteItem> availableClients = FXCollections.observableArrayList();

    public AttivitaController(AttivitaView view, AttivitaService service) {
        this.view = view;
        this.service = service;
        configureActions();
        loadWorkspace();
    }

    private void configureActions() {
        view.getActivitiesListView().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selectActivity(newValue));
        view.getSearchClientField().textProperty().addListener((observable, oldValue, newValue) -> refreshAvailableClients());
        view.getAddClientButton().setOnAction(event -> addSelectedClient());
        view.getRemoveClientButton().setOnAction(event -> removeSelectedClient());
        view.getNewActivityButton().setOnAction(event -> showNewActivityPlaceholder());
        view.getAvailableClientsListView().setItems(availableClients);
        view.getSelectedClientsListView().setItems(selectedClients);
    }

    private void loadWorkspace() {
        try {
            AttivitaWorkspace workspace = service.loadWorkspace();
            allClients = workspace.clienti();
            view.getActivitiesListView().setItems(FXCollections.observableArrayList(workspace.attivita()));
            if (!workspace.attivita().isEmpty()) {
                view.getActivitiesListView().getSelectionModel().selectFirst();
            } else {
                view.getSelectedActivityTitle().setText("Nessuna attività presente");
                selectedClients.clear();
                refreshAvailableClients();
            }
        } catch (RuntimeException e) {
            showError("Caricamento attività non riuscito", e);
        }
    }

    private void selectActivity(AttivitaItem activity) {
        selectedClients.clear();
        if (activity == null) {
            view.getSelectedActivityTitle().setText("Seleziona un'attività");
            refreshAvailableClients();
            return;
        }

        view.getSelectedActivityTitle().setText("Clienti per: " + activity.titolo());
        try {
            selectedClients.setAll(service.loadClientiAttivita(activity.id(), allClients));
            refreshAvailableClients();
        } catch (RuntimeException e) {
            showError("Caricamento clienti attività non riuscito", e);
        }
    }

    private void addSelectedClient() {
        ClienteItem selected = view.getAvailableClientsListView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        selectedClients.add(selected);
        sortSelectedClients();
        view.getAvailableClientsListView().getSelectionModel().clearSelection();
        refreshAvailableClients();
    }

    private void removeSelectedClient() {
        ClienteItem selected = view.getSelectedClientsListView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        selectedClients.remove(selected);
        view.getSelectedClientsListView().getSelectionModel().clearSelection();
        refreshAvailableClients();
    }

    private void refreshAvailableClients() {
        String searchText = cleanSearchText(view.getSearchClientField().getText());
        Set<UUID> selectedIds = selectedClients.stream()
                .map(ClienteItem::id)
                .collect(Collectors.toSet());
        availableClients.setAll(allClients.stream()
                .filter(cliente -> !selectedIds.contains(cliente.id()))
                .filter(cliente -> searchText.isBlank() || cliente.ragioneSociale().toLowerCase().contains(searchText))
                .sorted(Comparator.comparing(ClienteItem::ragioneSociale, String.CASE_INSENSITIVE_ORDER))
                .toList());
    }

    private void sortSelectedClients() {
        List<ClienteItem> sortedClients = new ArrayList<>(selectedClients);
        sortedClients.sort(Comparator.comparing(ClienteItem::ragioneSociale, String.CASE_INSENSITIVE_ORDER));
        selectedClients.setAll(sortedClients);
    }

    private String cleanSearchText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void showNewActivityPlaceholder() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nuova attività");
        alert.setHeaderText("Nuova attività");
        alert.setContentText("La creazione attività verrà collegata in uno step successivo.");
        alert.showAndWait();
    }

    private void showError(String title, RuntimeException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage() == null ? "Errore imprevisto." : e.getMessage());
        alert.showAndWait();
    }
}

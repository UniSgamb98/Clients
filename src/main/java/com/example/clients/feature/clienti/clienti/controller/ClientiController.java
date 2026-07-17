package com.example.clients.feature.clienti.clienti.controller;

import com.example.clients.core.async.AsyncLoader;
import com.example.clients.feature.clienti.clienti.service.ClientiService;
import com.example.clients.feature.clienti.clienti.dto.ClientePreview;
import com.example.clients.feature.clienti.clienti.dto.ClientePreviewRow;
import com.example.clients.feature.clienti.clienti.dto.ClientiPage;
import com.example.clients.feature.clienti.clienti.dto.ClientiSearchRequest;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;
import com.example.clients.feature.clienti.clienti.dto.SortColumn;
import com.example.clients.feature.clienti.clienti.dto.TextFilter;
import com.example.clients.feature.clienti.clienti.view.ClientiView;
import com.example.clients.feature.clienti.navigator.ClientiNav;
import javafx.animation.PauseTransition;
import javafx.scene.control.Alert;
import javafx.util.Duration;

import java.util.List;

public class ClientiController {

    private static final int PAGE_SIZE = 50;
    private static final Duration SEARCH_DEBOUNCE = Duration.millis(300);

    private final ClientiView view;
    private final ClientiNav clientiNav;
    private final ClientiService service;
    private final PauseTransition searchDebounce = new PauseTransition(SEARCH_DEBOUNCE);
    private SortColumn currentSortColumn = SortColumn.NAME;
    private boolean ascending = true;
    private String currentSearchText = "";
    private OperatoreFilter currentOperatoreFilter = OperatoreFilter.empty();
    private TextFilter currentTipoClienteFilter = TextFilter.empty("Tutti");
    private TextFilter currentStatoTrattativaFilter = TextFilter.empty("Tutti");
    private int currentPage;
    private long loadVersion;
    private boolean clearingFilters;

    public ClientiController(ClientiView view, ClientiNav clientiNav, ClientiService service) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
    }

    private void configureActions() {
        view.getNewClientButton().setOnAction(event -> clientiNav.showNuovoCliente());
        view.getNameHeaderButton().setOnAction(event -> sortClienti(SortColumn.NAME));
        view.getTypeHeaderButton().setOnAction(event -> sortClienti(SortColumn.TYPE));
        view.getContactHeaderButton().setOnAction(event -> sortClienti(SortColumn.CONTACT));
        view.getPhoneHeaderButton().setOnAction(event -> sortClienti(SortColumn.PHONE));
        view.getEmailHeaderButton().setOnAction(event -> sortClienti(SortColumn.EMAIL));
        view.getStatusHeaderButton().setOnAction(event -> sortClienti(SortColumn.STATUS));
        view.getPreviousPageButton().setOnAction(event -> loadPage(currentPage - 1));
        view.getNextPageButton().setOnAction(event -> loadPage(currentPage + 1));
        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> searchClienti(newValue));
        view.getOperatorFilterChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> filterByOperatore(newValue));
        view.getTypeFilterChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> filterByTipoCliente(newValue));
        view.getStatusFilterChoiceBox().getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> filterByStatoTrattativa(newValue));
        view.getClearFiltersButton().setOnAction(event -> clearFilters());
        view.getSaveSearchButton().setOnAction(event -> showSaveSearchUnavailable());
    }

    public void loadPreviewClientsAsync() {
        loadFiltersAsync();
        loadPage(0);
    }

    private void loadFiltersAsync() {
        AsyncLoader.run(
                service::getOperatorFilters,
                view::setOperatorFilters,
                error -> view.setOperatorFilters(List.of())
        );
        AsyncLoader.run(
                service::getTipoClienteFilters,
                view::setTypeFilters,
                error -> view.setTypeFilters(List.of())
        );
        AsyncLoader.run(
                service::getStatoTrattativaFilters,
                view::setStatusFilters,
                error -> view.setStatusFilters(List.of())
        );
    }

    private void searchClienti(String searchText) {
        currentSearchText = searchText == null ? "" : searchText.trim();
        if (clearingFilters) {
            return;
        }
        searchDebounce.stop();
        searchDebounce.setOnFinished(event -> loadPage(0));
        searchDebounce.playFromStart();
    }

    private void filterByOperatore(OperatoreFilter operatoreFilter) {
        currentOperatoreFilter = operatoreFilter == null ? OperatoreFilter.empty() : operatoreFilter;
        if (!clearingFilters) {
            loadPage(0);
        }
    }

    private void filterByTipoCliente(TextFilter filter) {
        currentTipoClienteFilter = filter == null ? TextFilter.empty("Tutti") : filter;
        if (!clearingFilters) {
            loadPage(0);
        }
    }

    private void filterByStatoTrattativa(TextFilter filter) {
        currentStatoTrattativaFilter = filter == null ? TextFilter.empty("Tutti") : filter;
        if (!clearingFilters) {
            loadPage(0);
        }
    }

    private void clearFilters() {
        clearingFilters = true;
        searchDebounce.stop();
        view.clearFilters();
        currentSearchText = "";
        currentOperatoreFilter = OperatoreFilter.empty();
        currentTipoClienteFilter = TextFilter.empty("Tutti");
        currentStatoTrattativaFilter = TextFilter.empty("Tutti");
        clearingFilters = false;
        loadPage(0);
    }

    private void showSaveSearchUnavailable() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Funzionalità in sviluppo");
        alert.setHeaderText("Salvataggio ricerca");
        alert.setContentText("Questa funzionalità sarà disponibile prossimamente.");
        alert.showAndWait();
    }

    private void sortClienti(SortColumn sortColumn) {
        if (sortColumn == currentSortColumn) {
            ascending = !ascending;
        } else {
            currentSortColumn = sortColumn;
            ascending = true;
        }
        loadPage(0);
    }

    private void loadPage(int page) {
        currentPage = Math.max(0, page);
        ClientiSearchRequest request = new ClientiSearchRequest(
                currentPage,
                PAGE_SIZE,
                currentSearchText,
                currentOperatoreFilter.id(),
                filterValue(currentTipoClienteFilter),
                filterValue(currentStatoTrattativaFilter),
                currentSortColumn,
                ascending
        );
        long version = ++loadVersion;
        view.showLoading();
        view.setPaginationDisabled(true);

        AsyncLoader.run(
                () -> service.getClientiPreview(request),
                clientiPage -> {
                    if (version == loadVersion) {
                        renderClienti(clientiPage);
                    }
                },
                error -> {
                    if (version == loadVersion) {
                        view.showError("Caricamento clienti non riuscito.");
                        view.setPaginationDisabled(true);
                    }
                }
        );
    }

    private String filterValue(TextFilter filter) {
        return filter == null || filter.isEmptyOption() ? "" : filter.value();
    }

    private void renderClienti(ClientiPage page) {
        currentPage = page.page();
        view.renderPagination(page.page(), page.totalPages(), page.hasPreviousPage(), page.hasNextPage());
        view.setResultsCount(page.totalRows());

        if (page.rows().isEmpty()) {
            view.showEmpty();
            return;
        }

        view.clearClientRows();

        for (ClientePreviewRow cliente : page.rows()) {
            ClientePreview preview = cliente.preview();
            view.addClientRow(
                    preview.name(),
                    preview.type(),
                    preview.contact(),
                    preview.phone(),
                    preview.email(),
                    preview.status()
            ).setOnMouseClicked(event -> clientiNav.showSchedaCliente(cliente.clienteId()));
        }
    }

    public ClientiView getView() {
        return view;
    }

    public ClientiNav getClientiNav() {
        return clientiNav;
    }

    public ClientiService getService() {
        return service;
    }
}

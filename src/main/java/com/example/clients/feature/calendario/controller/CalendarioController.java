package com.example.clients.feature.calendario.controller;

import com.example.clients.feature.calendario.service.CalendarioService;
import com.example.clients.feature.calendario.view.CalendarioView;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;

import java.time.YearMonth;
import java.util.UUID;
import java.util.function.Consumer;

public class CalendarioController {

    private final CalendarioView view;
    private final CalendarioService service;
    private final Consumer<UUID> openClienteHandler;
    private YearMonth displayedMonth;
    private OperatoreFilter selectedOperatore = OperatoreFilter.empty();

    public CalendarioController(CalendarioView view, CalendarioService service, Consumer<UUID> openClienteHandler) {
        this.view = view;
        this.service = service;
        this.openClienteHandler = openClienteHandler;
        displayedMonth = service.currentMonth();

        view.setOperatorFilters(service.getOperatorFilters());
        view.getOperatorFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedOperatore = newValue == null ? OperatoreFilter.empty() : newValue;
            refreshMonth();
        });
        view.setOpenCallHandler(call -> this.openClienteHandler.accept(call.clienteId()));
        view.getPreviousYearButton().setOnAction(event -> showMonth(displayedMonth.minusYears(1)));
        view.getPreviousMonthButton().setOnAction(event -> showMonth(displayedMonth.minusMonths(1)));
        view.getNextMonthButton().setOnAction(event -> showMonth(displayedMonth.plusMonths(1)));
        view.getNextYearButton().setOnAction(event -> showMonth(displayedMonth.plusYears(1)));
        refreshMonth();
    }

    private void showMonth(YearMonth month) {
        displayedMonth = month;
        refreshMonth();
    }

    private void refreshMonth() {
        view.showMonth(displayedMonth, service.getMonth(displayedMonth, selectedOperatore.id()));
    }

    public CalendarioView getView() {
        return view;
    }

    public CalendarioService getService() {
        return service;
    }
}

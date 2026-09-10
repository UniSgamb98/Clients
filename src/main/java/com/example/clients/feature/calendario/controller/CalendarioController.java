package com.example.clients.feature.calendario.controller;

import com.example.clients.feature.calendario.service.CalendarioService;
import com.example.clients.feature.calendario.view.CalendarioView;
import com.example.clients.feature.calendario.dto.CalendarioDisplayMode;
import com.example.clients.feature.clienti.clienti.dto.OperatoreFilter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import java.util.function.Consumer;

public class CalendarioController {

    private final CalendarioView view;
    private final CalendarioService service;
    private final Consumer<UUID> openClienteHandler;
    private LocalDate referenceDate;
    private CalendarioDisplayMode displayMode = CalendarioDisplayMode.MONTH;
    private OperatoreFilter selectedOperatore = OperatoreFilter.empty();

    public CalendarioController(CalendarioView view, CalendarioService service, Consumer<UUID> openClienteHandler) {
        this.view = view;
        this.service = service;
        this.openClienteHandler = openClienteHandler;
        referenceDate = service.currentDate();

        view.setOperatorFilters(service.getOperatorFilters());
        view.getOperatorFilterChoiceBox().getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedOperatore = newValue == null ? OperatoreFilter.empty() : newValue;
            refresh();
        });
        view.setOpenCallHandler(call -> this.openClienteHandler.accept(call.clienteId()));
        view.setSelectDateHandler(date -> referenceDate = date);
        view.getPreviousYearButton().setOnAction(event -> moveYears(-1));
        view.getPreviousMonthButton().setOnAction(event -> movePeriod(-1));
        view.getNextMonthButton().setOnAction(event -> movePeriod(1));
        view.getNextYearButton().setOnAction(event -> moveYears(1));
        view.getMonthViewButton().setOnAction(event -> changeMode(CalendarioDisplayMode.MONTH));
        view.getWeekViewButton().setOnAction(event -> changeMode(CalendarioDisplayMode.WEEK));
        refresh();
    }

    private void changeMode(CalendarioDisplayMode mode) {
        displayMode = mode;
        refresh();
    }

    private void movePeriod(int direction) {
        referenceDate = displayMode == CalendarioDisplayMode.WEEK
                ? referenceDate.plusWeeks(direction)
                : referenceDate.plusMonths(direction);
        refresh();
    }

    private void moveYears(int direction) {
        referenceDate = referenceDate.plusYears(direction);
        refresh();
    }

    private void refresh() {
        if (displayMode == CalendarioDisplayMode.WEEK) {
            view.showWeek(service.getWeek(referenceDate, selectedOperatore.id()), referenceDate);
            return;
        }
        YearMonth month = YearMonth.from(referenceDate);
        view.showMonth(month, service.getMonth(month, selectedOperatore.id()), referenceDate);
    }

    public CalendarioView getView() {
        return view;
    }

    public CalendarioService getService() {
        return service;
    }
}

package com.example.clients.feature.calendario.controller;

import com.example.clients.feature.calendario.service.CalendarioService;
import com.example.clients.feature.calendario.view.CalendarioView;

import java.time.YearMonth;

public class CalendarioController {

    private final CalendarioView view;
    private final CalendarioService service;
    private YearMonth displayedMonth;

    public CalendarioController(CalendarioView view, CalendarioService service) {
        this.view = view;
        this.service = service;
        displayedMonth = service.currentMonth();

        view.getPreviousYearButton().setOnAction(event -> showMonth(displayedMonth.minusYears(1)));
        view.getPreviousMonthButton().setOnAction(event -> showMonth(displayedMonth.minusMonths(1)));
        view.getNextMonthButton().setOnAction(event -> showMonth(displayedMonth.plusMonths(1)));
        view.getNextYearButton().setOnAction(event -> showMonth(displayedMonth.plusYears(1)));
        view.getTodayButton().setOnAction(event -> showMonth(service.currentMonth()));
        refreshMonth();
    }

    private void showMonth(YearMonth month) {
        displayedMonth = month;
        refreshMonth();
    }

    private void refreshMonth() {
        view.showMonth(service.getMonth(displayedMonth));
    }

    public CalendarioView getView() {
        return view;
    }

    public CalendarioService getService() {
        return service;
    }
}

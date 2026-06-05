package com.example.clients.feature.dashboard.controller;

import com.example.clients.app.Navigator;
import com.example.clients.feature.dashboard.service.DashboardService;
import com.example.clients.feature.dashboard.view.DashboardView;

public class DashboardController {

    private final DashboardView view;
    private final Navigator navigator;
    private final DashboardService service;

    public DashboardController(DashboardView view, Navigator navigator, DashboardService service) {
        this.view = view;
        this.navigator = navigator;
        this.service = service;
    }

    public DashboardView getView() {
        return view;
    }

    public Navigator getNavigator() {
        return navigator;
    }

    public DashboardService getService() {
        return service;
    }
}

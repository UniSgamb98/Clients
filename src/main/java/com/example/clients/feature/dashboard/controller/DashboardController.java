package com.example.clients.feature.dashboard.controller;

import com.example.clients.app.navigators.DashboardNav;
import com.example.clients.feature.dashboard.service.DashboardService;
import com.example.clients.feature.dashboard.view.DashboardView;

public class DashboardController {

    private final DashboardView view;
    private final DashboardNav dashboardNav;
    private final DashboardService service;

    public DashboardController(DashboardView view, DashboardNav dashboardNav, DashboardService service) {
        this.view = view;
        this.dashboardNav = dashboardNav;
        this.service = service;
    }

    public DashboardView getView() {
        return view;
    }

    public DashboardNav getDashboardNav() {
        return dashboardNav;
    }

    public DashboardService getService() {
        return service;
    }
}

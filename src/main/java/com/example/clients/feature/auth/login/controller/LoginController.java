package com.example.clients.feature.auth.login.controller;

import com.example.clients.core.database.service.CurrentOperatoreService;
import com.example.clients.feature.auth.login.navigator.LoginNav;
import com.example.clients.feature.auth.login.service.LoginService;
import com.example.clients.feature.auth.login.view.LoginView;

public class LoginController {

    private final LoginView view;
    private final LoginNav loginNav;
    private final LoginService service;

    public LoginController(LoginView view, LoginNav loginNav, LoginService service) {
        this.view = view;
        this.loginNav = loginNav;
        this.service = service;
        configureActions();
        loadUsers();
    }

    private void configureActions() {
        view.getLoginButton().setOnAction(event -> login());
    }

    private void loadUsers() {
        try {
            view.setUsers(service.loadUsers());
            if (view.getSelectedUser() == null) {
                view.showError("Nessun utente attivo trovato nel database.");
                view.getLoginButton().setDisable(true);
            }
        } catch (RuntimeException e) {
            view.showError("Caricamento utenti non riuscito: " + safeMessage(e));
            view.getLoginButton().setDisable(true);
        }
    }

    private void login() {
        try {
            LoginService.LoginSession session = service.login(view.getSelectedUser());
            CurrentOperatoreService.setCurrentOperatore(session.userId(), session.username());
            view.clearError();
            loginNav.showDashboardAfterLogin();
        } catch (IllegalArgumentException e) {
            view.showError(e.getMessage());
        } catch (RuntimeException e) {
            view.showError("Accesso riuscito, ma avvio applicazione non riuscito: " + safeMessage(e));
        }
    }

    private String safeMessage(RuntimeException e) {
        return e.getMessage() == null || e.getMessage().isBlank() ? "errore imprevisto." : e.getMessage();
    }
}

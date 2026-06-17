package com.example.clients.feature.auth.login.controller;

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
    }

    private void configureActions() {
        view.getLoginButton().setOnAction(event -> login());
        view.getPasswordField().setOnAction(event -> login());
    }

    private void login() {
        try {
            service.login(view.getUsernameField().getText(), view.getPasswordField().getText());
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


package com.example.clients.feature.auth.login.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LoginView extends BorderPane {

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Button loginButton;
    private final Label errorLabel;

    public LoginView() {
        usernameField = new TextField();
        usernameField.setPromptText("Nome utente");
        usernameField.getStyleClass().add("login-field");
        usernameField.setText("utente");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("login-field");

        loginButton = new Button("Accedi");
        loginButton.getStyleClass().add("login-primary-button");
        loginButton.setDefaultButton(true);

        errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        getStyleClass().add("login-root");
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox wrapper = new VBox();
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(32));
        wrapper.getStyleClass().add("login-wrapper");

        VBox card = new VBox(18);
        card.setMaxWidth(420);
        card.getStyleClass().add("login-card");

        VBox heading = new VBox(6);
        heading.getStyleClass().add("login-heading");
        Label title = new Label("Bentornato");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("Accedi per gestire clienti, contatti e attività commerciali.");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setWrapText(true);
        heading.getChildren().addAll(title, subtitle);

        VBox form = new VBox(12);
        form.getStyleClass().add("login-form");
        form.getChildren().addAll(
                createFieldGroup("Nome utente", usernameField),
                createFieldGroup("Password", passwordField),
                errorLabel,
                createActions()
        );

        card.getChildren().addAll(heading, form);
        wrapper.getChildren().add(card);
        return wrapper;
    }

    private VBox createFieldGroup(String labelText, TextField field) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("login-field-label");
        field.setMaxWidth(Double.MAX_VALUE);
        group.getChildren().addAll(label, field);
        return group;
    }

    private HBox createActions() {
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(loginButton, Priority.NEVER);
        actions.getChildren().add(loginButton);
        return actions;
    }

    public void showError(String message) {
        errorLabel.setText(message == null || message.isBlank() ? "Accesso non riuscito." : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    public TextField getUsernameField() {
        return usernameField;
    }

    public PasswordField getPasswordField() {
        return passwordField;
    }

    public Button getLoginButton() {
        return loginButton;
    }
}

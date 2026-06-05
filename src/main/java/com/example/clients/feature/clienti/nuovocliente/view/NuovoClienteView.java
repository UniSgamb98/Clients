package com.example.clients.feature.clienti.nuovocliente.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NuovoClienteView extends BorderPane {

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final TextField nameField;
    private final TextField typeField;
    private final TextField contactField;
    private final TextField phoneField;
    private final TextField emailField;
    private final Button saveButton;
    private final Button cancelButton;

    public NuovoClienteView() {
        header = new AppHeader("Nuovo cliente");
        sidebar = new AppSidebar();

        nameField = createTextField("Nome o ragione sociale");
        typeField = createTextField("Tipo cliente");
        contactField = createTextField("Referente principale");
        phoneField = createTextField("Telefono");
        emailField = createTextField("Email");

        saveButton = new Button("Salva cliente");
        saveButton.getStyleClass().add("clients-primary-button");
        cancelButton = new Button("Annulla");
        cancelButton.getStyleClass().add("clients-filter-button");

        setTop(header);
        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("clients-content");

        VBox titleBox = new VBox(4);
        Label title = new Label("Nuovo cliente");
        title.getStyleClass().add("clients-title");
        Label subtitle = new Label("Inserisci i dati principali del cliente. Per ora è solo una bozza grafica.");
        subtitle.getStyleClass().add("clients-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        VBox form = new VBox(12);
        form.getStyleClass().add("new-client-form");
        form.getChildren().addAll(
                createFieldGroup("Cliente", nameField),
                createFieldGroup("Tipo", typeField),
                createFieldGroup("Referente", contactField),
                createFieldGroup("Telefono", phoneField),
                createFieldGroup("Email", emailField)
        );

        HBox actions = new HBox(10);
        actions.getStyleClass().add("new-client-actions");
        actions.getChildren().addAll(saveButton, cancelButton);

        content.getChildren().addAll(titleBox, form, actions);
        return content;
    }

    private VBox createFieldGroup(String labelText, TextField field) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("new-client-field-label");
        field.setMaxWidth(Double.MAX_VALUE);
        group.getChildren().addAll(label, field);
        return group;
    }

    private TextField createTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("clients-search-field");
        return textField;
    }

    public AppHeader getHeader() {
        return header;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getTypeField() {
        return typeField;
    }

    public TextField getContactField() {
        return contactField;
    }

    public TextField getPhoneField() {
        return phoneField;
    }

    public TextField getEmailField() {
        return emailField;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}

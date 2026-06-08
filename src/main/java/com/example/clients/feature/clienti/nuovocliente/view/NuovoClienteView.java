package com.example.clients.feature.clienti.nuovocliente.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class NuovoClienteView extends BorderPane {

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final TextField nameField;
    private final TextField typeField;
    private final TextField statusField;
    private final TextField vatField;
    private final TextField fiscalCodeField;
    private final TextField acquisitionField;
    private final TextField operatorField;
    private final TextField websiteField;
    private final TextField emailField;
    private final TextField contactField;
    private final TextField phoneField;
    private final ComboBox<String> contactPhoneField;
    private final ComboBox<String> contactEmailField;
    private final TextField countryField;
    private final TextField regionField;
    private final TextField provinceField;
    private final TextField cityField;
    private final TextField addressField;
    private final TextField streetNumberField;
    private final TextField zipField;
    private final Button addWebsiteButton;
    private final Button addEmailButton;
    private final Button addContactButton;
    private final Button addPhoneButton;
    private final Button addAddressButton;
    private final Button saveButton;
    private final Button cancelButton;

    public NuovoClienteView() {
        header = new AppHeader("Nuovo cliente");
        sidebar = new AppSidebar();

        nameField = createTextField("Ragione sociale");
        typeField = createTextField("Tipo cliente");
        statusField = createTextField("Stato trattativa");
        vatField = createTextField("Partita IVA");
        fiscalCodeField = createTextField("Codice fiscale");
        acquisitionField = createTextField("Data acquisizione");
        operatorField = createTextField("Operatore assegnato");
        websiteField = createTextField("Sito web");
        emailField = createTextField("Email cliente");
        contactField = createTextField("Nome referente");
        phoneField = createTextField("Telefono cliente");
        contactPhoneField = createLinkedComboBox("Telefono referente");
        contactEmailField = createLinkedComboBox("Email referente");
        countryField = createTextField("Paese");
        regionField = createTextField("Regione");
        provinceField = createTextField("Provincia");
        cityField = createTextField("Città");
        addressField = createTextField("Indirizzo");
        streetNumberField = createTextField("Numero civico");
        zipField = createTextField("CAP");

        addWebsiteButton = createAddButton();
        addEmailButton = createAddButton();
        addContactButton = createAddButton();
        addPhoneButton = createAddButton();
        addAddressButton = createAddButton();

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
        Label subtitle = new Label("Inserisci i dati del cliente, divisi per le stesse aree dello schema database.");
        subtitle.getStyleClass().add("clients-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        ScrollPane scrollPane = new ScrollPane(createScrollableForm());
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("new-client-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        HBox actions = new HBox(10);
        actions.getStyleClass().add("new-client-actions");
        actions.getChildren().addAll(saveButton, cancelButton);

        content.getChildren().addAll(titleBox, scrollPane, actions);
        return content;
    }

    private HBox createScrollableForm() {
        HBox formColumns = new HBox(18);
        formColumns.getStyleClass().add("new-client-form-grid");

        VBox leftColumn = new VBox(14);
        leftColumn.getStyleClass().add("new-client-form-column");
        leftColumn.getChildren().addAll(
                createSection("Dati cliente", createClientFields()),
                createSection("Contatti cliente", createContactBlock())
        );

        VBox rightColumn = new VBox(14);
        rightColumn.getStyleClass().add("new-client-form-column");
        rightColumn.getChildren().add(
                createSection("Indirizzi cliente", createAddressFields())
        );

        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        formColumns.getChildren().addAll(leftColumn, rightColumn);
        return formColumns;
    }

    private VBox createClientFields() {
        VBox fields = new VBox(10);
        fields.getChildren().addAll(
                createFieldGroup("Ragione sociale", nameField),
                createFieldGroup("Tipo cliente", typeField),
                createFieldGroup("Stato trattativa", statusField),
                createFieldGroup("Partita IVA", vatField),
                createFieldGroup("Codice fiscale", fiscalCodeField),
                createFieldGroup("Acquisizione", acquisitionField),
                createFieldGroup("Operatore", operatorField),
                createRepeatableField("Sito web", websiteField, addWebsiteButton),
                createRepeatableField("Email", emailField, addEmailButton),
                createRepeatableField("Telefono", phoneField, addPhoneButton)
        );
        return fields;
    }

    private VBox createAddressFields() {
        VBox fields = new VBox(10);
        fields.getChildren().addAll(
                createFieldGroup("Paese", countryField),
                createFieldGroup("Regione", regionField),
                createFieldGroup("Provincia", provinceField),
                createFieldGroup("Città", cityField),
                createFieldGroup("Indirizzo", addressField),
                createFieldGroup("Numero civico", streetNumberField),
                createFieldGroup("CAP", zipField),
                createRepeatableField("Altro indirizzo", createTextField("Aggiungi altra sede"), addAddressButton)
        );
        return fields;
    }

    private VBox createContactBlock() {
        VBox block = new VBox(10);
        block.getChildren().add(createRepeatableField("Referente", contactField, addContactButton));

        VBox card = new VBox(10);
        card.getStyleClass().add("new-client-contact-card");
        Label hint = new Label("Collega il referente ai recapiti già inseriti sopra, oppure scrivine uno nuovo.");
        hint.getStyleClass().add("new-client-card-hint");
        card.getChildren().addAll(
                hint,
                createComboGroup("Telefono referente", contactPhoneField),
                createComboGroup("Email referente", contactEmailField)
        );
        block.getChildren().add(card);
        return block;
    }

    private VBox createSection(String titleText, VBox body) {
        VBox section = new VBox(12);
        section.getStyleClass().add("new-client-section");

        Label title = new Label(titleText);
        title.getStyleClass().add("new-client-section-title");

        section.getChildren().addAll(title, body);
        return section;
    }

    private VBox createFieldGroup(String labelText, TextField field) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("new-client-field-label");
        field.setMaxWidth(Double.MAX_VALUE);
        group.getChildren().addAll(label, field);
        return group;
    }

    private VBox createComboGroup(String labelText, ComboBox<String> field) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("new-client-field-label");
        field.setMaxWidth(Double.MAX_VALUE);
        group.getChildren().addAll(label, field);
        return group;
    }

    private VBox createRepeatableField(String labelText, TextField field, Button addButton) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("new-client-field-label");

        HBox row = new HBox(8);
        row.getStyleClass().add("new-client-repeatable-row");
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(field, addButton);

        group.getChildren().addAll(label, row);
        return group;
    }

    private TextField createTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("clients-search-field");
        return textField;
    }

    private ComboBox<String> createLinkedComboBox(String prompt) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.setPromptText(prompt);
        comboBox.getStyleClass().add("new-client-linked-combo");
        return comboBox;
    }

    private Button createAddButton() {
        Button button = new Button("+");
        button.getStyleClass().add("new-client-add-button");
        return button;
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

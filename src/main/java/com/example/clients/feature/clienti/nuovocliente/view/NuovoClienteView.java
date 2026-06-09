package com.example.clients.feature.clienti.nuovocliente.view;

import com.example.clients.core.ui.AppHeader;
import com.example.clients.core.ui.AppSidebar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private final VBox websiteFieldsContainer;
    private final VBox emailFieldsContainer;
    private final VBox phoneFieldsContainer;
    private final VBox contactFieldsContainer;
    private final VBox extraAddressFieldsContainer;
    private final List<TextField> websiteFields;
    private final List<TextField> emailFields;
    private final List<TextField> phoneFields;
    private final List<TextField> contactFields;
    private final List<ComboBox<String>> contactPhoneFields;
    private final List<ComboBox<String>> contactEmailFields;
    private final List<TextField> extraAddressFields;

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

        websiteFieldsContainer = createRepeatableContainer(websiteField, addWebsiteButton);
        emailFieldsContainer = createRepeatableContainer(emailField, addEmailButton);
        phoneFieldsContainer = createRepeatableContainer(phoneField, addPhoneButton);
        contactFieldsContainer = new VBox(10);
        extraAddressFieldsContainer = createRepeatableContainer(createTextField("Aggiungi altra sede"), addAddressButton);

        websiteFields = new ArrayList<>();
        emailFields = new ArrayList<>();
        phoneFields = new ArrayList<>();
        contactFields = new ArrayList<>();
        contactPhoneFields = new ArrayList<>();
        contactEmailFields = new ArrayList<>();
        extraAddressFields = new ArrayList<>();
        websiteFields.add(websiteField);
        emailFields.add(emailField);
        phoneFields.add(phoneField);
        contactFields.add(contactField);
        contactPhoneFields.add(contactPhoneField);
        contactEmailFields.add(contactEmailField);
        extraAddressFields.add((TextField) ((HBox) extraAddressFieldsContainer.getChildren().get(0)).getChildren().get(0));
        contactFieldsContainer.getChildren().add(createContactEntry(contactField, contactPhoneField, contactEmailField, addContactButton));

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
        Label subtitle = new Label("Inserisci i dati del cliente e i relativi recapiti, contatti e indirizzi.");
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
        leftColumn.getChildren().add(createSection("Dati cliente", createClientFields()));

        VBox rightColumn = new VBox(14);
        rightColumn.getStyleClass().add("new-client-form-column");
        rightColumn.getChildren().addAll(
                createSection("Indirizzi cliente", createAddressFields()),
                createSection("Contatti cliente", createContactBlock())
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
                createRepeatableFieldGroup("Sito web", websiteFieldsContainer),
                createRepeatableFieldGroup("Email", emailFieldsContainer),
                createRepeatableFieldGroup("Telefono", phoneFieldsContainer)
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
                createRepeatableFieldGroup("Altri indirizzi", extraAddressFieldsContainer)
        );
        return fields;
    }

    private VBox createContactBlock() {
        VBox block = new VBox(10);
        block.getChildren().add(contactFieldsContainer);
        return block;
    }

    private VBox createContactEntry(
            TextField contact,
            ComboBox<String> contactPhone,
            ComboBox<String> contactEmail,
            Button addButton
    ) {
        VBox entry = new VBox(10);
        entry.getStyleClass().add("new-client-contact-card");
        entry.getChildren().add(createRepeatableFieldGroup("Referente", createRepeatableContainer(contact, addButton)));

        Label hint = new Label("Collega il referente ai recapiti già inseriti sopra, oppure scrivine uno nuovo.");
        hint.getStyleClass().add("new-client-card-hint");
        entry.getChildren().addAll(
                hint,
                createComboGroup("Telefono referente", contactPhone),
                createComboGroup("Email referente", contactEmail)
        );
        return entry;
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

    private VBox createRepeatableFieldGroup(String labelText, VBox fieldsContainer) {
        VBox group = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("new-client-field-label");
        group.getChildren().addAll(label, fieldsContainer);
        return group;
    }

    private VBox createRepeatableContainer(TextField field, Button addButton) {
        VBox fieldsContainer = new VBox(8);
        fieldsContainer.getChildren().add(createRepeatableRow(field, addButton));
        return fieldsContainer;
    }

    private HBox createRepeatableRow(TextField field, Button addButton) {
        HBox row = new HBox(8);
        row.getStyleClass().add("new-client-repeatable-row");
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().add(field);
        if (addButton != null) {
            row.getChildren().add(addButton);
        }
        return row;
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

    public TextField addWebsiteField() {
        TextField field = createTextField("Altro sito web");
        websiteFields.add(field);
        websiteFieldsContainer.getChildren().add(createRepeatableRow(field, null));
        return field;
    }

    public TextField addEmailField() {
        TextField field = createTextField("Altra email");
        emailFields.add(field);
        emailFieldsContainer.getChildren().add(createRepeatableRow(field, null));
        return field;
    }

    public TextField addPhoneField() {
        TextField field = createTextField("Altro telefono");
        phoneFields.add(field);
        phoneFieldsContainer.getChildren().add(createRepeatableRow(field, null));
        return field;
    }

    public TextField addAddressField() {
        TextField field = createTextField("Altra sede");
        extraAddressFields.add(field);
        extraAddressFieldsContainer.getChildren().add(createRepeatableRow(field, null));
        return field;
    }

    public TextField addContactEntry(List<String> phoneOptions, List<String> emailOptions) {
        TextField contact = createTextField("Altro referente");
        ComboBox<String> phone = createLinkedComboBox("Telefono referente");
        ComboBox<String> email = createLinkedComboBox("Email referente");
        phone.getItems().setAll(phoneOptions);
        email.getItems().setAll(emailOptions);
        contactFields.add(contact);
        contactPhoneFields.add(phone);
        contactEmailFields.add(email);
        contactFieldsContainer.getChildren().add(createContactEntry(contact, phone, email, null));
        return contact;
    }

    public void setContactPhoneOptions(List<String> phones) {
        contactPhoneFields.forEach(field -> field.getItems().setAll(phones));
    }

    public void setContactEmailOptions(List<String> emails) {
        contactEmailFields.forEach(field -> field.getItems().setAll(emails));
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

    public TextField getStatusField() {
        return statusField;
    }

    public TextField getVatField() {
        return vatField;
    }

    public TextField getFiscalCodeField() {
        return fiscalCodeField;
    }

    public TextField getAcquisitionField() {
        return acquisitionField;
    }

    public TextField getOperatorField() {
        return operatorField;
    }

    public TextField getCountryField() {
        return countryField;
    }

    public TextField getRegionField() {
        return regionField;
    }

    public TextField getProvinceField() {
        return provinceField;
    }

    public TextField getCityField() {
        return cityField;
    }

    public TextField getAddressField() {
        return addressField;
    }

    public TextField getStreetNumberField() {
        return streetNumberField;
    }

    public TextField getZipField() {
        return zipField;
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

    public List<TextField> getWebsiteFields() {
        return Collections.unmodifiableList(websiteFields);
    }

    public List<TextField> getEmailFields() {
        return Collections.unmodifiableList(emailFields);
    }

    public List<TextField> getPhoneFields() {
        return Collections.unmodifiableList(phoneFields);
    }

    public List<TextField> getContactFields() {
        return Collections.unmodifiableList(contactFields);
    }

    public List<ComboBox<String>> getContactPhoneFields() {
        return Collections.unmodifiableList(contactPhoneFields);
    }

    public List<ComboBox<String>> getContactEmailFields() {
        return Collections.unmodifiableList(contactEmailFields);
    }

    public List<TextField> getExtraAddressFields() {
        return Collections.unmodifiableList(extraAddressFields);
    }

    public Button getAddWebsiteButton() {
        return addWebsiteButton;
    }

    public Button getAddEmailButton() {
        return addEmailButton;
    }

    public Button getAddContactButton() {
        return addContactButton;
    }

    public Button getAddPhoneButton() {
        return addPhoneButton;
    }

    public Button getAddAddressButton() {
        return addAddressButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public Button getCancelButton() {
        return cancelButton;
    }
}

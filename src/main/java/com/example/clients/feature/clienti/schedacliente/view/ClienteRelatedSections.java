package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.AddressEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.AddressItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ContactEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ContactItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ValueEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ValueItem;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

final class ClienteRelatedSections extends ResponsiveTilePane {

    private final VBox contactsList;
    private final VBox addressesList;
    private final List<ContactEditControls> contactEditControls = new ArrayList<>();
    private final List<AddressEditControls> addressEditControls = new ArrayList<>();
    private List<TextField> companyPhoneFields = List.of();
    private List<TextField> companyEmailFields = List.of();

    ClienteRelatedSections(double gap, double twoColumnBreakpoint) {
        super(gap, twoColumnBreakpoint);
        getStyleClass().add("client-profile-related-sections-grid");
        contactsList = new VBox(8);
        addressesList = new VBox(8);
        addStretchingTile(createSection("Contatti", contactsList));
        addStretchingTile(createSection("Indirizzi", addressesList));
    }

    void setCompanyValueSources(List<TextField> phoneFields, List<TextField> emailFields) {
        companyPhoneFields = phoneFields == null ? List.of() : phoneFields;
        companyEmailFields = emailFields == null ? List.of() : emailFields;
        refreshLinkedContactOptions();
    }

    void renderContacts(List<ContactItem> values) {
        renderList(contactsList, values.stream().map(this::formatContact).toList());
    }

    void renderAddresses(List<AddressItem> values) {
        renderList(addressesList, values.stream().map(this::formatAddress).toList());
    }

    void renderContactsEditor(List<ContactEditInput> values) {
        contactsList.getChildren().clear();
        contactEditControls.clear();
        List<ContactEditInput> safeValues = values.isEmpty() ? List.of(new ContactEditInput(null, "", List.of(), List.of())) : values;
        safeValues.forEach(this::addContactEditor);
    }

    void renderAddressesEditor(List<AddressEditInput> values) {
        addressesList.getChildren().clear();
        addressEditControls.clear();
        List<AddressEditInput> safeValues = values.isEmpty() ? List.of(emptyAddressInput()) : values;
        safeValues.forEach(this::addAddressEditor);
    }

    List<ContactEditInput> collectContacts() {
        return contactEditControls.stream()
                .map(control -> new ContactEditInput(
                        control.id(),
                        valueOf(control.descriptionField()),
                        linkedValuesOf(control.phoneFields(), companyPhoneFields),
                        linkedValuesOf(control.emailFields(), companyEmailFields)))
                .toList();
    }

    List<AddressEditInput> collectAddresses() {
        return addressEditControls.stream()
                .map(control -> new AddressEditInput(
                        control.id(),
                        valueOf(control.countryField()),
                        valueOf(control.regionField()),
                        valueOf(control.provinceField()),
                        valueOf(control.cityField()),
                        valueOf(control.addressField()),
                        valueOf(control.streetNumberField()),
                        valueOf(control.zipField()),
                        control.primaryCheck().isSelected()))
                .toList();
    }

    void refreshLinkedContactOptions() {
        contactEditControls.forEach(control -> {
            refreshLinkedComboOptions(control.phoneFields(), companyPhoneFields);
            refreshLinkedComboOptions(control.emailFields(), companyEmailFields);
        });
    }

    private void addContactEditor(ContactEditInput value) {
        VBox card = new VBox(8);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("client-profile-timeline-card");
        TextField descriptionField = createTextField(value.descrizione(), "Nome referente / contatto");

        List<ComboBox<String>> phoneFields = new ArrayList<>();
        List<ComboBox<String>> emailFields = new ArrayList<>();
        VBox phoneBox = createLinkedEditableValuesSection("Telefoni contatto", phoneFields, value.telefoni(), "Telefono contatto", linkedOptions(companyPhoneFields, value.telefoni()));
        VBox emailBox = createLinkedEditableValuesSection("Email contatto", emailFields, value.email(), "Email contatto", linkedOptions(companyEmailFields, value.email()));

        HBox actions = new HBox(8);
        Button addButton = createSmallButton("+");
        Button removeButton = createSmallButton("-");
        addButton.setOnAction(event -> addContactEditor(new ContactEditInput(null, "", List.of(), List.of())));
        removeButton.setOnAction(event -> {
            contactsList.getChildren().remove(card);
            contactEditControls.removeIf(control -> control.container() == card);
            if (contactEditControls.isEmpty()) {
                addContactEditor(new ContactEditInput(null, "", List.of(), List.of()));
            }
        });
        actions.getChildren().addAll(addButton, removeButton);

        card.getChildren().addAll(createFieldRow("Contatto", descriptionField), phoneBox, emailBox, actions);
        contactsList.getChildren().add(card);
        contactEditControls.add(new ContactEditControls(value.id(), descriptionField, phoneFields, emailFields, card));
    }

    private void addAddressEditor(AddressEditInput value) {
        VBox card = new VBox(8);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("client-profile-timeline-card");
        TextField countryField = createTextField(value.paese(), "Paese");
        TextField regionField = createTextField(value.regione(), "Regione");
        TextField provinceField = createTextField(value.provincia(), "Provincia");
        TextField cityField = createTextField(value.citta(), "Città");
        TextField addressField = createTextField(value.indirizzo(), "Indirizzo");
        TextField streetNumberField = createTextField(value.numeroCivico(), "Numero civico");
        TextField zipField = createTextField(value.cap(), "CAP");
        CheckBox primaryCheck = new CheckBox("Indirizzo principale");
        primaryCheck.getStyleClass().add("client-profile-primary-check");
        primaryCheck.setSelected(value.principale());
        primaryCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                unsetOtherPrimaryChecks(primaryCheck);
            }
        });

        HBox actions = new HBox(8);
        Button addButton = createSmallButton("+");
        Button removeButton = createSmallButton("-");
        addButton.setOnAction(event -> addAddressEditor(emptyAddressInput()));
        removeButton.setOnAction(event -> {
            addressesList.getChildren().remove(card);
            addressEditControls.removeIf(control -> control.container() == card);
            if (addressEditControls.isEmpty()) {
                addAddressEditor(emptyAddressInput());
            }
        });
        actions.getChildren().addAll(addButton, removeButton);

        card.getChildren().addAll(
                createFieldRow("Paese", countryField),
                createFieldRow("Regione", regionField),
                createFieldRow("Provincia", provinceField),
                createFieldRow("Città", cityField),
                createFieldRow("Indirizzo", addressField),
                createFieldRow("Numero civico", streetNumberField),
                createFieldRow("CAP", zipField),
                primaryCheck,
                actions
        );
        addressesList.getChildren().add(card);
        addressEditControls.add(new AddressEditControls(value.id(), countryField, regionField, provinceField, cityField, addressField, streetNumberField, zipField, primaryCheck, card));
        if (primaryCheck.isSelected()) {
            unsetOtherPrimaryChecks(primaryCheck);
        }
    }

    private VBox createLinkedEditableValuesSection(String title, List<ComboBox<String>> target, List<ValueEditInput> values, String prompt, List<String> options) {
        VBox section = new VBox(8);
        section.getStyleClass().add("client-profile-edit-values-section");
        section.getChildren().add(createEditSectionLabel(title));
        addLinkedEditableValues(section, target, values, prompt, options);
        return section;
    }

    private void addLinkedEditableValues(VBox container, List<ComboBox<String>> target, List<ValueEditInput> values, String prompt, List<String> options) {
        target.clear();
        List<ValueEditInput> safeValues = values.isEmpty() ? List.of(new ValueEditInput(null, "")) : values;
        safeValues.forEach(value -> addLinkedEditableValueRow(container, target, value.id(), value.value(), prompt, options));
    }

    private void addLinkedEditableValueRow(VBox container, List<ComboBox<String>> target, java.util.UUID id, String value, String prompt, List<String> options) {
        ComboBox<String> field = new ComboBox<>();
        field.setEditable(true);
        field.setPromptText(prompt);
        field.getStyleClass().add("client-profile-linked-combo");
        field.getItems().setAll(options);
        field.getEditor().setText(emptyFallbackForEdit(value));
        field.setUserData(id);
        target.add(field);

        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Button addButton = createSmallButton("+");
        Button removeButton = createSmallButton("-");
        addButton.setOnAction(event -> addLinkedEditableValueRow(container, target, null, "", prompt, options));
        removeButton.setOnAction(event -> {
            target.remove(field);
            container.getChildren().remove(row);
            if (target.isEmpty()) {
                addLinkedEditableValueRow(container, target, null, "", prompt, options);
            }
        });
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(field, addButton, removeButton);
        container.getChildren().add(row);
    }

    private void refreshLinkedComboOptions(List<ComboBox<String>> comboFields, List<TextField> sourceFields) {
        List<String> options = linkedOptions(sourceFields, linkedValuesForOptions(comboFields));
        comboFields.forEach(field -> field.getItems().setAll(options));
    }

    private List<ValueEditInput> linkedValuesForOptions(List<ComboBox<String>> fields) {
        return fields.stream()
                .map(field -> new ValueEditInput((java.util.UUID) field.getUserData(), comboValue(field)))
                .toList();
    }

    private List<String> linkedOptions(List<TextField> sourceFields, List<ValueEditInput> selectedValues) {
        List<String> options = new ArrayList<>();
        sourceFields.stream()
                .map(this::valueOf)
                .filter(value -> !value.isBlank())
                .forEach(options::add);
        selectedValues.stream()
                .map(ValueEditInput::value)
                .filter(value -> value != null && !value.isBlank() && !options.contains(value))
                .forEach(options::add);
        return options;
    }

    private List<ValueEditInput> linkedValuesOf(List<ComboBox<String>> fields, List<TextField> sourceFields) {
        return fields.stream()
                .map(field -> new ValueEditInput(idForLinkedValue(field, sourceFields), comboValue(field)))
                .toList();
    }

    private java.util.UUID idForLinkedValue(ComboBox<String> field, List<TextField> sourceFields) {
        String value = comboValue(field);
        if (!value.isBlank()) {
            for (TextField sourceField : sourceFields) {
                if (value.equals(valueOf(sourceField))) {
                    return (java.util.UUID) sourceField.getUserData();
                }
            }
        }
        return (java.util.UUID) field.getUserData();
    }

    private void unsetOtherPrimaryChecks(CheckBox selectedCheck) {
        addressEditControls.stream()
                .map(AddressEditControls::primaryCheck)
                .filter(checkBox -> checkBox != selectedCheck)
                .forEach(checkBox -> checkBox.setSelected(false));
    }

    private void renderList(Pane container, List<String> values) {
        container.getChildren().clear();
        if (values.isEmpty()) {
            container.getChildren().add(createInfoLabel("Nessun dato disponibile"));
            return;
        }
        values.forEach(value -> container.getChildren().add(createInfoLabel(value)));
    }

    private VBox createSection(String titleText, Node body) {
        VBox section = new VBox(12);
        section.getStyleClass().add("new-client-section");
        Label title = new Label(titleText);
        title.getStyleClass().add("new-client-section-title");
        section.getChildren().addAll(title, body);
        return section;
    }

    private HBox createFieldRow(String labelText, TextField field) {
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        row.getChildren().addAll(createEditLabel(labelText), field);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private TextField createTextField(String value, String prompt) {
        TextField field = new TextField(emptyFallbackForEdit(value));
        field.setPromptText(prompt);
        field.getStyleClass().add("client-profile-edit-field");
        return field;
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("client-profile-info-label");
        label.setWrapText(true);
        return label;
    }

    private Label createEditLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("client-profile-edit-label");
        label.setMinWidth(120);
        return label;
    }

    private Label createEditSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("client-profile-edit-section-title");
        return label;
    }

    private Button createSmallButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("client-profile-small-filter-button");
        return button;
    }

    private AddressEditInput emptyAddressInput() {
        return new AddressEditInput(null, "", "", "", "", "", "", "", false);
    }

    private String formatContact(ContactItem contact) {
        return joinNonBlank(
                contact.descrizione(),
                contact.telefoni().isEmpty() ? "" : "Tel: " + joinProfileValues(contact.telefoni()),
                contact.email().isEmpty() ? "" : "Email: " + joinProfileValues(contact.email())
        );
    }

    private String formatAddress(AddressItem address) {
        return joinNonBlank(
                address.indirizzo(),
                address.numeroCivico(),
                address.cap(),
                address.citta(),
                address.provincia(),
                address.regione(),
                address.paese()
        );
    }

    private String joinNonBlank(String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                parts.add(value.trim());
            }
        }
        return String.join(" · ", parts);
    }

    private String joinProfileValues(List<ValueItem> values) {
        return values.isEmpty() ? "-" : String.join(", ", values.stream().map(ValueItem::value).toList());
    }

    private String valueOf(TextField field) {
        return field == null ? "" : field.getText();
    }

    private String comboValue(ComboBox<String> comboBox) {
        String editorText = comboBox.getEditor().getText();
        if (editorText != null && !editorText.isBlank()) {
            return editorText.trim();
        }
        String value = comboBox.getValue();
        return value == null ? "" : value.trim();
    }

    private String emptyFallbackForEdit(String value) {
        return value == null ? "" : value;
    }

    private record ContactEditControls(
            java.util.UUID id,
            TextField descriptionField,
            List<ComboBox<String>> phoneFields,
            List<ComboBox<String>> emailFields,
            VBox container
    ) {
    }

    private record AddressEditControls(
            java.util.UUID id,
            TextField countryField,
            TextField regionField,
            TextField provinceField,
            TextField cityField,
            TextField addressField,
            TextField streetNumberField,
            TextField zipField,
            CheckBox primaryCheck,
            VBox container
    ) {
    }
}

package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.core.ui.AppSidebar;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.AddressEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.AddressItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ClienteProfile;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ContactEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ContactItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.EditProfileDraft;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionPreview;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.InteractionType;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.TimelineFilter;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ValueEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.ValueItem;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SchedaClienteView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double SIDE_COLUMN_MIN_WIDTH = 330;
    private static final double SIDE_COLUMN_PREF_WIDTH = 560;
    private static final double SIDE_COLUMN_MAX_WIDTH = 620;
    private static final double RELATED_SECTIONS_GAP = 12;
    private static final double RELATED_SECTIONS_TWO_COLUMN_BREAKPOINT = 560;
    private static final double CUSTOMER_DATA_TILE_WIDTH = 240;
    private static final double CUSTOMER_DATA_TWO_COLUMN_BREAKPOINT = CUSTOMER_DATA_TILE_WIDTH * 2 + RELATED_SECTIONS_GAP;
    private static final double EDIT_NEXT_CALL_PICKER_PREF_WIDTH = 170;
    private static final double EDIT_NEXT_CALL_PICKER_MAX_WIDTH = 190;
    private static final double DELETE_INTERACTION_BUTTON_WIDTH = 36;
    private static final double EDIT_INTERACTION_TEXT_AREA_MAX_WIDTH = 760;
    private static final double EDIT_INTERACTION_TEXT_AREA_MIN_HEIGHT = 160;
    private static final int EDIT_INTERACTION_TEXT_AREA_PREF_ROWS = 5;

    private final AppSidebar sidebar;
    private final ClienteProfileHeader header;
    private final ClienteDataSection dataSection;
    private final ClienteRelatedSections relatedSections;
    private final ClienteTimelineSection timelineSection;
    private final List<TextField> phoneEditFields = new ArrayList<>();
    private final List<TextField> emailEditFields = new ArrayList<>();
    private final List<TextField> siteEditFields = new ArrayList<>();
    private final List<ContactEditControls> contactEditControls = new ArrayList<>();
    private final List<AddressEditControls> addressEditControls = new ArrayList<>();
    private final List<TimelineEditField> timelineEditFields = new ArrayList<>();
    private TextField ragioneSocialeEditField;
    private ChoiceBox<String> tipoClienteEditField;
    private List<String> tipoClienteOptions = List.of();
    private ChoiceBox<String> statoTrattativaEditField;
    private ChoiceBox<Integer> coinvolgimentoEditField;
    private List<String> statoTrattativaOptions = List.of();
    private TextField partitaIvaEditField;
    private TextField codiceFiscaleEditField;
    private DatePicker acquisizioneEditPicker;

    public SchedaClienteView() {
        sidebar = new AppSidebar();
        header = new ClienteProfileHeader();
        dataSection = new ClienteDataSection();
        relatedSections = new ClienteRelatedSections(RELATED_SECTIONS_GAP, RELATED_SECTIONS_TWO_COLUMN_BREAKPOINT);
        timelineSection = new ClienteTimelineSection();
        setEditMode(false);

        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("clients-content");

        VBox body = new VBox(18);
        body.getChildren().addAll(header, createMainColumns());

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.getStyleClass().add("new-client-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        content.getChildren().add(scrollPane);
        return content;
    }

    private HBox createMainColumns() {
        HBox columns = new HBox(18);
        VBox leftColumn = new VBox(14);
        VBox rightColumn = new VBox(14);
        leftColumn.getStyleClass().addAll("client-profile-column", "client-profile-side-column");
        rightColumn.getStyleClass().addAll("client-profile-column", "client-profile-main-column");
        leftColumn.setMinWidth(SIDE_COLUMN_MIN_WIDTH);
        leftColumn.setPrefWidth(SIDE_COLUMN_PREF_WIDTH);
        leftColumn.setMaxWidth(SIDE_COLUMN_MAX_WIDTH);
        rightColumn.setMaxWidth(Double.MAX_VALUE);
        leftColumn.getChildren().addAll(dataSection, relatedSections);
        rightColumn.getChildren().add(timelineSection);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        columns.getChildren().addAll(leftColumn, rightColumn);
        return columns;
    }

    public void renderProfile(ClienteProfile profile) {
        setEditMode(false);
        header.setTitle(profile.ragioneSociale());
        header.setSubtitle(profile.tipoCliente() + " · " + profile.statoTrattativa());
        header.setAcquisitionText("Acquisito " + formatDate(profile.acquisizione()));
        header.setLastInteractionText("Ultima chiamata " + lastCallText(profile.interazioni()));
        header.setNextInteractionText("Prossima chiamata " + nextCallText(profile.interazioni()));
        setFavorite(profile.favorite());
        setInvolvementSliderValue(profile.coinvolgimento());
        renderCustomerData(profile);
        renderContactList(relatedSections.getContactsList(), profile.contatti());
        renderAddressList(relatedSections.getAddressesList(), profile.indirizzi());
        renderTimeline(profile.interazioni());
    }

    private void renderCustomerData(ClienteProfile profile) {
        dataSection.getContent().getChildren().clear();
        ResponsiveTilePane grid = createCustomerDataGrid();
        addCustomerDataNode(grid, createInfoLabel("Ragione sociale: " + emptyFallback(profile.ragioneSociale())));
        addCustomerDataNode(grid, createInfoLabel("Tipo cliente: " + emptyFallback(profile.tipoCliente())));
        addCustomerDataNode(grid, createInfoLabel("Stato trattativa: " + emptyFallback(profile.statoTrattativa())));
        addCustomerDataNode(grid, createInfoLabel("Coinvolgimento: " + emptyFallback(profile.coinvolgimento())));
        addCustomerDataNode(grid, createInfoLabel("Partita IVA: " + emptyFallback(profile.partitaIva())));
        addCustomerDataNode(grid, createInfoLabel("Codice fiscale: " + emptyFallback(profile.codiceFiscale())));
        addCustomerDataNode(grid, createInfoLabel("Acquisizione: " + formatDate(profile.acquisizione())));
        dataSection.getContent().getChildren().addAll(
                grid,
                createInfoLabel("Telefoni azienda: " + joinProfileValues(profile.telefoni())),
                createInfoLabel("Email azienda: " + joinProfileValues(profile.email())),
                createInfoLabel("Siti web: " + joinProfileValues(profile.sitiWeb()))
        );
    }

    public void renderEditableProfile(EditProfileDraft draft) {
        setEditMode(true);
        header.setTitle(draft.ragioneSociale().isBlank() ? "Cliente" : draft.ragioneSociale());
        header.setSubtitle("Modifica dati cliente");
        header.setAcquisitionText("Acquisito " + formatDate(draft.acquisizione()));
        header.setLastInteractionText("Ultima chiamata " + lastEditableCallText(draft.interazioni()));
        header.setNextInteractionText("Prossima chiamata " + nextEditableCallText(draft.interazioni()));
        setActiveTimelineFilter(TimelineFilter.ALL);
        renderCustomerDataEditor(draft);
        renderEditableContacts(draft.contatti());
        renderEditableAddresses(draft.indirizzi());
        renderEditableTimeline(draft.interazioni());
    }

    public EditProfileDraft collectEditDraft() {
        return new EditProfileDraft(
                valueOf(ragioneSocialeEditField),
                choiceValueOf(tipoClienteEditField),
                choiceValueOf(statoTrattativaEditField),
                coinvolgimentoEditField == null ? null : coinvolgimentoEditField.getValue(),
                valueOf(partitaIvaEditField),
                valueOf(codiceFiscaleEditField),
                acquisizioneEditPicker.getValue(),
                valuesOf(phoneEditFields),
                valuesOf(emailEditFields),
                valuesOf(siteEditFields),
                addressInputs(),
                contactInputs(),
                timelineEditFields.stream()
                        .map(field -> new InteractionEditInput(
                                field.notaId(),
                                field.interazioneId(),
                                field.data(),
                                field.type(),
                                field.nextCallPicker() == null ? field.prossimoContatto() : field.nextCallPicker().getValue(),
                                field.textArea().getText()))
                        .toList()
        );
    }

    private void renderCustomerDataEditor(EditProfileDraft draft) {
        dataSection.getContent().getChildren().clear();
        ragioneSocialeEditField = createTextField(draft.ragioneSociale(), "Ragione sociale");
        tipoClienteEditField = createChoiceBox(draft.tipoCliente());
        statoTrattativaEditField = createChoiceBox(draft.statoTrattativa(), statoTrattativaOptions);
        coinvolgimentoEditField = createIntegerChoiceBox(draft.coinvolgimento());
        partitaIvaEditField = createTextField(draft.partitaIva(), "Partita IVA");
        codiceFiscaleEditField = createTextField(draft.codiceFiscale(), "Codice fiscale");
        acquisizioneEditPicker = new DatePicker(draft.acquisizione());
        acquisizioneEditPicker.getStyleClass().add("client-profile-call-date-picker");

        ResponsiveTilePane grid = createCustomerDataGrid();
        addCustomerDataNode(grid, createFieldRow("Ragione sociale", ragioneSocialeEditField));
        addCustomerDataNode(grid, createChoiceRow("Tipo cliente", tipoClienteEditField));
        addCustomerDataNode(grid, createChoiceRow("Stato trattativa", statoTrattativaEditField));
        addCustomerDataNode(grid, createIntegerChoiceRow("Coinvolgimento", coinvolgimentoEditField));
        addCustomerDataNode(grid, createFieldRow("Partita IVA", partitaIvaEditField));
        addCustomerDataNode(grid, createFieldRow("Codice fiscale", codiceFiscaleEditField));
        addCustomerDataNode(grid, createDateRow("Acquisizione", acquisizioneEditPicker));

        dataSection.getContent().getChildren().addAll(
                grid,
                createEditableValuesSection("Telefoni azienda", phoneEditFields, draft.telefoni(), "Telefono azienda"),
                createEditableValuesSection("Email azienda", emailEditFields, draft.email(), "Email azienda"),
                createEditableValuesSection("Siti web", siteEditFields, draft.sitiWeb(), "Sito web")
        );
    }


    private ResponsiveTilePane createCustomerDataGrid() {
        ResponsiveTilePane grid = new ResponsiveTilePane(RELATED_SECTIONS_GAP, CUSTOMER_DATA_TWO_COLUMN_BREAKPOINT);
        grid.getStyleClass().add("client-profile-data-grid");
        return grid;
    }

    private void addCustomerDataNode(ResponsiveTilePane grid, Node node) {
        grid.addStretchingTile(node);
    }

    private VBox createEditableValuesSection(String title, List<TextField> target, List<ValueEditInput> values, String prompt) {
        VBox section = new VBox(8);
        section.getStyleClass().add("client-profile-edit-values-section");
        section.getChildren().add(createEditSectionLabel(title));
        addEditableValues(section, target, values, prompt);
        return section;
    }

    private void renderEditableValues(VBox container, List<TextField> target, List<ValueEditInput> values, String prompt) {
        container.getChildren().clear();
        addEditableValues(container, target, values, prompt);
    }

    private void addEditableValues(VBox container, List<TextField> target, List<ValueEditInput> values, String prompt) {
        target.clear();
        List<ValueEditInput> safeValues = values.isEmpty() ? List.of(new ValueEditInput(null, "")) : values;
        safeValues.forEach(value -> addEditableValueRow(container, target, value.id(), value.value(), prompt));
    }

    private void addEditableValueRow(VBox container, List<TextField> target, java.util.UUID id, String value, String prompt) {
        TextField field = createTextField(value, prompt);
        field.setUserData(id);
        target.add(field);
        configureLinkedContactOptionsRefresh(field, target);
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Button addButton = new Button("+");
        addButton.getStyleClass().add("client-profile-small-filter-button");
        Button removeButton = new Button("-");
        removeButton.getStyleClass().add("client-profile-small-filter-button");
        addButton.setOnAction(event -> {
            addEditableValueRow(container, target, null, "", prompt);
            refreshLinkedContactOptions();
        });
        removeButton.setOnAction(event -> {
            target.remove(field);
            container.getChildren().remove(row);
            if (target.isEmpty()) {
                addEditableValueRow(container, target, null, "", prompt);
            }
            refreshLinkedContactOptions();
        });
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(field, addButton, removeButton);
        container.getChildren().add(row);
    }

    private void renderEditableContacts(List<ContactEditInput> values) {
        relatedSections.getContactsList().getChildren().clear();
        contactEditControls.clear();
        List<ContactEditInput> safeValues = values.isEmpty() ? List.of(new ContactEditInput(null, "", List.of(), List.of())) : values;
        safeValues.forEach(this::addContactEditor);
    }

    private void addContactEditor(ContactEditInput value) {
        VBox card = new VBox(8);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("client-profile-timeline-card");
        TextField descriptionField = createTextField(value.descrizione(), "Nome referente / contatto");

        List<ComboBox<String>> phoneFields = new ArrayList<>();
        List<ComboBox<String>> emailFields = new ArrayList<>();
        VBox phoneBox = createLinkedEditableValuesSection("Telefoni contatto", phoneFields, value.telefoni(), "Telefono contatto", linkedOptions(phoneEditFields, value.telefoni()));
        VBox emailBox = createLinkedEditableValuesSection("Email contatto", emailFields, value.email(), "Email contatto", linkedOptions(emailEditFields, value.email()));

        HBox actions = new HBox(8);
        Button addButton = new Button("+");
        addButton.getStyleClass().add("client-profile-small-filter-button");
        Button removeButton = new Button("-");
        removeButton.getStyleClass().add("client-profile-small-filter-button");
        addButton.setOnAction(event -> addContactEditor(new ContactEditInput(null, "", List.of(), List.of())));
        removeButton.setOnAction(event -> {
            relatedSections.getContactsList().getChildren().remove(card);
            contactEditControls.removeIf(control -> control.container() == card);
            if (contactEditControls.isEmpty()) {
                addContactEditor(new ContactEditInput(null, "", List.of(), List.of()));
            }
        });
        actions.getChildren().addAll(addButton, removeButton);

        card.getChildren().addAll(createFieldRow("Contatto", descriptionField), phoneBox, emailBox, actions);
        relatedSections.getContactsList().getChildren().add(card);
        contactEditControls.add(new ContactEditControls(value.id(), descriptionField, phoneFields, emailFields, card));
    }

    private void renderEditableAddresses(List<AddressEditInput> values) {
        relatedSections.getAddressesList().getChildren().clear();
        addressEditControls.clear();
        List<AddressEditInput> safeValues = values.isEmpty() ? List.of(emptyAddressInput()) : values;
        safeValues.forEach(this::addAddressEditor);
    }

    private AddressEditInput emptyAddressInput() {
        return new AddressEditInput(null, "", "", "", "", "", "", "", false);
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
        Button addButton = new Button("+");
        addButton.getStyleClass().add("client-profile-small-filter-button");
        Button removeButton = new Button("-");
        removeButton.getStyleClass().add("client-profile-small-filter-button");
        addButton.setOnAction(event -> addAddressEditor(emptyAddressInput()));
        removeButton.setOnAction(event -> {
            relatedSections.getAddressesList().getChildren().remove(card);
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
        relatedSections.getAddressesList().getChildren().add(card);
        addressEditControls.add(new AddressEditControls(value.id(), countryField, regionField, provinceField, cityField, addressField, streetNumberField, zipField, primaryCheck, card));
        if (primaryCheck.isSelected()) {
            unsetOtherPrimaryChecks(primaryCheck);
        }
    }

    private List<ContactEditInput> contactInputs() {
        return contactEditControls.stream()
                .map(control -> new ContactEditInput(
                        control.id(),
                        valueOf(control.descriptionField()),
                        linkedValuesOf(control.phoneFields(), phoneEditFields),
                        linkedValuesOf(control.emailFields(), emailEditFields)))
                .toList();
    }

    private List<AddressEditInput> addressInputs() {
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
        Button addButton = new Button("+");
        addButton.getStyleClass().add("client-profile-small-filter-button");
        Button removeButton = new Button("-");
        removeButton.getStyleClass().add("client-profile-small-filter-button");
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

    private void configureLinkedContactOptionsRefresh(TextField field, List<TextField> target) {
        if (!isLinkedContactOptionsSource(target)) {
            return;
        }

        field.textProperty().addListener((observable, oldValue, newValue) -> refreshLinkedContactOptions());
    }

    private boolean isLinkedContactOptionsSource(List<TextField> target) {
        return target == phoneEditFields || target == emailEditFields;
    }

    private void refreshLinkedContactOptions() {
        contactEditControls.forEach(control -> {
            refreshLinkedComboOptions(control.phoneFields(), phoneEditFields);
            refreshLinkedComboOptions(control.emailFields(), emailEditFields);
        });
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

    private void unsetOtherPrimaryChecks(CheckBox selectedCheck) {
        addressEditControls.stream()
                .map(AddressEditControls::primaryCheck)
                .filter(checkBox -> checkBox != selectedCheck)
                .forEach(checkBox -> checkBox.setSelected(false));
    }

    private HBox createIntegerChoiceRow(String labelText, ChoiceBox<Integer> field) {
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Label label = createEditLabel(labelText);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(label, field);
        return row;
    }

    private HBox createChoiceRow(String labelText, ChoiceBox<String> field) {
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Label label = createEditLabel(labelText);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(label, field);
        return row;
    }

    private HBox createFieldRow(String labelText, TextField field) {
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Label label = createEditLabel(labelText);
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(label, field);
        return row;
    }

    private HBox createDateRow(String labelText, DatePicker picker) {
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Label label = createEditLabel(labelText);
        HBox.setHgrow(picker, Priority.ALWAYS);
        row.getChildren().addAll(label, picker);
        return row;
    }

    private ChoiceBox<Integer> createIntegerChoiceBox(Integer selectedValue) {
        ChoiceBox<Integer> choiceBox = new ChoiceBox<>();
        choiceBox.getStyleClass().add("client-profile-edit-choice-box");
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        choiceBox.getItems().setAll(1, 2, 3, 4, 5);
        if (selectedValue != null && selectedValue >= 1 && selectedValue <= 5) {
            choiceBox.setValue(selectedValue);
        }
        return choiceBox;
    }

    private ChoiceBox<String> createChoiceBox(String selectedValue) {
        return createChoiceBox(selectedValue, tipoClienteOptions);
    }

    private ChoiceBox<String> createChoiceBox(String selectedValue, List<String> sourceOptions) {
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getStyleClass().add("client-profile-edit-choice-box");
        choiceBox.setMaxWidth(Double.MAX_VALUE);
        List<String> options = new ArrayList<>(sourceOptions);
        String cleanSelectedValue = emptyFallbackForEdit(selectedValue);
        if (!cleanSelectedValue.isBlank() && !options.contains(cleanSelectedValue)) {
            options.add(0, cleanSelectedValue);
        }
        choiceBox.getItems().setAll(options);
        if (!cleanSelectedValue.isBlank()) {
            choiceBox.setValue(cleanSelectedValue);
        }
        return choiceBox;
    }

    private TextField createTextField(String value, String prompt) {
        TextField field = new TextField(emptyFallbackForEdit(value));
        field.setPromptText(prompt);
        field.getStyleClass().add("client-profile-edit-field");
        return field;
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

    private HBox createEditableInteractionActions(DatePicker nextCallPicker) {
        HBox actions = new HBox(8);
        actions.setMaxWidth(Double.MAX_VALUE);
        actions.getStyleClass().add("client-profile-edit-interaction-actions");
        nextCallPicker.setPrefWidth(EDIT_NEXT_CALL_PICKER_PREF_WIDTH);
        nextCallPicker.setMaxWidth(EDIT_NEXT_CALL_PICKER_MAX_WIDTH);
        HBox.setHgrow(nextCallPicker, Priority.NEVER);
        Button deleteButton = new Button("🗑");
        deleteButton.setAccessibleText("Elimina interazione");
        deleteButton.setMinWidth(DELETE_INTERACTION_BUTTON_WIDTH);
        deleteButton.setPrefWidth(DELETE_INTERACTION_BUTTON_WIDTH);
        deleteButton.getStyleClass().add("client-profile-delete-interaction-button");
        actions.getChildren().addAll(nextCallPicker, deleteButton);
        return actions;
    }

    private void configureEditableInteractionTextArea(TextArea textArea) {
        textArea.getStyleClass().add("client-profile-note-area");
        textArea.setWrapText(true);
        textArea.setMaxWidth(EDIT_INTERACTION_TEXT_AREA_MAX_WIDTH);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setMinHeight(EDIT_INTERACTION_TEXT_AREA_MIN_HEIGHT);
        textArea.setPrefRowCount(EDIT_INTERACTION_TEXT_AREA_PREF_ROWS);
        VBox.setVgrow(textArea, Priority.ALWAYS);
    }

    private void renderEditableTimeline(List<InteractionEditInput> interactions) {
        timelineSection.getTimelineList().getChildren().clear();
        timelineEditFields.clear();
        if (interactions.isEmpty()) {
            timelineSection.getTimelineList().getChildren().add(createInfoLabel("Nessuna interazione registrata"));
            return;
        }
        for (InteractionEditInput interaction : interactions) {
            VBox card = new VBox(8);
            card.setMaxWidth(Double.MAX_VALUE);
            card.getStyleClass().add("client-profile-timeline-card");
            Label title = createInfoLabel(DATE_FORMATTER.format(interaction.data()) + " · " + interaction.type().label());
            title.getStyleClass().add("client-profile-timeline-title");
            DatePicker nextCallPicker = null;
            if (interaction.type() == InteractionType.CHIAMATA) {
                nextCallPicker = new DatePicker(interaction.prossimoContatto());
                nextCallPicker.setPromptText("Prossima chiamata");
                nextCallPicker.getStyleClass().add("client-profile-call-date-picker");
                card.getChildren().addAll(title, createEditableInteractionActions(nextCallPicker));
            } else {
                card.getChildren().add(title);
            }
            TextArea textArea = new TextArea(interaction.testo());
            configureEditableInteractionTextArea(textArea);
            card.getChildren().add(textArea);
            timelineSection.getTimelineList().getChildren().add(card);
            timelineEditFields.add(new TimelineEditField(interaction.notaId(), interaction.interazioneId(), interaction.data(), interaction.type(), interaction.prossimoContatto(), nextCallPicker, textArea));
        }
    }

    private void setEditMode(boolean editMode) {
        header.setEditMode(editMode);
        timelineSection.setEditMode(editMode);
    }

    public void setFavorite(boolean favorite) {
        header.setFavorite(favorite);
    }

    public void showNoteEditor() {
        timelineSection.showNoteEditor();
    }

    public void showCallEditor() {
        timelineSection.showCallEditor();
    }

    public void hideNoteEditor() {
        timelineSection.hideNoteEditor();
    }

    public void setActiveTimelineFilter(TimelineFilter filter) {
        timelineSection.setActiveTimelineFilter(filter);
    }

    private void renderList(Pane container, List<String> values) {
        container.getChildren().clear();
        if (values.isEmpty()) {
            container.getChildren().add(createInfoLabel("Nessun dato disponibile"));
            return;
        }
        values.forEach(value -> container.getChildren().add(createInfoLabel(value)));
    }

    private void renderTimeline(List<InteractionPreview> interactions) {
        timelineSection.getTimelineList().getChildren().clear();
        if (interactions.isEmpty()) {
            timelineSection.getTimelineList().getChildren().add(createInfoLabel("Nessuna interazione registrata"));
            return;
        }
        for (InteractionPreview interaction : interactions) {
            VBox card = new VBox(4);
            card.getStyleClass().add("client-profile-timeline-card");
            Label title = createInfoLabel(DATE_FORMATTER.format(interaction.data()) + " · " + interaction.type().label());
            title.getStyleClass().add("client-profile-timeline-title");
            Label text = createInfoLabel(timelineText(interaction));
            card.getChildren().addAll(title, text);
            timelineSection.getTimelineList().getChildren().add(card);
        }
    }

    private String timelineText(InteractionPreview interaction) {
        if (interaction.prossimoContatto() == null) {
            return interaction.testo();
        }
        return interaction.testo() + "\nProssima chiamata: " + DATE_FORMATTER.format(interaction.prossimoContatto());
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("client-profile-info-label");
        label.setWrapText(true);
        return label;
    }

    private String lastCallText(List<InteractionPreview> interactions) {
        return interactions.stream()
                .filter(interaction -> interaction.type() == InteractionType.CHIAMATA)
                .findFirst()
                .map(interaction -> DATE_FORMATTER.format(interaction.data()))
                .orElse("-");
    }

    private String nextCallText(List<InteractionPreview> interactions) {
        return interactions.stream()
                .map(InteractionPreview::prossimoContatto)
                .filter(nextContact -> nextContact != null)
                .findFirst()
                .map(DATE_FORMATTER::format)
                .orElse("-");
    }

    private String lastEditableCallText(List<InteractionEditInput> interactions) {
        return interactions.stream()
                .filter(interaction -> interaction.type() == InteractionType.CHIAMATA)
                .findFirst()
                .map(interaction -> DATE_FORMATTER.format(interaction.data()))
                .orElse("-");
    }

    private String nextEditableCallText(List<InteractionEditInput> interactions) {
        return interactions.stream()
                .map(InteractionEditInput::prossimoContatto)
                .filter(nextContact -> nextContact != null)
                .findFirst()
                .map(DATE_FORMATTER::format)
                .orElse("-");
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMATTER.format(date);
    }

    private void renderContactList(Pane container, List<ContactItem> values) {
        renderList(container, values.stream().map(this::formatContact).toList());
    }

    private void renderAddressList(Pane container, List<AddressItem> values) {
        renderList(container, values.stream().map(this::formatAddress).toList());
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

    private String emptyFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String emptyFallback(Integer value) {
        return value == null ? "-" : value.toString();
    }

    private String emptyFallbackForEdit(String value) {
        return value == null ? "" : value;
    }

    public void setTipoClienteOptions(List<String> options) {
        tipoClienteOptions = options == null ? List.of() : List.copyOf(options);
    }

    public void setStatoTrattativaOptions(List<String> options) {
        statoTrattativaOptions = options == null ? List.of() : List.copyOf(options);
    }

    private String valueOf(TextField field) {
        return field == null ? "" : field.getText();
    }

    private String choiceValueOf(ChoiceBox<String> choiceBox) {
        String value = choiceBox == null ? null : choiceBox.getValue();
        return value == null ? "" : value;
    }

    private List<ValueEditInput> valuesOf(List<TextField> fields) {
        return fields.stream()
                .map(field -> new ValueEditInput((java.util.UUID) field.getUserData(), field.getText()))
                .toList();
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

    private String comboValue(ComboBox<String> comboBox) {
        String editorText = comboBox.getEditor().getText();
        if (editorText != null && !editorText.isBlank()) {
            return editorText.trim();
        }
        String value = comboBox.getValue();
        return value == null ? "" : value.trim();
    }


    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Slider getInvolvementSlider() {
        return header.getInvolvementSlider();
    }

    public boolean isUpdatingInvolvementSlider() {
        return header.isUpdatingInvolvementSlider();
    }

    public int involvementSliderValue() {
        return header.involvementSliderValue();
    }

    public void setInvolvementSliderValue(Integer value) {
        header.setInvolvementSliderValue(value);
    }

    public Button getFavoriteButton() {
        return header.getFavoriteButton();
    }

    public Button getEditProfileButton() {
        return header.getEditProfileButton();
    }

    public Button getSaveProfileEditButton() {
        return header.getSaveProfileEditButton();
    }

    public Button getCancelProfileEditButton() {
        return header.getCancelProfileEditButton();
    }

    public Button getNewNoteButton() {
        return timelineSection.getNewNoteButton();
    }

    public Button getNewCallButton() {
        return timelineSection.getNewCallButton();
    }

    public Button getAllFilterButton() {
        return timelineSection.getAllFilterButton();
    }

    public Button getNotesFilterButton() {
        return timelineSection.getNotesFilterButton();
    }

    public Button getCallsFilterButton() {
        return timelineSection.getCallsFilterButton();
    }

    public DatePicker getNextCallDatePicker() {
        return timelineSection.getNextCallDatePicker();
    }

    public TextArea getNoteTextArea() {
        return timelineSection.getNoteTextArea();
    }

    public Button getSaveNoteButton() {
        return timelineSection.getSaveNoteButton();
    }

    public Button getCancelNoteButton() {
        return timelineSection.getCancelNoteButton();
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

    private record TimelineEditField(
            java.util.UUID notaId,
            java.util.UUID interazioneId,
            LocalDate data,
            InteractionType type,
            LocalDate prossimoContatto,
            DatePicker nextCallPicker,
            TextArea textArea
    ) {
    }
}

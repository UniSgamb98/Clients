package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.core.ui.AppHeader;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SchedaClienteView extends BorderPane {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AppHeader header;
    private final AppSidebar sidebar;
    private final Label titleLabel;
    private final Label subtitleLabel;
    private final Label acquisitionLabel;
    private final Label lastInteractionLabel;
    private final Label nextInteractionLabel;
    private final Button favoriteButton;
    private final Button editProfileButton;
    private final Button saveProfileEditButton;
    private final Button cancelProfileEditButton;
    private final Button newNoteButton;
    private final Button newCallButton;
    private final Button allFilterButton;
    private final Button notesFilterButton;
    private final Button callsFilterButton;
    private final VBox customerDataList;
    private final VBox contactsList;
    private final VBox addressesList;
    private final VBox timelineList;
    private final VBox noteEditor;
    private final DatePicker nextCallDatePicker;
    private final TextArea noteTextArea;
    private final Button saveNoteButton;
    private final Button cancelNoteButton;
    private final List<TextField> phoneEditFields = new ArrayList<>();
    private final List<TextField> emailEditFields = new ArrayList<>();
    private final List<TextField> siteEditFields = new ArrayList<>();
    private final List<ContactEditControls> contactEditControls = new ArrayList<>();
    private final List<AddressEditControls> addressEditControls = new ArrayList<>();
    private final List<TimelineEditField> timelineEditFields = new ArrayList<>();
    private TextField ragioneSocialeEditField;
    private TextField tipoClienteEditField;
    private TextField statoTrattativaEditField;
    private TextField partitaIvaEditField;
    private TextField codiceFiscaleEditField;
    private DatePicker acquisizioneEditPicker;

    public SchedaClienteView() {
        header = new AppHeader("Scheda cliente");
        sidebar = new AppSidebar();
        titleLabel = new Label("Cliente");
        titleLabel.getStyleClass().add("clients-title");
        subtitleLabel = new Label("Profilo cliente e storico comunicazioni");
        subtitleLabel.getStyleClass().add("clients-subtitle");
        acquisitionLabel = createBadgeLabel();
        lastInteractionLabel = createBadgeLabel();
        nextInteractionLabel = createBadgeLabel();
        favoriteButton = new Button("☆");
        favoriteButton.getStyleClass().add("client-profile-favorite-button");
        editProfileButton = new Button("Modifica");
        editProfileButton.getStyleClass().add("clients-filter-button");
        saveProfileEditButton = new Button("Salva modifiche");
        saveProfileEditButton.getStyleClass().add("clients-primary-button");
        cancelProfileEditButton = new Button("Annulla");
        cancelProfileEditButton.getStyleClass().add("clients-filter-button");
        newNoteButton = new Button("+ Nuova nota");
        newNoteButton.getStyleClass().add("clients-primary-button");
        newCallButton = new Button("+ Nuova chiamata");
        newCallButton.getStyleClass().add("clients-filter-button");
        allFilterButton = createTimelineFilterButton("Tutti");
        notesFilterButton = createTimelineFilterButton("Solo note");
        callsFilterButton = createTimelineFilterButton("Solo chiamate");
        customerDataList = new VBox(8);
        contactsList = new VBox(8);
        addressesList = new VBox(8);
        timelineList = new VBox(10);
        noteEditor = createNoteEditor();
        nextCallDatePicker = new DatePicker();
        nextCallDatePicker.setPromptText("Prossima chiamata");
        nextCallDatePicker.getStyleClass().add("client-profile-call-date-picker");
        noteTextArea = new TextArea();
        noteTextArea.setPromptText("Scrivi una nota sulla comunicazione con il cliente...");
        noteTextArea.getStyleClass().add("client-profile-note-area");
        saveNoteButton = new Button("Salva");
        saveNoteButton.getStyleClass().add("clients-primary-button");
        cancelNoteButton = new Button("Annulla");
        cancelNoteButton.getStyleClass().add("clients-filter-button");
        noteEditor.getChildren().addAll(nextCallDatePicker, noteTextArea, createNoteActions());
        setActiveTimelineFilter(TimelineFilter.ALL);
        setEditMode(false);
        hideNoteEditor();

        setTop(header);
        setLeft(sidebar);
        setCenter(createContent());
    }

    private VBox createContent() {
        VBox content = new VBox(18);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("clients-content");

        VBox body = new VBox(18);
        body.getChildren().addAll(createHero(), createMainColumns());

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("new-client-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        content.getChildren().add(scrollPane);
        return content;
    }

    private VBox createHero() {
        VBox hero = new VBox(12);
        hero.getStyleClass().add("client-profile-hero");

        HBox titleRow = new HBox(12);
        titleRow.getStyleClass().add("clients-title-bar");
        VBox titleBox = new VBox(4);
        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(titleBox, spacer, favoriteButton, editProfileButton, saveProfileEditButton, cancelProfileEditButton);

        HBox callBadges = new HBox(10);
        callBadges.getStyleClass().add("client-profile-badges");
        callBadges.getChildren().addAll(acquisitionLabel, lastInteractionLabel, nextInteractionLabel);

        hero.getChildren().addAll(titleRow, callBadges);
        return hero;
    }

    private HBox createMainColumns() {
        HBox columns = new HBox(18);
        VBox leftColumn = new VBox(14);
        VBox rightColumn = new VBox(14);
        leftColumn.getStyleClass().add("client-profile-column");
        rightColumn.getStyleClass().add("client-profile-column");
        leftColumn.getChildren().addAll(
                createSection("Dati cliente", customerDataList),
                createSection("Contatti", contactsList),
                createSection("Indirizzi", addressesList)
        );
        rightColumn.getChildren().add(createTimelineSection());
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);
        columns.getChildren().addAll(leftColumn, rightColumn);
        return columns;
    }

    private VBox createTimelineSection() {
        VBox section = createSection("Interazioni", timelineList);
        HBox actions = new HBox(10);
        actions.getStyleClass().add("client-profile-timeline-actions");
        actions.getChildren().addAll(newNoteButton, newCallButton);

        HBox filters = new HBox(6);
        filters.getStyleClass().add("client-profile-timeline-filter-bar");
        filters.getChildren().addAll(allFilterButton, notesFilterButton, callsFilterButton);

        section.getChildren().add(1, actions);
        section.getChildren().add(2, filters);
        section.getChildren().add(3, noteEditor);
        return section;
    }

    private VBox createSection(String titleText, VBox body) {
        VBox section = new VBox(12);
        section.getStyleClass().add("new-client-section");
        Label title = new Label(titleText);
        title.getStyleClass().add("new-client-section-title");
        section.getChildren().addAll(title, body);
        return section;
    }

    private VBox createNoteEditor() {
        VBox editor = new VBox(10);
        editor.getStyleClass().add("client-profile-note-editor");
        return editor;
    }

    private HBox createNoteActions() {
        HBox actions = new HBox(10);
        actions.getChildren().addAll(saveNoteButton, cancelNoteButton);
        return actions;
    }

    private Button createTimelineFilterButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("client-profile-small-filter-button");
        return button;
    }

    private Label createBadgeLabel() {
        Label label = new Label();
        label.getStyleClass().add("client-profile-badge");
        return label;
    }

    public void renderProfile(ClienteProfile profile) {
        setEditMode(false);
        titleLabel.setText(profile.ragioneSociale());
        subtitleLabel.setText(profile.tipoCliente() + " · " + profile.statoTrattativa());
        acquisitionLabel.setText("Acquisito " + formatDate(profile.acquisizione()));
        lastInteractionLabel.setText("Ultima chiamata " + lastCallText(profile.interazioni()));
        nextInteractionLabel.setText("Prossima chiamata " + nextCallText(profile.interazioni()));
        setFavorite(profile.favorite());
        renderList(customerDataList, List.of(
                "Ragione sociale: " + emptyFallback(profile.ragioneSociale()),
                "Tipo cliente: " + emptyFallback(profile.tipoCliente()),
                "Stato trattativa: " + emptyFallback(profile.statoTrattativa()),
                "Partita IVA: " + emptyFallback(profile.partitaIva()),
                "Codice fiscale: " + emptyFallback(profile.codiceFiscale()),
                "Acquisizione: " + formatDate(profile.acquisizione()),
                "Telefoni azienda: " + joinProfileValues(profile.telefoni()),
                "Email azienda: " + joinProfileValues(profile.email()),
                "Siti web: " + joinProfileValues(profile.sitiWeb())
        ));
        renderContactList(contactsList, profile.contatti());
        renderAddressList(addressesList, profile.indirizzi());
        renderTimeline(profile.interazioni());
    }

    public void renderEditableProfile(EditProfileDraft draft) {
        setEditMode(true);
        titleLabel.setText(draft.ragioneSociale().isBlank() ? "Cliente" : draft.ragioneSociale());
        subtitleLabel.setText("Modifica dati cliente");
        acquisitionLabel.setText("Acquisito " + formatDate(draft.acquisizione()));
        lastInteractionLabel.setText("Ultima chiamata " + lastEditableCallText(draft.interazioni()));
        nextInteractionLabel.setText("Prossima chiamata " + nextEditableCallText(draft.interazioni()));
        setActiveTimelineFilter(TimelineFilter.ALL);
        renderCustomerDataEditor(draft);
        renderEditableContacts(draft.contatti());
        renderEditableAddresses(draft.indirizzi());
        renderEditableTimeline(draft.interazioni());
    }

    public EditProfileDraft collectEditDraft() {
        return new EditProfileDraft(
                valueOf(ragioneSocialeEditField),
                valueOf(tipoClienteEditField),
                valueOf(statoTrattativaEditField),
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
        customerDataList.getChildren().clear();
        ragioneSocialeEditField = createTextField(draft.ragioneSociale(), "Ragione sociale");
        tipoClienteEditField = createTextField(draft.tipoCliente(), "Tipo cliente");
        statoTrattativaEditField = createTextField(draft.statoTrattativa(), "Stato trattativa");
        partitaIvaEditField = createTextField(draft.partitaIva(), "Partita IVA");
        codiceFiscaleEditField = createTextField(draft.codiceFiscale(), "Codice fiscale");
        acquisizioneEditPicker = new DatePicker(draft.acquisizione());
        acquisizioneEditPicker.getStyleClass().add("client-profile-call-date-picker");

        customerDataList.getChildren().addAll(
                createFieldRow("Ragione sociale", ragioneSocialeEditField),
                createFieldRow("Tipo cliente", tipoClienteEditField),
                createFieldRow("Stato trattativa", statoTrattativaEditField),
                createFieldRow("Partita IVA", partitaIvaEditField),
                createFieldRow("Codice fiscale", codiceFiscaleEditField),
                createDateRow("Acquisizione", acquisizioneEditPicker),
                createEditableValuesSection("Telefoni azienda", phoneEditFields, draft.telefoni(), "Telefono azienda"),
                createEditableValuesSection("Email azienda", emailEditFields, draft.email(), "Email azienda"),
                createEditableValuesSection("Siti web", siteEditFields, draft.sitiWeb(), "Sito web")
        );
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
        HBox row = new HBox(8);
        row.getStyleClass().add("client-profile-edit-row");
        Button addButton = new Button("+");
        addButton.getStyleClass().add("client-profile-small-filter-button");
        Button removeButton = new Button("-");
        removeButton.getStyleClass().add("client-profile-small-filter-button");
        addButton.setOnAction(event -> addEditableValueRow(container, target, null, "", prompt));
        removeButton.setOnAction(event -> {
            target.remove(field);
            container.getChildren().remove(row);
            if (target.isEmpty()) {
                addEditableValueRow(container, target, null, "", prompt);
            }
        });
        HBox.setHgrow(field, Priority.ALWAYS);
        row.getChildren().addAll(field, addButton, removeButton);
        container.getChildren().add(row);
    }

    private void renderEditableContacts(List<ContactEditInput> values) {
        contactsList.getChildren().clear();
        contactEditControls.clear();
        List<ContactEditInput> safeValues = values.isEmpty() ? List.of(new ContactEditInput(null, "", List.of(), List.of())) : values;
        safeValues.forEach(this::addContactEditor);
    }

    private void addContactEditor(ContactEditInput value) {
        VBox card = new VBox(8);
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

    private void renderEditableAddresses(List<AddressEditInput> values) {
        addressesList.getChildren().clear();
        addressEditControls.clear();
        List<AddressEditInput> safeValues = values.isEmpty() ? List.of(emptyAddressInput()) : values;
        safeValues.forEach(this::addAddressEditor);
    }

    private AddressEditInput emptyAddressInput() {
        return new AddressEditInput(null, "", "", "", "", "", "", "", false);
    }

    private void addAddressEditor(AddressEditInput value) {
        VBox card = new VBox(8);
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

    private void renderEditableTimeline(List<InteractionEditInput> interactions) {
        timelineList.getChildren().clear();
        timelineEditFields.clear();
        if (interactions.isEmpty()) {
            timelineList.getChildren().add(createInfoLabel("Nessuna interazione registrata"));
            return;
        }
        for (InteractionEditInput interaction : interactions) {
            VBox card = new VBox(8);
            card.getStyleClass().add("client-profile-timeline-card");
            Label title = createInfoLabel(DATE_FORMATTER.format(interaction.data()) + " · " + interaction.type().label());
            title.getStyleClass().add("client-profile-timeline-title");
            DatePicker nextCallPicker = null;
            if (interaction.type() == InteractionType.CHIAMATA) {
                nextCallPicker = new DatePicker(interaction.prossimoContatto());
                nextCallPicker.setPromptText("Prossima chiamata");
                nextCallPicker.getStyleClass().add("client-profile-call-date-picker");
                card.getChildren().addAll(title, nextCallPicker);
            } else {
                card.getChildren().add(title);
            }
            TextArea textArea = new TextArea(interaction.testo());
            textArea.getStyleClass().add("client-profile-note-area");
            textArea.setPrefRowCount(3);
            card.getChildren().add(textArea);
            timelineList.getChildren().add(card);
            timelineEditFields.add(new TimelineEditField(interaction.notaId(), interaction.interazioneId(), interaction.data(), interaction.type(), interaction.prossimoContatto(), nextCallPicker, textArea));
        }
    }

    private void setEditMode(boolean editMode) {
        editProfileButton.setVisible(!editMode);
        editProfileButton.setManaged(!editMode);
        saveProfileEditButton.setVisible(editMode);
        saveProfileEditButton.setManaged(editMode);
        cancelProfileEditButton.setVisible(editMode);
        cancelProfileEditButton.setManaged(editMode);
        newNoteButton.setDisable(editMode);
        newCallButton.setDisable(editMode);
        allFilterButton.setDisable(editMode);
        notesFilterButton.setDisable(editMode);
        callsFilterButton.setDisable(editMode);
    }

    public void setFavorite(boolean favorite) {
        favoriteButton.setText(favorite ? "★" : "☆");
        favoriteButton.getStyleClass().remove("client-profile-favorite-active");
        if (favorite) {
            favoriteButton.getStyleClass().add("client-profile-favorite-active");
        }
    }

    public void showNoteEditor() {
        saveNoteButton.setText("Salva nota");
        nextCallDatePicker.setVisible(false);
        nextCallDatePicker.setManaged(false);
        showEditor();
    }

    public void showCallEditor() {
        saveNoteButton.setText("Salva chiamata");
        nextCallDatePicker.setVisible(true);
        nextCallDatePicker.setManaged(true);
        showEditor();
    }

    private void showEditor() {
        noteEditor.setVisible(true);
        noteEditor.setManaged(true);
        noteTextArea.requestFocus();
    }

    public void hideNoteEditor() {
        noteEditor.setVisible(false);
        noteEditor.setManaged(false);
        noteTextArea.clear();
        nextCallDatePicker.setValue(null);
    }

    public void setActiveTimelineFilter(TimelineFilter filter) {
        allFilterButton.getStyleClass().remove("client-profile-small-filter-active");
        notesFilterButton.getStyleClass().remove("client-profile-small-filter-active");
        callsFilterButton.getStyleClass().remove("client-profile-small-filter-active");

        Button activeButton = switch (filter) {
            case NOTES -> notesFilterButton;
            case CALLS -> callsFilterButton;
            case ALL -> allFilterButton;
        };
        activeButton.getStyleClass().add("client-profile-small-filter-active");
    }

    private void renderList(VBox container, List<String> values) {
        container.getChildren().clear();
        if (values.isEmpty()) {
            container.getChildren().add(createInfoLabel("Nessun dato disponibile"));
            return;
        }
        values.forEach(value -> container.getChildren().add(createInfoLabel(value)));
    }

    private void renderTimeline(List<InteractionPreview> interactions) {
        timelineList.getChildren().clear();
        if (interactions.isEmpty()) {
            timelineList.getChildren().add(createInfoLabel("Nessuna interazione registrata"));
            return;
        }
        for (InteractionPreview interaction : interactions) {
            VBox card = new VBox(4);
            card.getStyleClass().add("client-profile-timeline-card");
            Label title = createInfoLabel(DATE_FORMATTER.format(interaction.data()) + " · " + interaction.type().label());
            title.getStyleClass().add("client-profile-timeline-title");
            Label text = createInfoLabel(timelineText(interaction));
            card.getChildren().addAll(title, text);
            timelineList.getChildren().add(card);
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

    private void renderContactList(VBox container, List<ContactItem> values) {
        renderList(container, values.stream().map(this::formatContact).toList());
    }

    private void renderAddressList(VBox container, List<AddressItem> values) {
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

    private String emptyFallbackForEdit(String value) {
        return value == null ? "" : value;
    }

    private String valueOf(TextField field) {
        return field == null ? "" : field.getText();
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

    public AppHeader getHeader() {
        return header;
    }

    public AppSidebar getSidebar() {
        return sidebar;
    }

    public Button getFavoriteButton() {
        return favoriteButton;
    }

    public Button getEditProfileButton() {
        return editProfileButton;
    }

    public Button getSaveProfileEditButton() {
        return saveProfileEditButton;
    }

    public Button getCancelProfileEditButton() {
        return cancelProfileEditButton;
    }

    public Button getNewNoteButton() {
        return newNoteButton;
    }

    public Button getNewCallButton() {
        return newCallButton;
    }

    public Button getAllFilterButton() {
        return allFilterButton;
    }

    public Button getNotesFilterButton() {
        return notesFilterButton;
    }

    public Button getCallsFilterButton() {
        return callsFilterButton;
    }

    public DatePicker getNextCallDatePicker() {
        return nextCallDatePicker;
    }

    public TextArea getNoteTextArea() {
        return noteTextArea;
    }

    public Button getSaveNoteButton() {
        return saveNoteButton;
    }

    public Button getCancelNoteButton() {
        return cancelNoteButton;
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

package com.example.clients.feature.clienti.nuovocliente.controller;

import com.example.clients.core.database.query.ClienteLookupQuery.LookupValues;
import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.nuovocliente.dto.ClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.ContattoClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.EmailClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.IndirizzoClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.NuovoClienteRequest;
import com.example.clients.feature.clienti.nuovocliente.dto.SitoWebClienteInput;
import com.example.clients.feature.clienti.nuovocliente.dto.TelefonoClienteInput;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView.ContactEntryControls;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class NuovoClienteController {

    private final NuovoClienteView view;
    private final ClientiNav clientiNav;
    private final NuovoClienteService service;

    public NuovoClienteController(NuovoClienteView view, ClientiNav clientiNav, NuovoClienteService service) {
        this.view = view;
        this.clientiNav = clientiNav;
        this.service = service;
        configureActions();
    }

    private void configureActions() {
        configureLookupSuggestions();
        view.getCancelButton().setOnAction(event -> clientiNav.showClienti());
        view.getSaveButton().setOnAction(event -> {
            try {
                NuovoClienteService.NuovoClienteDraft draft = service.saveCliente(createRequest());
                clientiNav.showSchedaCliente(draft.cliente().id());
            } catch (RuntimeException e) {
                showError("Salvataggio cliente non riuscito", e);
            }
        });
        view.getAddWebsiteButton().setOnAction(event -> view.addWebsiteField().requestFocus());
        view.getAddAddressButton().setOnAction(event -> view.addAddressField().requestFocus());
        view.getAddContactButton().setOnAction(event -> {
            ContactEntryControls controls = view.addContactEntry(
                    nonBlankValues(view.getPhoneFields()),
                    nonBlankValues(view.getEmailFields())
            );
            configureContactRemoval(controls);
            configureContactAutocomplete(controls);
            controls.contactField().requestFocus();
        });
        view.getAddEmailButton().setOnAction(event -> {
            TextField field = view.addEmailField();
            configureContactOptionsRefresh(field);
            configureTextFieldAutocomplete(field, service.lookupValues().email());
            refreshContactOptions();
            field.requestFocus();
        });
        view.getAddPhoneButton().setOnAction(event -> {
            TextField field = view.addPhoneField();
            configureContactOptionsRefresh(field);
            configureTextFieldAutocomplete(field, service.lookupValues().telefoni());
            refreshContactOptions();
            field.requestFocus();
        });
        view.getEmailFields().forEach(this::configureContactOptionsRefresh);
        view.getPhoneFields().forEach(this::configureContactOptionsRefresh);
        view.getContactEntries().forEach(this::configureContactRemoval);
        view.getContactEntries().forEach(this::configureContactAutocomplete);
        refreshContactOptions();
    }

    private void configureLookupSuggestions() {
        try {
            LookupValues values = service.lookupValues();
            view.setClientTypeOptions(values.tipiCliente());
            view.setStatusOptions(values.statiTrattativa());
            configureComboAutocomplete(view.getTypeField(), values.tipiCliente());
            configureComboAutocomplete(view.getStatusField(), values.statiTrattativa());
            view.getPhoneFields().forEach(field -> configureTextFieldAutocomplete(field, values.telefoni()));
            view.getEmailFields().forEach(field -> configureTextFieldAutocomplete(field, values.email()));
        } catch (RuntimeException e) {
            showError("Caricamento suggerimenti non riuscito", e);
        }
    }

    private void configureContactRemoval(ContactEntryControls controls) {
        controls.removeButton().setOnAction(event -> view.removeContactEntry(controls));
    }

    private void configureContactAutocomplete(ContactEntryControls controls) {
        configureComboAutocomplete(controls.phoneField(), nonBlankValues(view.getPhoneFields()));
        configureComboAutocomplete(controls.emailField(), nonBlankValues(view.getEmailFields()));
    }

    private void configureContactOptionsRefresh(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (view.getPhoneFields().contains(field)) {
                updateLinkedContactValues(oldValue, newValue, true);
            } else if (view.getEmailFields().contains(field)) {
                updateLinkedContactValues(oldValue, newValue, false);
            }
            refreshContactOptions();
        });
    }

    private void updateLinkedContactValues(String oldValue, String newValue, boolean phoneValue) {
        if (oldValue == null || oldValue.isBlank()) {
            return;
        }
        String cleanOldValue = oldValue.trim();
        String cleanNewValue = newValue == null ? "" : newValue.trim();
        List<ComboBox<String>> fields = phoneValue ? view.getContactPhoneFields() : view.getContactEmailFields();
        fields.stream()
                .filter(field -> comboText(field).equals(cleanOldValue))
                .forEach(field -> field.getEditor().setText(cleanNewValue));
    }

    private void refreshContactOptions() {
        view.setContactPhoneOptions(nonBlankValues(view.getPhoneFields()));
        view.setContactEmailOptions(nonBlankValues(view.getEmailFields()));
    }

    private NuovoClienteRequest createRequest() {
        return new NuovoClienteRequest(
                new ClienteInput(
                        text(view.getNameField()),
                        comboText(view.getTypeField()),
                        comboText(view.getStatusField()),
                        text(view.getVatField()),
                        text(view.getFiscalCodeField()),
                        parseDate(text(view.getAcquisitionField())),
                        text(view.getOperatorField())
                ),
                indirizzi(),
                telefoni(view.getPhoneFields()),
                email(view.getEmailFields()),
                sitiWeb(),
                contactInputs()
        );
    }

    private List<IndirizzoClienteInput> indirizzi() {
        List<IndirizzoClienteInput> indirizzi = new ArrayList<>();
        indirizzi.add(new IndirizzoClienteInput(
                text(view.getCountryField()),
                text(view.getRegionField()),
                text(view.getProvinceField()),
                text(view.getCityField()),
                text(view.getAddressField()),
                text(view.getStreetNumberField()),
                text(view.getZipField()),
                true
        ));
        view.getExtraAddressFields().stream()
                .map(NuovoClienteController::text)
                .map(value -> new IndirizzoClienteInput("", "", "", "", value, "", "", false))
                .forEach(indirizzi::add);
        return indirizzi;
    }

    private List<SitoWebClienteInput> sitiWeb() {
        return values(view.getWebsiteFields()).stream()
                .map(SitoWebClienteInput::new)
                .toList();
    }

    private static List<TelefonoClienteInput> telefoni(List<TextField> fields) {
        return values(fields).stream()
                .map(TelefonoClienteInput::new)
                .toList();
    }

    private static List<EmailClienteInput> email(List<TextField> fields) {
        return values(fields).stream()
                .map(EmailClienteInput::new)
                .toList();
    }

    private List<ContattoClienteInput> contactInputs() {
        List<ContattoClienteInput> inputs = new ArrayList<>();
        for (int index = 0; index < view.getContactFields().size(); index++) {
            String telefono = comboText(view.getContactPhoneFields().get(index));
            String email = comboText(view.getContactEmailFields().get(index));
            inputs.add(new ContattoClienteInput(
                    text(view.getContactFields().get(index)),
                    telefono.isBlank() ? List.of() : List.of(new TelefonoClienteInput(telefono)),
                    email.isBlank() ? List.of() : List.of(new EmailClienteInput(email))
            ));
        }
        return inputs;
    }

    private static List<String> values(List<TextField> fields) {
        return fields.stream()
                .map(NuovoClienteController::text)
                .toList();
    }

    private static List<String> nonBlankValues(List<TextField> fields) {
        return fields.stream()
                .map(NuovoClienteController::text)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }


    private void configureTextFieldAutocomplete(TextField field, List<String> options) {
        final boolean[] updating = {false};
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (updating[0] || newValue == null || newValue.isBlank()) {
                return;
            }
            firstMatch(newValue, options).ifPresent(match -> {
                if (!match.equals(newValue)) {
                    updating[0] = true;
                    field.setText(match);
                    field.positionCaret(newValue.length());
                    field.selectRange(newValue.length(), match.length());
                    updating[0] = false;
                }
            });
        });
    }

    private void configureComboAutocomplete(ComboBox<String> comboBox, List<String> options) {
        comboBox.getProperties().put("autocompleteOptions", List.copyOf(options));
        final boolean[] updating = {false};
        comboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (updating[0]) {
                return;
            }
            List<String> currentOptions = comboAutocompleteOptions(comboBox);
            if (newValue == null || newValue.isBlank()) {
                comboBox.getItems().setAll(currentOptions);
                return;
            }
            comboBox.getItems().setAll(currentOptions.stream()
                    .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(newValue.toLowerCase(Locale.ROOT)))
                    .toList());
            firstMatch(newValue, currentOptions).ifPresent(match -> {
                if (!match.equals(newValue)) {
                    updating[0] = true;
                    comboBox.getEditor().setText(match);
                    comboBox.getEditor().positionCaret(newValue.length());
                    comboBox.getEditor().selectRange(newValue.length(), match.length());
                    updating[0] = false;
                }
            });
        });
    }

    @SuppressWarnings("unchecked")
    private List<String> comboAutocompleteOptions(ComboBox<String> comboBox) {
        Object options = comboBox.getProperties().get("autocompleteOptions");
        return options instanceof List<?> ? (List<String>) options : List.copyOf(comboBox.getItems());
    }

    private Optional<String> firstMatch(String value, List<String> options) {
        String lowerValue = value.toLowerCase(Locale.ROOT);
        return options.stream()
                .filter(option -> option != null && option.toLowerCase(Locale.ROOT).startsWith(lowerValue))
                .findFirst();
    }

    private static LocalDate parseDate(String value) {
        if (value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private static String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private static String comboText(ComboBox<String> comboBox) {
        String editorText = comboBox.getEditor().getText();
        if (editorText != null && !editorText.isBlank()) {
            return editorText.trim();
        }
        String value = comboBox.getValue();
        return value == null ? "" : value.trim();
    }

    private void showError(String title, RuntimeException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage() == null ? "Errore imprevisto." : e.getMessage());
        alert.showAndWait();
    }

    public NuovoClienteView getView() {
        return view;
    }

    public ClientiNav getClientiNav() {
        return clientiNav;
    }

    public NuovoClienteService getService() {
        return service;
    }
}

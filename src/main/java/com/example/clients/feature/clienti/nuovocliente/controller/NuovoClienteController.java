package com.example.clients.feature.clienti.nuovocliente.controller;

import com.example.clients.feature.clienti.navigator.ClientiNav;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService.ContattoInput;
import com.example.clients.feature.clienti.nuovocliente.service.NuovoClienteService.NuovoClienteFormData;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView;
import com.example.clients.feature.clienti.nuovocliente.view.NuovoClienteView.ContactEntryControls;
import java.util.ArrayList;
import java.util.List;
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
        view.getCancelButton().setOnAction(event -> clientiNav.showClienti());
        view.getSaveButton().setOnAction(event -> service.saveCliente(createFormData()));
        view.getAddWebsiteButton().setOnAction(event -> view.addWebsiteField().requestFocus());
        view.getAddAddressButton().setOnAction(event -> view.addAddressField().requestFocus());
        view.getAddContactButton().setOnAction(event -> {
            ContactEntryControls controls = view.addContactEntry(
                    nonBlankValues(view.getPhoneFields()),
                    nonBlankValues(view.getEmailFields())
            );
            configureContactRemoval(controls);
            controls.contactField().requestFocus();
        });
        view.getAddEmailButton().setOnAction(event -> {
            TextField field = view.addEmailField();
            configureContactOptionsRefresh(field);
            refreshContactOptions();
            field.requestFocus();
        });
        view.getAddPhoneButton().setOnAction(event -> {
            TextField field = view.addPhoneField();
            configureContactOptionsRefresh(field);
            refreshContactOptions();
            field.requestFocus();
        });
        view.getEmailFields().forEach(this::configureContactOptionsRefresh);
        view.getPhoneFields().forEach(this::configureContactOptionsRefresh);
        view.getContactEntries().forEach(this::configureContactRemoval);
        refreshContactOptions();
    }

    private void configureContactRemoval(ContactEntryControls controls) {
        controls.removeButton().setOnAction(event -> view.removeContactEntry(controls));
    }

    private void configureContactOptionsRefresh(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> refreshContactOptions());
    }

    private void refreshContactOptions() {
        view.setContactPhoneOptions(nonBlankValues(view.getPhoneFields()));
        view.setContactEmailOptions(nonBlankValues(view.getEmailFields()));
    }

    private NuovoClienteFormData createFormData() {
        return new NuovoClienteFormData(
                text(view.getNameField()),
                text(view.getTypeField()),
                text(view.getStatusField()),
                text(view.getVatField()),
                text(view.getFiscalCodeField()),
                text(view.getAcquisitionField()),
                text(view.getOperatorField()),
                text(view.getCountryField()),
                text(view.getRegionField()),
                text(view.getProvinceField()),
                text(view.getCityField()),
                text(view.getAddressField()),
                text(view.getStreetNumberField()),
                text(view.getZipField()),
                values(view.getExtraAddressFields()),
                values(view.getWebsiteFields()),
                values(view.getEmailFields()),
                values(view.getPhoneFields()),
                contactInputs()
        );
    }

    private List<ContattoInput> contactInputs() {
        List<ContattoInput> inputs = new ArrayList<>();
        for (int index = 0; index < view.getContactFields().size(); index++) {
            inputs.add(new ContattoInput(
                    text(view.getContactFields().get(index)),
                    comboText(view.getContactPhoneFields().get(index)),
                    comboText(view.getContactEmailFields().get(index))
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

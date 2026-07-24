package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoCatalogItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoClienteEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoClienteItem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/** Full-width panel reserved for the customer's production resources. */
final class ClienteProfileResourcesPanel extends VBox {

    private final FlowPane resources = new FlowPane(12, 12);
    private final ClienteResourceSection forniSection = new ClienteResourceSection("Forni", "+ Forno");
    private final List<FornoCatalogItem> forniCatalog = new ArrayList<>();
    private final List<FornoEditorRow> fornoEditorRows = new ArrayList<>();
    private boolean editMode;

    ClienteProfileResourcesPanel() {
        super(12);
        getStyleClass().add("client-profile-resources-panel");

        Label title = new Label("Risorse cliente");
        title.getStyleClass().add("client-profile-resources-title");

        resources.getStyleClass().add("client-profile-resources-flow");
        forniSection.addButton().setOnAction(event -> addFornoEditorRow(emptyForno()));
        setEditMode(false);

        getChildren().addAll(title, resources);
    }

    void setForniCatalog(List<FornoCatalogItem> values) {
        forniCatalog.clear();
        if (values != null) {
            forniCatalog.addAll(values);
        }
        fornoEditorRows.forEach(this::refreshSuggestions);
    }

    void renderForni(List<FornoClienteItem> forni) {
        editMode = false;
        resources.getChildren().setAll(forniSection);
        forniSection.clearCards();
        forniSection.setAddButtonVisible(false);

        if (forni == null || forni.isEmpty()) {
            forniSection.addCard(createEmptyLabel("Nessun forno associato"));
            return;
        }

        for (FornoClienteItem forno : forni) {
            forniSection.addCard(createFornoCard(forno));
        }
    }

    void renderForniEditor(List<FornoClienteEditInput> forni) {
        editMode = true;
        resources.getChildren().setAll(forniSection);
        forniSection.clearCards();
        fornoEditorRows.clear();
        forniSection.setAddButtonVisible(true);

        if (forni == null || forni.isEmpty()) {
            addFornoEditorRow(emptyForno());
            return;
        }

        forni.forEach(this::addFornoEditorRow);
    }

    List<FornoClienteEditInput> collectForni() {
        return fornoEditorRows.stream()
                .map(FornoEditorRow::toInput)
                .toList();
    }

    private VBox createFornoCard(FornoClienteItem forno) {
        VBox card = new VBox(8);
        card.getStyleClass().add("client-profile-forno-card");
        card.getChildren().addAll(
                createFornoDisplayRow("Tecnologia: " + display(forno.tecnologia()), "Anno: " + display(forno.anno())),
                createFornoDisplayRow("Marca: " + display(forno.marca()), "Modello: " + display(forno.modello()))
        );
        if (forno.nota() != null && !forno.nota().isBlank()) {
            Label note = new Label("Nota: " + forno.nota());
            note.getStyleClass().add("client-profile-resource-note");
            card.getChildren().add(note);
        }
        return card;
    }

    private HBox createFornoDisplayRow(String first, String second) {
        HBox row = new HBox(18);
        row.getStyleClass().add("client-profile-forno-row");
        Label firstLabel = new Label(first);
        Label secondLabel = new Label(second);
        firstLabel.getStyleClass().add("client-profile-resource-value");
        secondLabel.getStyleClass().add("client-profile-resource-value");
        row.getChildren().addAll(firstLabel, secondLabel);
        return row;
    }

    private void addFornoEditorRow(FornoClienteEditInput forno) {
        VBox card = new VBox(8);
        card.getStyleClass().add("client-profile-forno-card");

        ResourceAutocompleteComboBox tecnologia = createAutocompleteComboBox(forno.tecnologia(), "Tecnologia");
        ResourceAutocompleteComboBox anno = createAutocompleteComboBox(forno.anno(), "Anno");
        ResourceAutocompleteComboBox marca = createAutocompleteComboBox(forno.marca(), "Marca");
        ResourceAutocompleteComboBox modello = createAutocompleteComboBox(forno.modello(), "Modello");
        TextArea nota = new TextArea(display(forno.nota()));
        nota.setPromptText("Nota cliente-forno");
        nota.getStyleClass().add("client-profile-resource-note-editor");

        Button remove = new Button("−");
        remove.getStyleClass().add("client-profile-delete-interaction-button");

        FornoEditorRow editorRow = new FornoEditorRow(forno.id(), forno.fornoId(), card, tecnologia, anno, marca, modello, nota);
        fornoEditorRows.add(editorRow);
        remove.setOnAction(event -> {
            fornoEditorRows.remove(editorRow);
            forniSection.removeCard(card);
        });

        card.getChildren().addAll(
                createFornoEditorLine("Tecnologia", tecnologia, "Anno", anno),
                createFornoEditorLine("Marca", marca, "Modello", modello),
                nota,
                remove
        );
        forniSection.addCard(card);
        refreshSuggestions(editorRow);
    }

    private HBox createFornoEditorLine(String firstLabel, ResourceAutocompleteComboBox firstField, String secondLabel, ResourceAutocompleteComboBox secondField) {
        HBox row = new HBox(10);
        row.getStyleClass().add("client-profile-forno-row");
        row.getChildren().addAll(createFornoEditorField(firstLabel, firstField), createFornoEditorField(secondLabel, secondField));
        return row;
    }

    private VBox createFornoEditorField(String labelText, ResourceAutocompleteComboBox field) {
        VBox wrapper = new VBox(4);
        Label label = new Label(labelText + ":");
        label.getStyleClass().add("client-profile-resource-field-label");
        wrapper.getChildren().addAll(label, field);
        return wrapper;
    }

    private ResourceAutocompleteComboBox createAutocompleteComboBox(String value, String prompt) {
        ResourceAutocompleteComboBox comboBox = new ResourceAutocompleteComboBox(value, prompt, field -> fornoEditorRows.stream()
                .filter(row -> row.contains(field))
                .findFirst()
                .ifPresent(row -> refreshSuggestions(row, field)));
        comboBox.setOnShowing(event -> fornoEditorRows.stream()
                .filter(row -> row.contains(comboBox))
                .findFirst()
                .ifPresent(this::refreshSuggestions));
        return comboBox;
    }

    private void refreshSuggestions(FornoEditorRow row) {
        refreshSuggestions(row, null);
    }

    private void refreshSuggestions(FornoEditorRow row, ResourceAutocompleteComboBox activeField) {
        updateSuggestions(row.tecnologia(), FornoCatalogItem::tecnologia, row, false, activeField);
        updateSuggestions(row.anno(), FornoCatalogItem::anno, row, false, activeField);
        updateSuggestions(row.marca(), FornoCatalogItem::marca, row, false, activeField);
        updateSuggestions(row.modello(), FornoCatalogItem::modello, row, true, activeField);
    }

    private void updateSuggestions(ResourceAutocompleteComboBox field, Function<FornoCatalogItem, String> extractor, FornoEditorRow row, boolean filterByMarca, ResourceAutocompleteComboBox activeField) {
        field.setSuggestions(suggestionsFor(extractor, row, field, filterByMarca), field == activeField);
    }

    private List<String> suggestionsFor(Function<FornoCatalogItem, String> extractor, FornoEditorRow row, ResourceAutocompleteComboBox field, boolean filterByMarca) {
        Set<String> values = new LinkedHashSet<>();
        for (FornoCatalogItem item : forniCatalog) {
            if (matches(row.tecnologia(), item.tecnologia(), field)
                    && matches(row.anno(), item.anno(), field)
                    && matches(row.marca(), item.marca(), field)
                    && (!filterByMarca || matches(row.marca(), item.marca(), null))) {
                String value = extractor.apply(item);
                if (value != null && !value.isBlank()) {
                    values.add(value);
                }
            }
        }
        return new ArrayList<>(values);
    }

    private boolean matches(ResourceAutocompleteComboBox field, String candidate, ResourceAutocompleteComboBox ignoredField) {
        if (field == ignoredField) {
            return true;
        }
        String value = textOf(field);
        return value.isBlank() || display(candidate).toLowerCase().contains(value.toLowerCase());
    }

    private Label createEmptyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("client-profile-info-label");
        return label;
    }

    private FornoClienteEditInput emptyForno() {
        return new FornoClienteEditInput(null, null, "", "", "", "", "");
    }

    private String textOf(ResourceAutocompleteComboBox field) {
        return display(field.textValue());
    }

    private String display(String value) {
        return value == null ? "" : value;
    }

    void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    private record FornoEditorRow(
            UUID id,
            UUID fornoId,
            VBox card,
            ResourceAutocompleteComboBox tecnologia,
            ResourceAutocompleteComboBox anno,
            ResourceAutocompleteComboBox marca,
            ResourceAutocompleteComboBox modello,
            TextArea nota
    ) {
        private boolean contains(ResourceAutocompleteComboBox field) {
            return tecnologia == field || anno == field || marca == field || modello == field;
        }

        private FornoClienteEditInput toInput() {
            return new FornoClienteEditInput(id, fornoId, text(tecnologia), text(anno), text(marca), text(modello), nota.getText());
        }

        private static String text(ResourceAutocompleteComboBox field) {
            return field.trimmedTextValue();
        }
    }
}

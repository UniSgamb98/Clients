package com.example.clients.feature.clienti.schedacliente.view;

import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoCatalogItem;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoClienteEditInput;
import com.example.clients.feature.clienti.schedacliente.service.SchedaClienteService.FornoClienteItem;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** Full-width panel reserved for the customer's production resources. */
final class ClienteProfileResourcesPanel extends VBox {

    private final FlowPane resources = new FlowPane(12, 12);
    private final VBox forniList = new VBox(10);
    private final Button addFornoButton = new Button("+ Forno");
    private final List<FornoCatalogItem> forniCatalog = new ArrayList<>();
    private final List<FornoEditorRow> fornoEditorRows = new ArrayList<>();
    private boolean editMode;

    ClienteProfileResourcesPanel() {
        super(12);
        getStyleClass().add("client-profile-resources-panel");

        Label title = new Label("Risorse cliente");
        title.getStyleClass().add("client-profile-resources-title");

        resources.getStyleClass().add("client-profile-resources-flow");
        addFornoButton.getStyleClass().add("clients-primary-button");
        addFornoButton.setOnAction(event -> addFornoEditorRow(emptyForno()));
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
        resources.getChildren().setAll(createForniSection());
        forniList.getChildren().clear();
        addFornoButton.setVisible(false);
        addFornoButton.setManaged(false);

        if (forni == null || forni.isEmpty()) {
            forniList.getChildren().add(createEmptyLabel("Nessun forno associato"));
            return;
        }

        for (FornoClienteItem forno : forni) {
            forniList.getChildren().add(createFornoCard(forno));
        }
    }

    void renderForniEditor(List<FornoClienteEditInput> forni) {
        editMode = true;
        resources.getChildren().setAll(createForniSection());
        forniList.getChildren().clear();
        fornoEditorRows.clear();
        addFornoButton.setVisible(true);
        addFornoButton.setManaged(true);

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

    private VBox createForniSection() {
        VBox section = new VBox(10);
        section.getStyleClass().add("client-profile-resource-group");

        HBox header = new HBox(10);
        header.getStyleClass().add("client-profile-resource-group-header");
        Label title = new Label("Forni");
        title.getStyleClass().add("client-profile-resource-card-title");
        header.getChildren().addAll(title, addFornoButton);

        section.getChildren().addAll(header, forniList);
        return section;
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

        ComboBox<String> tecnologia = createEditableComboBox(forno.tecnologia(), "Tecnologia");
        ComboBox<String> anno = createEditableComboBox(forno.anno(), "Anno");
        ComboBox<String> marca = createEditableComboBox(forno.marca(), "Marca");
        ComboBox<String> modello = createEditableComboBox(forno.modello(), "Modello");
        TextArea nota = new TextArea(display(forno.nota()));
        nota.setPromptText("Nota cliente-forno");
        nota.getStyleClass().add("client-profile-resource-note-editor");

        Button remove = new Button("−");
        remove.getStyleClass().add("client-profile-delete-interaction-button");

        FornoEditorRow editorRow = new FornoEditorRow(forno.id(), forno.fornoId(), card, tecnologia, anno, marca, modello, nota);
        fornoEditorRows.add(editorRow);
        remove.setOnAction(event -> {
            fornoEditorRows.remove(editorRow);
            forniList.getChildren().remove(card);
        });

        card.getChildren().addAll(
                createFornoEditorLine("Tecnologia", tecnologia, "Anno", anno),
                createFornoEditorLine("Marca", marca, "Modello", modello),
                nota,
                remove
        );
        forniList.getChildren().add(card);
        refreshSuggestions(editorRow);
    }

    private HBox createFornoEditorLine(String firstLabel, ComboBox<String> firstField, String secondLabel, ComboBox<String> secondField) {
        HBox row = new HBox(10);
        row.getStyleClass().add("client-profile-forno-row");
        row.getChildren().addAll(createFornoEditorField(firstLabel, firstField), createFornoEditorField(secondLabel, secondField));
        return row;
    }

    private VBox createFornoEditorField(String labelText, ComboBox<String> field) {
        VBox wrapper = new VBox(4);
        Label label = new Label(labelText + ":");
        label.getStyleClass().add("client-profile-resource-field-label");
        wrapper.getChildren().addAll(label, field);
        return wrapper;
    }

    private ComboBox<String> createEditableComboBox(String value, String prompt) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(true);
        comboBox.setValue(display(value));
        comboBox.setPromptText(prompt);
        comboBox.getStyleClass().add("client-profile-resource-combo");
        comboBox.setOnShowing(event -> fornoEditorRows.stream()
                .filter(row -> row.contains(comboBox))
                .findFirst()
                .ifPresent(this::refreshSuggestions));
        comboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> fornoEditorRows.stream()
                .filter(row -> row.contains(comboBox))
                .findFirst()
                .ifPresent(this::refreshSuggestions));
        return comboBox;
    }

    private void refreshSuggestions(FornoEditorRow row) {
        updateSuggestions(row.tecnologia(), FornoCatalogItem::tecnologia, row, false);
        updateSuggestions(row.anno(), FornoCatalogItem::anno, row, false);
        updateSuggestions(row.marca(), FornoCatalogItem::marca, row, false);
        updateSuggestions(row.modello(), FornoCatalogItem::modello, row, true);
    }

    private void updateSuggestions(ComboBox<String> field, Function<FornoCatalogItem, String> extractor, FornoEditorRow row, boolean filterByMarca) {
        String currentText = textOf(field);
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
        field.getItems().setAll(values);
        field.getEditor().setText(currentText);
    }

    private boolean matches(ComboBox<String> field, String candidate, ComboBox<String> ignoredField) {
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

    private String textOf(ComboBox<String> field) {
        return display(field.getEditor().getText());
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
            ComboBox<String> tecnologia,
            ComboBox<String> anno,
            ComboBox<String> marca,
            ComboBox<String> modello,
            TextArea nota
    ) {
        private boolean contains(ComboBox<String> field) {
            return tecnologia == field || anno == field || marca == field || modello == field;
        }

        private FornoClienteEditInput toInput() {
            return new FornoClienteEditInput(id, fornoId, text(tecnologia), text(anno), text(marca), text(modello), nota.getText());
        }

        private static String text(ComboBox<String> field) {
            String value = field.getEditor().getText();
            return value == null ? "" : value.trim();
        }
    }
}

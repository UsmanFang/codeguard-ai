package com.byteanarchists.codeguard.ui;

import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.PrimerDark;
import com.byteanarchists.codeguard.api.ApiKeyStore;
import com.byteanarchists.codeguard.api.ModelPreferenceStore;
import com.byteanarchists.codeguard.util.SettingsStore;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;

public class SettingsView extends ScrollPane {

    private Runnable onModelChangedCallback; // <-- callback field

    public SettingsView() {
        setFitToWidth(true);
        setStyle("-fx-background: #282a36; -fx-background-color: #282a36;");

        VBox rootBox = new VBox(20);
        rootBox.setPadding(new Insets(24));
        setContent(rootBox);

        Label panelHeader = new Label("Settings Configuration");
        panelHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2; -fx-padding: 0 0 10 0;");
        rootBox.getChildren().add(panelHeader);

        // Section 0: Fireworks API Key
        VBox apiKeyCard = createSettingCard("Fireworks API Key");
        HBox keyRow = new HBox(10);
        keyRow.setAlignment(Pos.CENTER_LEFT);

        PasswordField apiKeyField = new PasswordField();
        apiKeyField.setPromptText("fw_...");
        apiKeyField.setPrefWidth(320);
        String existingKey = ApiKeyStore.resolveApiKey();
        if (existingKey != null) {
            apiKeyField.setText(existingKey);
        }

        Label keyStatus = new Label(existingKey != null ? "KEY LOADED" : "NO KEY SET");
        keyStatus.setStyle(keyStatusStyle(existingKey != null));

        Button saveKeyBtn = new Button("Save Key");
        saveKeyBtn.getStyleClass().add("btn-emerald");
        saveKeyBtn.setOnAction(e -> {
            String value = apiKeyField.getText();
            if (value != null && !value.isBlank()) {
                ApiKeyStore.saveApiKey(value);
                keyStatus.setText("KEY LOADED");
                keyStatus.setStyle(keyStatusStyle(true));
            }
        });

        keyRow.getChildren().addAll(apiKeyField, saveKeyBtn, keyStatus);
        apiKeyCard.getChildren().addAll(
            createRowLabel("Paste the key from your Fireworks AI dashboard"),
            keyRow
        );

        // Section 1: Appearance (three-theme version)
        VBox appCard = createSettingCard("Appearance");
        HBox pillContainer = new HBox(8);
        Button draculaPill = new Button("Dracula Theme");
        Button nordPill = new Button("Nord Dark");
        Button primerPill = new Button("Primer Dark");

        List<Button> themePills = List.of(draculaPill, nordPill, primerPill);

        String savedTheme = com.byteanarchists.codeguard.api.ThemePreferenceStore.resolveThemeName();
        Button initiallyActivePill = switch (savedTheme) {
            case com.byteanarchists.codeguard.api.ThemePreferenceStore.NORD_DARK -> nordPill;
            case com.byteanarchists.codeguard.api.ThemePreferenceStore.PRIMER_DARK -> primerPill;
            default -> draculaPill;
        };
        initiallyActivePill.getStyleClass().add("btn-emerald");

        draculaPill.setOnAction(e -> {
            applyTheme(new Dracula().getUserAgentStylesheet(), draculaPill, themePills);
            com.byteanarchists.codeguard.api.ThemePreferenceStore.saveTheme(
                com.byteanarchists.codeguard.api.ThemePreferenceStore.DRACULA);
        });
        nordPill.setOnAction(e -> {
            applyTheme(new NordDark().getUserAgentStylesheet(), nordPill, themePills);
            com.byteanarchists.codeguard.api.ThemePreferenceStore.saveTheme(
                com.byteanarchists.codeguard.api.ThemePreferenceStore.NORD_DARK);
        });
        primerPill.setOnAction(e -> {
            applyTheme(new PrimerDark().getUserAgentStylesheet(), primerPill, themePills);
            com.byteanarchists.codeguard.api.ThemePreferenceStore.saveTheme(
                com.byteanarchists.codeguard.api.ThemePreferenceStore.PRIMER_DARK);
        });

        pillContainer.getChildren().addAll(draculaPill, nordPill, primerPill);
        appCard.getChildren().addAll(createRowLabel("Interface Color Space Matrix"), pillContainer);

        // Section 2: Model Configuration – with callback
        VBox modelCard = createSettingCard("AI Core Inference Engine");
        HBox connectionRow = new HBox(10);
        connectionRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> models = new ComboBox<>();
        models.getItems().addAll("kimi-k2p7-code", "deepseek-v4-pro");
        models.setValue(ModelPreferenceStore.resolveModelShortName());

        models.setOnAction(e -> {
            String selected = models.getValue();
            if (selected != null) {
                ModelPreferenceStore.saveModel(selected);
                // Notify MainView to update the label
                if (onModelChangedCallback != null) {
                    onModelChangedCallback.run();
                }
            }
        });

        Label liveBadge = new Label("CONNECTED");
        liveBadge.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2px 6px; -fx-background-radius: 4px;");

        connectionRow.getChildren().addAll(models, liveBadge);
        modelCard.getChildren().addAll(createRowLabel("Active Target Remote LLM"), connectionRow);

        // Section 3: Runtime Directives – auto‑save
        VBox featuresCard = createSettingCard("Runtime Scanner Directives");
        CheckBox patchFlag = new CheckBox("Auto-save after applying a patch repair");
        patchFlag.setSelected(SettingsStore.isAutoSaveEnabled());
        patchFlag.selectedProperty().addListener((obs, oldVal, newVal) ->
            SettingsStore.setAutoSaveEnabled(newVal)
        );
        featuresCard.getChildren().addAll(patchFlag);

        // Section 4: Hackathon Footer
        VBox buildFooter = new VBox(4);
        buildFooter.setPadding(new Insets(20, 0, 0, 0));
        Label appName = new Label("CodeGuard AI Studio v1.0.0-beta");
        appName.setStyle("-fx-text-fill: #f8f8f2; -fx-font-weight: bold;");
        Label teamSignature = new Label("Engineered by The Witch Cult squad for AMD Developer Hackathon Act II");
        teamSignature.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 11px;");
        buildFooter.getChildren().addAll(appName, teamSignature);

        rootBox.getChildren().addAll(apiKeyCard, appCard, modelCard, featuresCard, buildFooter);
    }

    // <-- NEW: setter for the callback
    public void setOnModelChanged(Runnable callback) {
        this.onModelChangedCallback = callback;
    }

    private String keyStatusStyle(boolean loaded) {
        String bg = loaded ? "#50fa7b" : "#ff5555";
        return "-fx-background-color: " + bg + "; -fx-text-fill: #282a36; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2px 6px; -fx-background-radius: 4px;";
    }

    private void applyTheme(String stylesheet, Button selected, List<Button> allPills) {
        Application.setUserAgentStylesheet(stylesheet);
        for (Button pill : allPills) {
            pill.getStyleClass().remove("btn-emerald");
        }
        selected.getStyleClass().add("btn-emerald");
    }

    private VBox createSettingCard(String title) {
        VBox card = new VBox(12);
        card.getStyleClass().add("bordered-card");
        Label cardTitle = new Label(title);
        cardTitle.setStyle("-fx-text-fill: #50fa7b; -fx-font-weight: bold; -fx-font-size: 13px;");
        card.getChildren().add(cardTitle);
        return card;
    }

    private Label createRowLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");
        return l;
    }
}
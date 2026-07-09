package com.byteanarchists.codeguard.ui;

import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.PrimerLight;
import com.byteanarchists.codeguard.api.ApiKeyStore;
import com.byteanarchists.codeguard.api.ModelPreferenceStore;
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

    // Persistent runtime state variables backing placeholder checkboxes
    private static boolean autoSaveEnabled = true;
    private static boolean lineHighlightEnabled = true;

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

        // Section 1: Appearance Options (Simplified precisely to Dark and Light choices)
        VBox appCard = createSettingCard("Appearance");
        HBox pillContainer = new HBox(8);
        Button darkPill = new Button("Dark Theme");
        Button lightPill = new Button("Light Theme");
        List<Button> themePills = List.of(darkPill, lightPill);

        darkPill.getStyleClass().add("btn-emerald"); // Default visual scope active state

        darkPill.setOnAction(e -> {
            Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());
            if (getScene() != null && getScene().getRoot() != null) {
                getScene().getRoot().getStyleClass().remove("theme-light");
                getScene().getRoot().getStyleClass().add("theme-dark");
            }
            applyPillSelection(darkPill, themePills);
        });

        lightPill.setOnAction(e -> {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            if (getScene() != null && getScene().getRoot() != null) {
                getScene().getRoot().getStyleClass().remove("theme-dark");
                getScene().getRoot().getStyleClass().add("theme-light");
            }
            applyPillSelection(lightPill, themePills);
        });

        pillContainer.getChildren().addAll(darkPill, lightPill);
        appCard.getChildren().addAll(createRowLabel("Interface Color Space Matrix"), pillContainer);

        // Section 2: Model Configuration
        VBox modelCard = createSettingCard("AI Core Inference Engine");
        HBox connectionRow = new HBox(10);
        connectionRow.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> models = new ComboBox<>();
        models.getItems().addAll("kimi-k2p7-code", "deepseek-v4-pro");
        models.setValue(ModelPreferenceStore.resolveModelShortName());
        
        Label liveBadge = new Label("CONNECTED");
        liveBadge.setStyle("-fx-background-color: #50fa7b; -fx-text-fill: #282a36; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2px 6px; -fx-background-radius: 4px;");
        
        models.setOnAction(e -> ModelPreferenceStore.saveModel(models.getValue()));
        connectionRow.getChildren().addAll(models, liveBadge);
        modelCard.getChildren().addAll(createRowLabel("Active Target Remote LLM"), connectionRow);

        // Section 3: Active Runtime Directives (Functional Selection Bindings)
        VBox featuresCard = createSettingCard("Runtime Scanner Directives");
        CheckBox patchFlag = new CheckBox("Auto-save after applying a patch repair");
        patchFlag.setSelected(autoSaveEnabled);
        patchFlag.selectedProperty().addListener((obs, oldVal, newVal) -> autoSaveEnabled = newVal);

       // CheckBox lineHighlightFlag = new CheckBox("Highlight structural vulnerable source frames");
        //lineHighlightFlag.setSelected(lineHighlightEnabled);
        //lineHighlightFlag.selectedProperty().addListener((obs, oldVal, newVal) -> lineHighlightEnabled = newVal);

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


    public static boolean isAutoSaveEnabled() { return autoSaveEnabled; }
    public static boolean isLineHighlightEnabled() { return lineHighlightEnabled; }

    private String keyStatusStyle(boolean loaded) {
        String bg = loaded ? "#50fa7b" : "#ff5555";
        return "-fx-background-color: " + bg + "; -fx-text-fill: #282a36; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2px 6px; -fx-background-radius: 4px;";
    }

    private void applyPillSelection(Button selected, List<Button> allPills) {
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
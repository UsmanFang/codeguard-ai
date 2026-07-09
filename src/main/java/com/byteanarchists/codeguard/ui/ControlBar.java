package com.byteanarchists.codeguard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import com.byteanarchists.codeguard.api.ModelPreferenceStore;

public class ControlBar extends HBox {

    private final Label engineLabel;

    public ControlBar(Runnable openAction, Runnable scanAction, Runnable saveAction, Runnable closeAction) {
        // Enforcing core strategic layout cushions requested by the review
        setPadding(new Insets(10, 14, 10, 14));
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #1e1f29; -fx-border-color: #44475a; -fx-border-width: 0 0 1px 0;");

        Button openBtn = new Button("Open Target File");
        Button saveBtn = new Button("Save");
        Button closeBtn = new Button("✕ Close file");
        closeBtn.setStyle("-fx-text-fill: #ff5555; -fx-background-color: transparent;");

        // Repositioned, aligned and specifically named CTA Security Trigger
        Button scanBtn = new Button("Initiate Security Scan");
        scanBtn.getStyleClass().add("btn-emerald");

        // Spacer element pushing system indicators to the right edge
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Dynamically backed core configuration target label
        engineLabel = new Label();
        engineLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-text-fill: #6272a4; -fx-font-size: 12px;");
        refreshModelLabel();

        openBtn.setOnAction(e -> openAction.run());
        scanBtn.setOnAction(e -> scanAction.run());
        saveBtn.setOnAction(e -> saveAction.run());
        closeBtn.setOnAction(e -> closeAction.run());

        // Assembly order placement preserving design rules
        getChildren().addAll(openBtn, scanBtn, saveBtn, closeBtn, spacer, engineLabel);
    }

    /**
     * Resolves and updates the current active engine tracking text label immediately.
     */
    public final void refreshModelLabel() {
        String activeModel = ModelPreferenceStore.resolveModelShortName();
        engineLabel.setText("Fireworks AI [" + activeModel + "]  ⚙");
    }
}
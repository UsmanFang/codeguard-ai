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

    public ControlBar(Runnable openAction, Runnable scanAction, Runnable saveAction, Runnable saveAsAction, Runnable closeAction) {
        // Layout styling
        setPadding(new Insets(10, 14, 10, 14));
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #1e1f29; -fx-border-color: #44475a; -fx-border-width: 0 0 1px 0;");

        // Buttons
        Button openBtn = new Button("Open Target File");
        Button saveBtn = new Button("Save");
        Button saveAsBtn = new Button("Save As...");
        Button closeBtn = new Button("✕ Close file");
        closeBtn.setStyle("-fx-text-fill: #ff5555; -fx-background-color: transparent;");

        // Scan button (styled CTA)
        Button scanBtn = new Button("Initiate Security Scan");
        scanBtn.getStyleClass().add("btn-emerald");

        // Spacer to push engine label to the right
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Engine label (from friend's side – needed for the addAll below)
        engineLabel = new Label();
        engineLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-text-fill: #6272a4; -fx-font-size: 12px;");
        refreshModelLabel();

        // Wire actions
        openBtn.setOnAction(e -> openAction.run());
        scanBtn.setOnAction(e -> scanAction.run());
        saveBtn.setOnAction(e -> saveAction.run());
        saveAsBtn.setOnAction(e -> saveAsAction.run());
        closeBtn.setOnAction(e -> closeAction.run());

        // Add all components in order
        getChildren().addAll(openBtn, scanBtn, saveBtn, saveAsBtn, closeBtn, spacer, engineLabel);
    }

    /**
     * Updates the engine label to show the currently selected model.
     */
    public final void refreshModelLabel() {
        String activeModel = ModelPreferenceStore.resolveModelShortName();
        engineLabel.setText("Fireworks AI [" + activeModel + "]  ⚙");
    }
}
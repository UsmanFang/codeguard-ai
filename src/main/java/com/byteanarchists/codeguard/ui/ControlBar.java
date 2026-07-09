// "Open File" + "Scan with AI Model" buttons (HBox)
package com.byteanarchists.codeguard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import com.byteanarchists.codeguard.api.ModelPreferenceStore;

public class ControlBar extends HBox {

    public ControlBar(Runnable openAction, Runnable scanAction, Runnable saveAction, Runnable saveAsAction, Runnable closeAction) {
    // Enforcing core strategic layout cushions requested by the review
    setPadding(new Insets(10, 14, 10, 14));
    setSpacing(10);
    setAlignment(Pos.CENTER_LEFT);
    setStyle("-fx-background-color: #1e1f29; -fx-border-color: #44475a; -fx-border-width: 0 0 1px 0;");

    Button openBtn = new Button("Open Target File");

    Button scanBtn = new Button("Initiate Security Scan");
    scanBtn.getStyleClass().add("btn-emerald"); // Explicit functional color mappings

    Button saveBtn = new Button("Save");

    // "Save As" lets the user pick a new destination file instead of always
    // overwriting the currently-open one. Plain Save still overwrites in place.
    Button saveAsBtn = new Button("Save As...");

    Button closeBtn = new Button("✕ Close file");
    closeBtn.setStyle("-fx-text-fill: #ff5555; -fx-background-color: transparent;");


    // Spacer element pushing system indicators to the right edge
    HBox spacer = new HBox();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // PREVIOUSLY hardcoded to "llama3-70b" regardless of which model was actually
    // configured/active - now reflects ModelPreferenceStore, same source of truth
    // FireworksScannerImpl reads from.
    Label engineLabel = new Label("Fireworks AI [" + ModelPreferenceStore.resolveModelShortName() + "]  ⚙");
    engineLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-text-fill: #6272a4; -fx-font-size: 12px;");

    openBtn.setOnAction(e -> openAction.run());
    scanBtn.setOnAction(e -> scanAction.run());
    saveBtn.setOnAction(e -> saveAction.run());
    saveAsBtn.setOnAction(e -> saveAsAction.run());
    closeBtn.setOnAction(e -> closeAction.run());

    getChildren().addAll(openBtn, scanBtn, saveBtn, saveAsBtn, closeBtn, spacer, engineLabel);
}
}
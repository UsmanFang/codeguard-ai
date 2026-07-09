// reusable card, now with Copy fix + Dismiss + Auto‑save
package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.Finding;
import com.byteanarchists.codeguard.util.SettingsStore;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class FindingCard extends VBox {
    private Runnable saveAction; // <-- new field

    public FindingCard(Finding finding, CodeEditorPanel editorPanel) {
        getStyleClass().add("bordered-card");
        setSpacing(10);

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        String color = finding.getSeverity().equalsIgnoreCase("CRITICAL") ? "#ff5555" :
                       finding.getSeverity().equalsIgnoreCase("HIGH") ? "#ffb86c" : "#8be9fd";

        Label badge = new Label(finding.getSeverity() + " · LINE " + finding.getLineNumber());
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #282a36; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 4px; -fx-font-size: 11px;");

        Label titleLabel = new Label(finding.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8f8f2; -fx-font-size: 14px;");

        headerRow.getChildren().addAll(badge, titleLabel);
        headerRow.setOnMouseClicked(e -> editorPanel.highlightVulnerableLine(finding.getLineNumber()));
        headerRow.setStyle("-fx-cursor: hand;");

        Label description = new Label(finding.getDescription());
        description.setWrapText(true);
        description.setStyle("-fx-text-fill: #f8f8f2; -fx-font-size: 13px;");

        getChildren().addAll(headerRow, description);

        if (finding.getFixSnippet() != null && !finding.getFixSnippet().isEmpty()) {
            TextArea codeBlock = new TextArea(finding.getFixSnippet());
            codeBlock.setEditable(false);
            codeBlock.setPrefHeight(65);
            codeBlock.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-control-inner-background: #282a36; -fx-text-fill: #50fa7b; -fx-border-color: #44475a; -fx-border-radius: 4px;");

            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_RIGHT);
            Button copyBtn = new Button("Apply Patch");
            copyBtn.setStyle("-fx-background-color: #44475a; -fx-text-fill: #f8f8f2;");
            copyBtn.setOnAction(e -> {
                // Apply the fix
                editorPanel.applyFixAtLine(finding.getLineNumber(), finding.getFixSnippet());
                copyBtn.setText("Applied ✓");
                copyBtn.setDisable(true);

                // Auto‑save if enabled
                if (SettingsStore.isAutoSaveEnabled() && saveAction != null) {
                    saveAction.run();
                }
            });

            Button dismissBtn = new Button("Dismiss");
            dismissBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6272a4;");
            dismissBtn.setOnAction(e -> {
                if (getParent() instanceof VBox parentBox) {
                    parentBox.getChildren().remove(this);
                }
            });

            actions.getChildren().addAll(dismissBtn, copyBtn);
            getChildren().addAll(codeBlock, actions);
        }
    }

    // <-- new setter
    public void setSaveAction(Runnable saveAction) {
        this.saveAction = saveAction;
    }
}
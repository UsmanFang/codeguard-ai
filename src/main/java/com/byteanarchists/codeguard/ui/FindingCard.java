package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.Finding;
import com.byteanarchists.codeguard.util.SettingsStore;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FindingCard extends VBox {
    private final Button applyBtn;
    private final Button dismissBtn;

    public FindingCard(Finding finding, CodeEditorPanel editorPanel, int lineNumber,
                       BiConsumer<Integer, Boolean> onApplyDismiss, Consumer<Integer> onDismiss,
                       boolean initiallyApplied, boolean initiallyDismissed,
                       Runnable saveAction) { // <-- new parameter
        getStyleClass().add("bordered-card");
        setSpacing(10);

        applyBtn = new Button("Apply Patch");
        dismissBtn = new Button("Dismiss");

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        String color = finding.getSeverity().equalsIgnoreCase("CRITICAL") ? "#ff5555" :
                       finding.getSeverity().equalsIgnoreCase("HIGH") ? "#ffb86c" : "#8be9fd";

        Label badge = new Label(finding.getSeverity() + " · LINE " + lineNumber);
        badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #282a36; -fx-font-weight: bold; -fx-padding: 2px 6px; -fx-background-radius: 4px; -fx-font-size: 11px;");

        Label titleLabel = new Label(finding.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f8f8f2; -fx-font-size: 14px;");

        headerRow.getChildren().addAll(badge, titleLabel);
        headerRow.setOnMouseClicked(e -> editorPanel.highlightVulnerableLine(lineNumber));
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

            applyBtn.setStyle("-fx-background-color: #44475a; -fx-text-fill: #f8f8f2;");
            applyBtn.setOnAction(e -> {
                // 1. Apply the fix to the editor
                editorPanel.applyFixAtLine(lineNumber, finding.getFixSnippet());
                applyBtn.setText("Applied ✓");
                applyBtn.setDisable(true);

                // 2. Update the review state (applied lines)
                if (onApplyDismiss != null) {
                    onApplyDismiss.accept(lineNumber, true);
                }

                // 3. Auto‑save ONLY if the checkbox is checked and we have a save action
                if (SettingsStore.isAutoSaveEnabled() && saveAction != null) {
                    saveAction.run(); // triggers handleSaveFile() in MainView
                }
            });

            dismissBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6272a4;");
            dismissBtn.setOnAction(e -> {
                if (getParent() instanceof VBox parentBox) {
                    parentBox.getChildren().remove(this);
                }
                if (onDismiss != null) {
                    onDismiss.accept(lineNumber);
                }
                if (onApplyDismiss != null) {
                    onApplyDismiss.accept(lineNumber, false);
                }
            });

            if (initiallyApplied) {
                applyBtn.setText("Applied ✓");
                applyBtn.setDisable(true);
            }
            if (initiallyDismissed) {
                this.setVisible(false);
                this.setManaged(false);
            }

            actions.getChildren().addAll(dismissBtn, applyBtn);
            getChildren().addAll(codeBlock, actions);
        } else {
            Label noFix = new Label("No fix snippet available.");
            noFix.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");
            getChildren().add(noFix);
            applyBtn.setVisible(false);
            dismissBtn.setVisible(false);
        }
    }
}
package com.byteanarchists.codeguard.ui;

import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.beans.property.StringProperty;

public class CodeEditorPanel extends StackPane {
    private final TextArea codeArea;

    // Define Soft-Studio Light Theme
    private static final String LIGHT_THEME = 
        "-fx-font-family: 'JetBrains Mono', 'Courier New', monospace; " +
        "-fx-font-size: 13px; " +
        "-fx-text-fill: #2D3436; " + 
        "-fx-control-inner-background: #FFFFFF; " + 
        "-fx-background-color: #F4F5F7; " +
        "-fx-border-color: #DFE4EA; " + 
        "-fx-border-width: 1px;";

    // Define Standard Dracula Dark Theme
    private static final String DARK_THEME = 
        "-fx-font-family: 'JetBrains Mono', 'Courier New', monospace; " +
        "-fx-font-size: 13px; " +
        "-fx-text-fill: #f8f8f2; " +
        "-fx-control-inner-background: #282a36; " +
        "-fx-background-color: transparent; " +
        "-fx-focus-color: transparent; " +
        "-fx-text-box-border: transparent;";

    public CodeEditorPanel() {
        getStyleClass().add("code-panel");
        codeArea = new TextArea();
        codeArea.setStyle(DARK_THEME); // Default to dark
        codeArea.setPromptText("// Load target system files to trigger operational checks...");
        getChildren().add(codeArea);
    }

    // Dynamic Theme Toggle
    public void applyTheme(boolean isLightMode) {
        codeArea.setStyle(isLightMode ? LIGHT_THEME : DARK_THEME);
    }

    public StringProperty textProperty() {
        return codeArea.textProperty();
    }

    public void setCodeContent(String text) {
        codeArea.setText(text);
    }

    public String getCodeContent() {
        return codeArea.getText();
    }

    public void clearHighlights() {
        this.getStyleClass().remove("vulnerable-line-highlight");
    }

    public void highlightVulnerableLine(int targetLine) {
        int[] range = lineRange(targetLine);
        if (range == null) return;
        codeArea.requestFocus();
        codeArea.selectRange(range[0], range[1]);
    }

    public void applyFixAtLine(int targetLine, String fixSnippet) {
        int[] range = lineRange(targetLine);
        if (range == null) return;
        codeArea.selectRange(range[0], range[1]);
        codeArea.replaceSelection(fixSnippet);
    }

    private int[] lineRange(int targetLine) {
        String text = codeArea.getText();
        String[] lines = text.split("\n", -1);
        if (targetLine < 1 || targetLine > lines.length) return null;

        int start = 0;
        for (int i = 0; i < targetLine - 1; i++) {
            start += lines[i].length() + 1;
        }
        int end = start + lines[targetLine - 1].length();
        return new int[]{start, end};
    }
}
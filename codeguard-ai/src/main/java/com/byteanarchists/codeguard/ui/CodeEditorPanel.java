// # left panel — editable TextArea (or CodeArea if using
//  RichTextFX)
package com.byteanarchists.codeguard.ui;

import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

public class CodeEditorPanel extends StackPane {
    private final TextArea codeArea;

    public CodeEditorPanel() {
        getStyleClass().add("code-panel");

        codeArea = new TextArea();
        codeArea.setStyle(
            "-fx-font-family: 'JetBrains Mono', 'Courier New', monospace; " +
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #f8f8f2; " +
            "-fx-control-inner-background: #282a36; " +
            "-fx-background-color: transparent; " +
            "-fx-focus-color: transparent; " +
            "-fx-text-box-border: transparent;"
        );
        codeArea.setPromptText("// Load target system files to trigger operational checks...");

        getChildren().add(codeArea);
    }

    public void setCodeContent(String text) {
        codeArea.setText(text);
    }

    public String getCodeContent() {
        return codeArea.getText();
    }

    /**
     * Selects the given 1-based line so the user can visually see where a
     * finding is. TextArea has no per-substring styling API without pulling in
     * RichTextFX, so selection (which JavaFX renders with a highlight color)
     * is the simplest real signal - this replaces the old no-op that only
     * printed to the console and never touched the UI.
     */
    public void highlightVulnerableLine(int targetLine) {
        int[] range = lineRange(targetLine);
        if (range == null) return;
        codeArea.requestFocus();
        codeArea.selectRange(range[0], range[1]);
    }

    /**
     * Replaces the text of the given 1-based line with the AI-suggested fix
     * snippet (which may itself span multiple lines). This is what backs the
     * "Apply Patch" button on a finding card - previously that button existed
     * in the UI but had no onAction handler at all, so clicking it did nothing.
     */
    public void applyFixAtLine(int targetLine, String fixSnippet) {
        int[] range = lineRange(targetLine);
        if (range == null) return;
        codeArea.selectRange(range[0], range[1]);
        codeArea.replaceSelection(fixSnippet);
    }

    /** Returns {startOffset, endOffset} (exclusive of trailing newline) for a 1-based line, or null if out of range. */
    private int[] lineRange(int targetLine) {
        String text = codeArea.getText();
        String[] lines = text.split("\n", -1);
        if (targetLine < 1 || targetLine > lines.length) return null;

        int start = 0;
        for (int i = 0; i < targetLine - 1; i++) {
            start += lines[i].length() + 1; // +1 for the '\n' we split on
        }
        int end = start + lines[targetLine - 1].length();
        return new int[]{start, end};
    }
}
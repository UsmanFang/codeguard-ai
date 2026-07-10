// SplitPane wiring left/right panels
package com.byteanarchists.codeguard.ui;

import javafx.geometry.Insets;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class WorkspaceSplitPane extends StackPane {
    private final CodeEditorPanel editorPanel;
    private final ReportPanel reportPanel;

    public WorkspaceSplitPane() {
        // Enforcing Outer Margin Cushion adjustments from the Visual Audit
        setPadding(new Insets(0, 14, 10, 14));
        setStyle("-fx-background-color: #1e1f29;");

        editorPanel = new CodeEditorPanel();
        reportPanel = new ReportPanel(editorPanel);

        SplitPane splitLayout = new SplitPane();
        splitLayout.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        splitLayout.getItems().addAll(editorPanel, reportPanel);
        splitLayout.setDividerPositions(0.5);

        getChildren().add(splitLayout);
    }

    public CodeEditorPanel getEditorPanel() { return editorPanel; }
    public ReportPanel getReportPanel() { return reportPanel; }
}
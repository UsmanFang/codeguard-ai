//right panel — read-only TextArea
package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.Finding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class ReportPanel extends StackPane {
    private final VBox contentBox;
    private final ScrollPane scrollWrapper;
    private final CodeEditorPanel editorPanel;

    public ReportPanel(CodeEditorPanel editorPanel) {
        this.editorPanel = editorPanel;
        getStyleClass().add("report-panel");

        contentBox = new VBox(12);
        contentBox.setPadding(new Insets(4));

        scrollWrapper = new ScrollPane(contentBox);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scrollWrapper);
        renderFindings(List.of()); // Forces initial operational Empty State trigger layout
    }

    public void renderFindings(List<Finding> findings) {
        contentBox.getChildren().clear();

        // Implementing Review Critique Point: Mid-workspace Active Placeholder State
        if (findings == null || findings.isEmpty()) {
            StackPane basePlaceholder = new StackPane();
            basePlaceholder.setPadding(new Insets(60, 20, 60, 20));
            
            Label message = new Label("Run a scan to generate a vulnerability report…");
            message.getStyleClass().add("placeholder-text");
            
            basePlaceholder.getChildren().add(message);
            contentBox.getChildren().add(basePlaceholder);
        } else {
            for (Finding finding : findings) {
                contentBox.getChildren().add(new FindingCard(finding, editorPanel));
            }
        }
    }
}
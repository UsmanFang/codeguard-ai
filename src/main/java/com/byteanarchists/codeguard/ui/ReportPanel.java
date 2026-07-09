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
import com.byteanarchists.codeguard.util.SettingsStore;

public class ReportPanel extends StackPane {
    private final VBox contentBox;
    private final ScrollPane scrollWrapper;
    private final CodeEditorPanel editorPanel;
    private Runnable saveAction; // <-- new field

    public ReportPanel(CodeEditorPanel editorPanel) {
        this.editorPanel = editorPanel;
        getStyleClass().add("report-panel");

        contentBox = new VBox(12);
        contentBox.setPadding(new Insets(4));

        scrollWrapper = new ScrollPane(contentBox);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scrollWrapper);
        renderFindings(List.of());
    }

    // <-- new setter
    public void setSaveAction(Runnable saveAction) {
        this.saveAction = saveAction;
    }

    public void renderFindings(List<Finding> findings) {
        contentBox.getChildren().clear();

        if (findings == null || findings.isEmpty()) {
            StackPane basePlaceholder = new StackPane();
            basePlaceholder.setPadding(new Insets(60, 20, 60, 20));
            
            Label message = new Label("Run a scan to generate a vulnerability report…");
            message.getStyleClass().add("placeholder-text");
            
            basePlaceholder.getChildren().add(message);
            contentBox.getChildren().add(basePlaceholder);
        } else {
            for (Finding finding : findings) {
                FindingCard card = new FindingCard(finding, editorPanel);
                card.setSaveAction(saveAction); // <-- pass the save action down
                contentBox.getChildren().add(card);
            }
        }
    }
}
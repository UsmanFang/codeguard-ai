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
        if (findings == null || findings.isEmpty()) {
            contentBox.getChildren().add(new Label("No vulnerabilities found."));
            return;
        }

        // Staggered card appearance
        int delay = 0;
        for (Finding finding : findings) {
            FindingCard card = new FindingCard(finding, this::applyFix, this::dismissFinding);
            card.setOpacity(0);
            contentBox.getChildren().add(card);

            PauseTransition pause = new PauseTransition(Duration.millis(delay));
            pause.setOnFinished(e -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            pause.play();

            delay += 150; // 150ms between each card
        }
    }

}
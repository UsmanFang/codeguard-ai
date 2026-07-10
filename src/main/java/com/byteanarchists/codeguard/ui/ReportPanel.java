package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.Finding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.BiConsumer;

public class ReportPanel extends StackPane {
    private final VBox contentBox;
    private final ScrollPane scrollWrapper;
    private final CodeEditorPanel editorPanel;
    private Runnable saveAction; // <-- holds the save action from MainView

    public ReportPanel(CodeEditorPanel editorPanel) {
        this.editorPanel = editorPanel;
        getStyleClass().add("report-panel");

        contentBox = new VBox(12);
        contentBox.setPadding(new Insets(4));

        scrollWrapper = new ScrollPane(contentBox);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scrollWrapper);
        renderFindings(List.of(), null, List.of(), List.of());
    }

    public void setSaveAction(Runnable saveAction) {
        this.saveAction = saveAction;
    }

    public void renderFindings(List<Finding> findings, BiConsumer<Integer, Boolean> onApplyDismiss,
                               List<Integer> appliedLines, List<Integer> dismissedLines) {
        contentBox.getChildren().clear();

        if (findings == null || findings.isEmpty()) {
            StackPane basePlaceholder = new StackPane();
            basePlaceholder.setPadding(new Insets(60, 20, 60, 20));
            Label message = new Label("Run a scan to generate a vulnerability report…");
            message.getStyleClass().add("placeholder-text");
            basePlaceholder.getChildren().add(message);
            contentBox.getChildren().add(basePlaceholder);
            return;
        }

        for (Finding finding : findings) {
            int line = finding.getLineNumber();
            boolean isApplied = appliedLines != null && appliedLines.contains(line);
            boolean isDismissed = dismissedLines != null && dismissedLines.contains(line);
            FindingCard card = new FindingCard(
                finding,
                editorPanel,
                line,
                onApplyDismiss,
                null,
                isApplied,
                isDismissed,
                this.saveAction // <-- pass the save action down
            );
            contentBox.getChildren().add(card);
        }
    }

    public void renderFindings(List<Finding> findings) {
        renderFindings(findings, null, List.of(), List.of());
    }

    public void renderFindings(List<Finding> findings, BiConsumer<Integer, Boolean> onApplyDismiss) {
        renderFindings(findings, onApplyDismiss, List.of(), List.of());
    }

    public void clear() {
        contentBox.getChildren().clear();
    }
}
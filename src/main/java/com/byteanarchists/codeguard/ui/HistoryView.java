// screen 4, past scans list
package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.ScanRecord;
import com.byteanarchists.codeguard.io.HistoryService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class HistoryView extends ScrollPane {
    private final VBox container;
    private final HistoryService historyService;

    public HistoryView() {
        historyService = new HistoryService();

        setFitToWidth(true);
        setStyle("-fx-background: #282a36; -fx-background-color: #282a36;");
        
        container = new VBox(12);
        container.setPadding(new Insets(24));
        setContent(container);

        Label viewTitle = new Label("Vulnerability Scan Logs");
        viewTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2; -fx-padding: 0 0 10 0;");
        container.getChildren().add(viewTitle);

        // ********* CHANGE HERE *********
        // Replace mockHistory with real history from HistoryService
        List<ScanRecord> realHistory = historyService.loadHistory();

        if (realHistory.isEmpty()) {
            Label emptyLabel = new Label("No scans recorded yet.");
            emptyLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 14px;");
            container.getChildren().add(emptyLabel);
        } else {
            for (ScanRecord record : realHistory) {
                container.getChildren().add(createHistoryCard(record));
            }
        }
    }

    private HBox createHistoryCard(ScanRecord record) {
        HBox card = new HBox(14);
        card.getStyleClass().add("bordered-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label logIcon = new Label("📊");
        
        Label targetName = new Label(record.getFilename());
        targetName.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-text-fill: #f8f8f2; -fx-font-weight: bold;");

        HBox badgeContainer = new HBox(6);
        for (String sev : record.getSeverities()) {
            Label badge = new Label(sev);
            String color = switch (sev) {
                case "CRITICAL" -> "#ff5555";
                case "HIGH" -> "#ffb86c";
                default -> "#8be9fd"; // INFO and anything else
            };
            badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #282a36; -fx-font-size: 9px; -fx-font-weight:bold; -fx-padding: 1px 4px; -fx-background-radius: 3px;");
            badgeContainer.getChildren().add(badge);
        }

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(record.getTimestamp());
        timeLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");

        card.getChildren().addAll(logIcon, targetName, badgeContainer, spacer, timeLabel);
        return card;
    }

    
    public void refresh() {
        // Clear all children except the title
        container.getChildren().clear();
        
        // Re-add title
        Label viewTitle = new Label("Vulnerability Scan Logs");
        viewTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2; -fx-padding: 0 0 10 0;");
        container.getChildren().add(viewTitle);

        // Load fresh history
        List<ScanRecord> realHistory = historyService.loadHistory();
        if (realHistory.isEmpty()) {
            Label emptyLabel = new Label("No scans recorded yet.");
            emptyLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 14px;");
            container.getChildren().add(emptyLabel);
        } else {
            for (ScanRecord record : realHistory) {
                container.getChildren().add(createHistoryCard(record));
            }
        }
    }
}
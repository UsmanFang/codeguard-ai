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
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import java.time.LocalDateTime;

import java.util.List;
import java.util.function.Consumer;

public class HistoryView extends ScrollPane {
    private final VBox container;
    private final HistoryService historyService;
    // Called with the absolute file path when the user clicks a history card
    // that has one, so MainView can reload that file into the editor.
    private final Consumer<String> onOpenFile;

    public HistoryView(Consumer<String> onOpenFile) {
        this.onOpenFile = onOpenFile;
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

        // Human-friendly relative time ("2h ago", "Yesterday", ...) instead of
        // the raw LocalDateTime.toString() that was stored.
        Label timeLabel = new Label(getRelativeTime(record.getTimestamp()));
        timeLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");

        card.getChildren().addAll(logIcon, targetName, badgeContainer, spacer, timeLabel);

        // Click-to-reopen: only wired up if this record has a saved file path
        // (older history entries recorded before this field existed will have
        // filePath == null, and just aren't clickable).
        String path = record.getFilePath();
        if (path != null && !path.isBlank()) {
            card.setStyle("-fx-cursor: hand;");
            card.setOnMouseClicked(e -> onOpenFile.accept(path));
        }

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

    private String getRelativeTime(String timestamp) {
        try {
            LocalDateTime past = LocalDateTime.parse(timestamp);
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(past, now);
            long seconds = duration.getSeconds();
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            if (seconds < 60) return "Just now";
            if (minutes < 60) return minutes + "m ago";
            if (hours < 24) return hours + "h ago";
            if (days < 2) return "Yesterday";
            if (days < 7) return days + "d ago";
            return past.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        } catch (Exception e) {
            return timestamp;
        }
    }
}
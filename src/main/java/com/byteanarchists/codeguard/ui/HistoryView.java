package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.ScanRecord;
import com.byteanarchists.codeguard.io.HistoryService;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration; // for FadeTransition

import java.time.LocalDateTime;
import java.time.Duration; // for getRelativeTime
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class HistoryView extends ScrollPane {
    private final VBox container;
    private final HistoryService historyService;
    private final Consumer<String> onOpenFile;          // for reopening files
    private Consumer<ScanRecord> onRecordSelectedCallback; // optional extra callback

    public HistoryView(Consumer<String> onOpenFile) {
        this.onOpenFile = onOpenFile;
        this.onRecordSelectedCallback = null;
        historyService = new HistoryService();

        setFitToWidth(true);
        setStyle("-fx-background: #282a36; -fx-background-color: #282a36;");
        
        container = new VBox(12);
        container.setPadding(new Insets(24));
        setContent(container);

        renderLogsContent();
    }

    public void setOnRecordSelected(Consumer<ScanRecord> callback) {
        this.onRecordSelectedCallback = callback;
    }

    private void renderLogsContent() {
        Label viewTitle = new Label("Recent Workplace Audits (Dynamic History Feed)");
        viewTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2; -fx-padding: 0 0 10 0;");
        container.getChildren().add(viewTitle);

        List<ScanRecord> realHistory = historyService.loadHistory();

        if (realHistory.isEmpty()) {
            Label emptyLabel = new Label("No historical audits found. Core file database empty.");
            emptyLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 14px;");
            container.getChildren().add(emptyLabel);
        } else {
            int staggerOffsetIndex = 0;
            for (ScanRecord record : realHistory) {
                HBox card = createHistoryCard(record);
                container.getChildren().add(card);
                applyStaggeredFadeInAnimation(card, staggerOffsetIndex++);
            }
        }
    }

    private HBox createHistoryCard(ScanRecord record) {
        HBox card = new HBox(14);
        card.getStyleClass().add("bordered-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Click handler: open file if path exists, and also invoke optional callback
        String path = record.getFilePath();
        if (path != null && !path.isBlank()) {
            card.setStyle("-fx-cursor: hand;");
            card.setOnMouseClicked(e -> {
                onOpenFile.accept(path);
                if (onRecordSelectedCallback != null) {
                    onRecordSelectedCallback.accept(record);
                }
            });
        } else {
            // No file path – still allow the callback if set, but no file reopen
            card.setOnMouseClicked(e -> {
                if (onRecordSelectedCallback != null) {
                    onRecordSelectedCallback.accept(record);
                }
            });
        }

        Label logIcon = new Label("📑");
        Label targetName = new Label(record.getFilename());
        targetName.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-text-fill: #f8f8f2; -fx-font-weight: bold;");

        HBox badgeContainer = new HBox(6);
        for (String sev : record.getSeverities()) {
            Label badge = new Label(sev);
            String color = switch (sev) {
                case "CRITICAL" -> "#ff5555";
                case "HIGH" -> "#ffb86c";
                default -> "#8be9fd";
            };
            badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: #282a36; -fx-font-size: 9px; -fx-font-weight:bold; -fx-padding: 1px 4px; -fx-background-radius: 3px;");
            badgeContainer.getChildren().add(badge);
        }

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Human‑friendly relative time
        Label timeLabel = new Label(getRelativeTime(record.getTimestamp()));
        timeLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");

        card.getChildren().addAll(logIcon, targetName, badgeContainer, spacer, timeLabel);
        return card;
    }

    private void applyStaggeredFadeInAnimation(Node element, int index) {
        element.setOpacity(0.0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), element);
        fadeIn.setToValue(1.0);
        fadeIn.setDelay(Duration.millis(index * 65));
        fadeIn.play();
    }

    public void refresh() {
        container.getChildren().clear();
        renderLogsContent();
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
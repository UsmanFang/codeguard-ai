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
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class HistoryView extends ScrollPane {
    private final VBox container;
    private final HistoryService historyService;
    private final Consumer<ScanRecord> onRecordSelected;

    public HistoryView(Consumer<ScanRecord> onRecordSelected) {
        this.onRecordSelected = onRecordSelected;
        this.historyService = new HistoryService();

        setFitToWidth(true);
        setStyle("-fx-background: #282a36; -fx-background-color: #282a36;");

        container = new VBox(12);
        container.setPadding(new Insets(24));
        setContent(container);

        renderLogsContent();
    }

    private void renderLogsContent() {
        container.getChildren().clear();

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
        card.setStyle("-fx-cursor: hand;");

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

        // Formatted timestamp with update count
        String formattedTime = formatTimestamp(record.getTimestamp());
        String updateText = "";
        if (record.getUpdateCount() > 0) {
            updateText = " · Updated (" + record.getUpdateCount() + ")";
        }
        Label timeLabel = new Label(formattedTime + updateText);
        timeLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");

        card.getChildren().addAll(logIcon, targetName, badgeContainer, spacer, timeLabel);

        // Click → pass full record to MainView for restoration
        card.setOnMouseClicked(e -> {
            if (onRecordSelected != null) {
                onRecordSelected.accept(record);
            }
        });

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

    private String formatTimestamp(String timestamp) {
        try {
            LocalDateTime dt = LocalDateTime.parse(timestamp);
            LocalDateTime now = LocalDateTime.now();
            if (dt.toLocalDate().equals(now.toLocalDate())) {
                return "Today · " + dt.format(DateTimeFormatter.ofPattern("h:mm a"));
            } else {
                return dt.format(DateTimeFormatter.ofPattern("MMM d · h:mm a"));
            }
        } catch (Exception e) {
            return timestamp;
        }
    }
}
package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.ScanRecord;
import com.byteanarchists.codeguard.io.HistoryService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class EmptyStateView extends VBox {
    private final Runnable onOpenFileAction;
    private final HistoryService historyService = new HistoryService();
    private final VBox recentAuditsContainer;
    private final Label primaryMessageLabel;
    private final Label secondaryMessageLabel;

    public EmptyStateView(Runnable onOpenFileAction) {
        this.onOpenFileAction = onOpenFileAction;
        
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: transparent;");
        setPadding(new Insets(40));

// Shield / Security Icon Container
        VBox iconCircle = new VBox();
        iconCircle.setAlignment(Pos.CENTER);
        iconCircle.setPrefSize(80, 80);
        iconCircle.setMaxSize(80, 80);
        iconCircle.setStyle("-fx-background-color: rgba(80, 250, 123, 0.1); -fx-background-radius: 40; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-border-radius: 40;");
        
        // FIX: Using a clean, universally compatible shield vector character 
        // to prevent OS font engine rendering split boxes.
        Label shieldIcon = new Label("⛨"); 
        shieldIcon.setStyle("-fx-font-size: 32px; -fx-text-fill: #50fa7b; -fx-font-weight: bold;");
        iconCircle.getChildren().add(shieldIcon);

        // Dynamic Main Text Header
        primaryMessageLabel = new Label();
        primaryMessageLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        // Dynamic Subtitle Text
        secondaryMessageLabel = new Label();
        secondaryMessageLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 14px;");

        // Open File Action Call-To-Action (CTA)
        Button openFileBtn = new Button("Open Target Source File");
        openFileBtn.getStyleClass().add("btn-emerald");
        openFileBtn.setPrefHeight(40);
        openFileBtn.setPadding(new Insets(0, 24, 0, 24));
        openFileBtn.setOnAction(e -> this.onOpenFileAction.run());

        // Dynamic Recent Workplace Audits Section
        recentAuditsContainer = new VBox(10);
        recentAuditsContainer.setMaxWidth(500);
        recentAuditsContainer.setAlignment(Pos.TOP_LEFT);
        recentAuditsContainer.setPadding(new Insets(20, 0, 0, 0));

        getChildren().addAll(iconCircle, primaryMessageLabel, secondaryMessageLabel, openFileBtn, recentAuditsContainer);
        
        // Populate view components with real state tracking variables
        refreshState();
    }

    /**
     * Re-reads the database logs to dynamically display the real 
     * workspace status without hardcoded strings or fake list entries.
     */
    public final void refreshState() {
        recentAuditsContainer.getChildren().clear();
        List<ScanRecord> activeLogs = historyService.loadHistory();

        if (activeLogs == null || activeLogs.isEmpty()) {
            primaryMessageLabel.setText("Begin Code Security Inspection");
            secondaryMessageLabel.setText("Load a clean source file script or project workspace to start.");
        } else {
            primaryMessageLabel.setText("Active Workspace Workbench");
            secondaryMessageLabel.setText("Select an action below or pick a recently tracked file to review.");

            Label sectionHeading = new Label("Recent Workspace Audits");
            sectionHeading.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #6272a4; -fx-padding: 10 0 4 0;");
            recentAuditsContainer.getChildren().add(sectionHeading);

            // Fetch up to 3 most recent real scans from database history
            int displayCount = Math.min(3, activeLogs.size());
            for (int i = activeLogs.size() - 1; i >= activeLogs.size() - displayCount; i--) {
                ScanRecord logItem = activeLogs.get(i);
                
                HBox miniCard = new HBox(12);
                miniCard.getStyleClass().add("bordered-card");
                miniCard.setAlignment(Pos.CENTER_LEFT);
                miniCard.setPadding(new Insets(10, 14, 10, 14));

                Label fileIcon = new Label("📄");
                Label fileName = new Label(logItem.getFilename());
                fileName.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");
                
                HBox spacer = new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label timeLabel = new Label(logItem.getTimestamp());
                timeLabel.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 12px;");

                miniCard.getChildren().addAll(fileIcon, fileName, spacer, timeLabel);
                recentAuditsContainer.getChildren().add(miniCard);
            }
        }
    }
}
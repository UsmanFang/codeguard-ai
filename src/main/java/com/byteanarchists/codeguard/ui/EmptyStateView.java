//screen 1, shown before any file is opened
package com.byteanarchists.codeguard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.lang.*;

public class EmptyStateView extends VBox {

    public EmptyStateView(Runnable onBrowseAction) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));
        setStyle("-fx-background-color: #282a36;");

        // Center visual stack badge
        Label circularBadge = new Label("🛡");
        circularBadge.setPrefSize(70, 70);
        circularBadge.setAlignment(Pos.CENTER);
        circularBadge.setStyle("-fx-font-size: 32px; -fx-background-color: rgba(80, 250, 123, 0.15); -fx-text-fill: #50fa7b; -fx-background-radius: 35px;");

        Label primaryTitle = new Label("Scan your first file");
        primaryTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f8f8f2;");

        Label secondarySub = new Label("Drop a Java file here, or browse your project folder.");
        secondarySub.setStyle("-fx-font-size: 13px; -fx-text-fill: #6272a4;");

        Button browseBtn = new Button("Open file");
        browseBtn.getStyleClass().add("btn-emerald");
        browseBtn.setPrefHeight(36);
        browseBtn.setPadding(new Insets(0, 24, 0, 24));
        browseBtn.setOnAction(e -> onBrowseAction.run());

        // Recent Activity container
        VBox recentContainer = new VBox(8);
        recentContainer.setMaxWidth(480);
        recentContainer.setPadding(new Insets(30, 0, 0, 0));

        Label recentHeader = new Label("Recent Workspace Audits");
        recentHeader.setStyle("-fx-font-size: 11px; -fx-text-fill: #6272a4; -fx-font-weight: bold;");

        HBox recentRow = new HBox(12);
        recentRow.getStyleClass().add("bordered-card");
        recentRow.setAlignment(Pos.CENTER_LEFT);

        Label fileIcon = new Label("📄");
        Label fileName = new Label("Target.java");
        fileName.setStyle("-fx-text-fill: #f8f8f2; -fx-font-family: 'JetBrains Mono';");
        Label timeBadge = new Label("2 hours ago");
        timeBadge.setStyle("-fx-text-fill: #6272a4; -fx-font-size: 11px;");

        HBox.setHgrow(fileName, Priority.ALWAYS);
        recentRow.getChildren().addAll(fileIcon, fileName, timeBadge);
        recentContainer.getChildren().addAll(recentHeader, recentRow);

        getChildren().addAll(circularBadge, primaryTitle, secondarySub, browseBtn, recentContainer);
    }
}
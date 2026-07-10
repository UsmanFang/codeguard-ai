package com.byteanarchists.codeguard.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

public class StatusBar extends HBox {
    private final Label operationalStatus = new Label("System Status: Idle");
    private final Label contextTelemetry = new Label("");
    private final Label backendHardwareRef = new Label("Fireworks AI · AMD Instinct™ MI300X Accelerator");
    private FadeTransition blinkAnimation;
    private String currentTextColor = "#f8f8f2";

    public StatusBar() {
        setPadding(new Insets(5, 12, 5, 12));
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(20);

        updateStateClass("status-idle");
        applyTextColor("#f8f8f2");
        operationalStatus.setText("System Status: Idle");
        contextTelemetry.setText("");

        HBox flexibleSpacer = new HBox();
        HBox.setHgrow(flexibleSpacer, Priority.ALWAYS);
        getChildren().addAll(operationalStatus, contextTelemetry, flexibleSpacer, backendHardwareRef);
    }

    // ---------- Transitions ----------
    public void transitionToIdle() {
        stopBlinkAnimation();
        updateStateClass("status-idle");
        operationalStatus.setText("System Status: Idle");
        contextTelemetry.setText("");
        applyTextColor("#f8f8f2");
    }

    public void transitionToScanning() {
        updateStateClass("status-scanning");
        operationalStatus.setText("System Status: Analyzing target elements via Fireworks API...");
        contextTelemetry.setText("Scanning in progress...");
        applyTextColor("#000000");
        startBlinkAnimation();
    }

    public void transitionToSuccess(int issueCount) {
        stopBlinkAnimation();
        updateStateClass("status-success");
        operationalStatus.setText("System Status: Evaluation complete!");
        // DO NOT set contextTelemetry here – let updateScanResult handle it.
        applyTextColor("#000000");
    }

    public void transitionToError(String message) {
        stopBlinkAnimation();
        updateStateClass("status-error");
        operationalStatus.setText("Error: " + message);
        contextTelemetry.setText("");
        applyTextColor("#000000");
    }

    // ---------- Public setters ----------
    /**
     * Sets the main status text WITHOUT changing the current color.
     * Used for info messages that should preserve the state's color.
     */
    public void setInfoText(String text) {
        operationalStatus.setText(text);
        // Do NOT change color – keep whatever the current state uses.
    }

    /**
     * Sets the main status text without changing color (for backward compatibility).
     */
    public void setText(String text) {
        operationalStatus.setText(text);
        // Preserve current color.
    }

    /**
     * Updates the scan result telemetry with real filename and count.
     * Forces black text because this is called only in success state.
     */
    public void updateScanResult(String filename, int issueCount) {
        contextTelemetry.setText(filename + " | Found " + issueCount + " Vulnerabilities");
        applyTextColor("#000000"); // ensure black on green
    }

    // ---------- Private helpers ----------
    private void applyTextColor(String color) {
        currentTextColor = color;
        operationalStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
        contextTelemetry.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
        backendHardwareRef.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px;");
    }

    private void updateStateClass(String newStyleClass) {
        getStyleClass().removeAll("status-idle", "status-scanning", "status-success", "status-error");
        getStyleClass().add(newStyleClass);
    }

    private void startBlinkAnimation() {
        if (blinkAnimation != null) blinkAnimation.stop();
        blinkAnimation = new FadeTransition(Duration.millis(800), operationalStatus);
        blinkAnimation.setFromValue(1.0);
        blinkAnimation.setToValue(0.3);
        blinkAnimation.setCycleCount(FadeTransition.INDEFINITE);
        blinkAnimation.setAutoReverse(true);
        blinkAnimation.play();
    }

    private void stopBlinkAnimation() {
        if (blinkAnimation != null) {
            blinkAnimation.stop();
            blinkAnimation = null;
        }
        operationalStatus.setOpacity(1.0);
    }
}
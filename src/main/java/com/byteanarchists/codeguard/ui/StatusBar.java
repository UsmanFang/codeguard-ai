//bottom Label: Idle / Scanning... / Done / Error
package com.byteanarchists.codeguard.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class StatusBar extends HBox {
    private final Label operationalStatus = new Label("System Status: Idle");
    private final Label contextTelemetry = new Label("Target.java | 0 Issues");
    private final Label backendHardwareRef = new Label("Fireworks AI · AMD Instinct™ MI300X Accelerator");
    private FadeTransition blinkAnimation;
    


    public StatusBar() {
        setPadding(new Insets(5, 12, 5, 12));
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(20);
        
        // Dynamic operational defaults initialization
        updateStateClass("status-idle");

        operationalStatus.setStyle("-fx-font-weight: bold;");
        
        HBox flexibleSpacer = new HBox();
        HBox.setHgrow(flexibleSpacer, Priority.ALWAYS);

        getChildren().addAll(operationalStatus, contextTelemetry, flexibleSpacer, backendHardwareRef);
    }

    public void transitionToIdle() {
        stopBlinkAnimation();
        updateStateClass("status-idle");
        operationalStatus.setText("System Status: Idle");
    }

    public void transitionToScanning() {
        updateStateClass("status-scanning");
        operationalStatus.setText("System Status: Analyzing target elements via Fireworks API...");
        startBlinkAnimation();
    }

    public void transitionToSuccess(int issueCount) {
        stopBlinkAnimation();
        updateStateClass("status-success");
        operationalStatus.setText("System Status: Evaluation complete!");
        contextTelemetry.setText("Target.java | Found " + issueCount + " Vulnerabilities");
    }

    /** Neutral info message (e.g. "Saved successfully") - does NOT apply error styling. */
    public void setInfoText(String message) {
        updateStateClass("status-idle");
        operationalStatus.setText(message);
    }

    private void updateStateClass(String newStyleClass) {
        getStyleClass().removeAll("status-idle", "status-scanning", "status-success", "status-error");
        getStyleClass().add(newStyleClass);
    }

    /** Sets an error message and applies error styling. Previously this was reused for
     *  ALL status text updates (including success messages like "Saved successfully"),
     *  which meant a successful save showed up styled as a red error. */
    public void setText(String message) {
        operationalStatus.setText(message);
        updateStateClass("status-error");
    }

    public void startBlinkAnimation() {
    if (blinkAnimation != null) {
            blinkAnimation.stop();
        }
        blinkAnimation = new FadeTransition(Duration.millis(800), operationalStatus);
        blinkAnimation.setFromValue(1.0);
        blinkAnimation.setToValue(0.3);
        blinkAnimation.setCycleCount(FadeTransition.INDEFINITE);
        blinkAnimation.setAutoReverse(true);
        blinkAnimation.play();
    }

    public void stopBlinkAnimation() {
        if (blinkAnimation != null) {
            blinkAnimation.stop();
            blinkAnimation = null;
        }
        operationalStatus.setOpacity(1.0);
    }
}
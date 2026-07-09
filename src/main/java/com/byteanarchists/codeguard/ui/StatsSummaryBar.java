//4-metric row (Total/Critical/High/Info)
package com.byteanarchists.codeguard.ui;

import com.byteanarchists.codeguard.api.model.Finding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;

public class StatsSummaryBar extends HBox {
    private final Label totalCount = new Label("0");
    private final Label criticalCount = new Label("0");
    private final Label highCount = new Label("0");
    private final Label infoCount = new Label("0");

    public StatsSummaryBar() {
        setPadding(new Insets(8, 14, 8, 14));
        setSpacing(16);
        setStyle("-fx-background-color: #21222c; -fx-border-color: #44475a; -fx-border-width: 0 0 1px 0;");

        getChildren().addAll(
            createMetricCell("TOTAL FINDINGS", totalCount, "#f8f8f2"),
            createMetricCell("CRITICAL", criticalCount, "#ff5555"),
            createMetricCell("HIGH SEVERITY", highCount, "#ffb86c"),
            createMetricCell("INFORMATIONAL", infoCount, "#8be9fd")
        );
    }

    private VBox createMetricCell(String headline, Label counter, String colorHex) {
        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER_LEFT);
        
        Label tag = new Label(headline);
        tag.setStyle("-fx-font-size: 10px; -fx-text-fill: #6272a4; -fx-font-weight: bold;");
        
        counter.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + colorHex + ";");
        
        cell.getChildren().addAll(tag, counter);
        return cell;
    }

    public void refreshCounts(List<Finding> findings) {
        long critical = findings.stream().filter(f -> "CRITICAL".equalsIgnoreCase(f.getSeverity())).count();
        long high = findings.stream().filter(f -> "HIGH".equalsIgnoreCase(f.getSeverity())).count();
        long info = findings.stream().filter(f -> "INFO".equalsIgnoreCase(f.getSeverity())).count();

        totalCount.setText(String.valueOf(findings.size()));
        criticalCount.setText(String.valueOf(critical));
        highCount.setText(String.valueOf(high));
        infoCount.setText(String.valueOf(info));
    }
}
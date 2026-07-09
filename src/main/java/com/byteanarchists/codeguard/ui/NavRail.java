//left icon strip (Scanner / History / Settings / Exit)
package com.byteanarchists.codeguard.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.util.function.Consumer;

public class NavRail extends VBox {
    private final Consumer<String> navigationCallback;
    private Button activeBtn;

    public NavRail(Consumer<String> callback) {
        this.navigationCallback = callback;

        setPrefWidth(84);
        setMinWidth(84);
        setMaxWidth(84);
        setStyle("-fx-background-color: #1e1f29; -fx-padding: 12px 0 12px 0;");
        setSpacing(14);
        setAlignment(Pos.TOP_CENTER);

        Button scanBtn = createNavButton("🛡", "Scanner");
        Button historyBtn = createNavButton("📜", "History");
        Button settingsBtn = createNavButton("⚙", "Settings");

        // Spacer pushes the exit control to the bottom of the rail
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button exitBtn = new Button("⏻");
        exitBtn.setPrefSize(48, 48);
        exitBtn.setStyle("-fx-font-size: 20px; -fx-background-radius: 10px; -fx-text-fill: #ff5555; -fx-background-color: transparent;");
        exitBtn.setOnAction(e -> Platform.exit());
        Tooltip.install(exitBtn, new Tooltip("Exit application"));

        getChildren().addAll(
            wrapWithCaption(scanBtn, "Scan"),
            wrapWithCaption(historyBtn, "History"),
            wrapWithCaption(settingsBtn, "Settings"),
            spacer,
            wrapWithCaption(exitBtn, "Exit")
        );

        setActive(scanBtn);
    }

    private static final String BASE_STYLE =
        "-fx-font-size: 22px; -fx-background-radius: 10px; -fx-text-fill: #6272a4; -fx-background-color: transparent;";

    private Button createNavButton(String icon, String viewName) {
        Button btn = new Button(icon);
        btn.setPrefSize(48, 48);
        btn.setStyle(BASE_STYLE);
        Tooltip.install(btn, new Tooltip(viewName));

        btn.setOnAction(e -> {
            setActive(btn);
            navigationCallback.accept(viewName);
        });
        return btn;
    }

    /** Wraps a nav button with a small caption label underneath, e.g. "Scan", "History". */
    private VBox wrapWithCaption(Button button, String captionText) {
        Label caption = new Label(captionText);
        caption.setStyle("-fx-font-size: 10px; -fx-text-fill: #6272a4; -fx-font-family: 'JetBrains Mono';");

        VBox wrapper = new VBox(4, button, caption);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private void setActive(Button target) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("nav-rail-active");
            activeBtn.setStyle(BASE_STYLE);
        }
        activeBtn = target;
        activeBtn.getStyleClass().add("nav-rail-active");
        activeBtn.setStyle(BASE_STYLE + " -fx-text-fill: #50fa7b;");
    }
}
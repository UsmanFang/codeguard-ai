// extends Application, launches MainView, sets AtlantaFX theme
package com.byteanarchists.codeguard;

import com.byteanarchists.codeguard.ui.MainView;

import atlantafx.base.theme.Dracula;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryWorkspaceStage) {
        // Enforce the core Dracula variant theme styling layer configuration
        Application.setUserAgentStylesheet(new Dracula().getUserAgentStylesheet());

        MainView rootAppWorkspaceView = new MainView();
        Scene globalScene = new Scene(rootAppWorkspaceView, 1200, 650);
        
        // Link custom structural framework extensions definitions on top
        globalScene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        primaryWorkspaceStage.setTitle("CodeGuard AI Studio");
        primaryWorkspaceStage.setScene(globalScene);
        primaryWorkspaceStage.centerOnScreen();
        primaryWorkspaceStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
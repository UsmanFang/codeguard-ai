//  root BorderPane: toolbar top, split pane center, status bottom
package com.byteanarchists.codeguard.ui;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import com.byteanarchists.codeguard.util.SettingsStore;
import com.byteanarchists.codeguard.api.FireworksScannerImpl;
import com.byteanarchists.codeguard.api.ScannerService;
import com.byteanarchists.codeguard.api.model.Finding;
import com.byteanarchists.codeguard.api.model.ScanRecord;
import com.byteanarchists.codeguard.io.FileService;
import com.byteanarchists.codeguard.io.FileServiceImpl;
import com.byteanarchists.codeguard.io.HistoryService;

import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;


public class MainView extends BorderPane {
    private final NavRail navigationRail;
    private final StatusBar applicationStatusBar;
    
    // Core functional views stack instances

    private final EmptyStateView filePickerPlaceholder;
    private final VBox interactiveWorkspaceContainer;
    private final WorkspaceSplitPane splitWorkspaceLayout;
    private final StatsSummaryBar workspaceStatsSummary;
    private final HistoryView historicAuditLogView;
    private final SettingsView applicationSettingsView;
    private File currentFile;
    private final FileService fileService = new FileServiceImpl();
    private final HistoryService historyService = new HistoryService();

    // private final ScannerService scanningEngine = new FakeScannerImpl();
    private final ScannerService scanningEngine = new FireworksScannerImpl();

    public MainView() {
        setStyle("-fx-background-color: #282a36;");

        // Initializing UI layouts Subcomponents
        applicationStatusBar = new StatusBar();
        
        splitWorkspaceLayout = new WorkspaceSplitPane();
        splitWorkspaceLayout.getReportPanel().setSaveAction(this::handleSaveFile);
        workspaceStatsSummary = new StatsSummaryBar();
        
        ControlBar workspaceControlToolbar = new ControlBar(
            this::handleFileLoadExecution,
            this::executeSecurityPipelineAnalysis,
            this::handleSaveFile,
            this::handleSaveAsFile,
            this::handleCloseFile
        );

        

        interactiveWorkspaceContainer = new VBox();
        interactiveWorkspaceContainer.getChildren().addAll(
            workspaceControlToolbar, 
            workspaceStatsSummary, 
            splitWorkspaceLayout
        );
        // Without this, the split pane (editor + report) only takes its preferred
        // size and the rest of the VBox is left blank instead of filling the window.
        VBox.setVgrow(splitWorkspaceLayout, javafx.scene.layout.Priority.ALWAYS);

        filePickerPlaceholder = new EmptyStateView(this::handleFileLoadExecution);
        historicAuditLogView = new HistoryView(this::openFileFromHistory);
        applicationSettingsView = new SettingsView();

        navigationRail = new NavRail(this::navigateToSubpanelView);

        // Core Layout Assembly Placement configuration rules
        setLeft(navigationRail);
        setCenter(filePickerPlaceholder); 
        setBottom(applicationStatusBar);
    }

    private void navigateToSubpanelView(String requestedTargetView) {
        switch (requestedTargetView) {
            case "Scanner" -> {
                // Return to loaded workbench environment or display fallback onboarding screen
                if (splitWorkspaceLayout.getEditorPanel().getCodeContent().isEmpty()) {
                    setCenter(filePickerPlaceholder);
                } else {
                    setCenter(interactiveWorkspaceContainer);
                }
                setBottom(applicationStatusBar);
            }
            case "History" -> {
                setCenter(historicAuditLogView);
                setBottom(null); // Clear context footer per requirement layout definitions
            }
            case "Settings" -> {
                setCenter(applicationSettingsView);
                setBottom(null);
            }
        }
    }

    private void handleCloseFile() {
        // Clear the loaded source and any findings, then return to the onboarding screen.
        // currentFile MUST be reset here too - otherwise a later "Save" (after the user
        // types something new without opening a file) would silently overwrite the
        // previously closed file on disk.
        splitWorkspaceLayout.getEditorPanel().setCodeContent("");
        splitWorkspaceLayout.getReportPanel().renderFindings(java.util.List.of());
        currentFile = null;
        setCenter(filePickerPlaceholder);
        applicationStatusBar.transitionToIdle();
    }

    private void handleFileLoadExecution() {
    Window owner = this.getScene().getWindow();
    File selected = fileService.pickFile(owner);
    if (selected != null) {
        try {
            String content = fileService.readFile(selected);
            currentFile = selected;
            splitWorkspaceLayout.getEditorPanel().setCodeContent(content);
            setCenter(interactiveWorkspaceContainer);
            applicationStatusBar.transitionToIdle();
        } catch (IOException e) {
            applicationStatusBar.setText("Error loading file: " + e.getMessage());
        }
    }
}

    private void executeSecurityPipelineAnalysis() {
            String codeSegment = splitWorkspaceLayout.getEditorPanel().getCodeContent();
            if (codeSegment.isEmpty()) return;

            applicationStatusBar.transitionToScanning();

            scanningEngine.runScanAsync(codeSegment).thenAccept(result -> Platform.runLater(() -> {
                if (result.isSuccess()) {
                    workspaceStatsSummary.refreshCounts(result.getFindings());
                    splitWorkspaceLayout.getReportPanel().renderFindings(result.getFindings());
                    applicationStatusBar.transitionToSuccess(result.getFindings().size());

                    // Triggers structural source context highlight lines mapping loops
                    result.getFindings().forEach(finding ->
                        splitWorkspaceLayout.getEditorPanel().highlightVulnerableLine(finding.getLineNumber())
                    );

                    // 🔽 Auto‑save if the setting is enabled
                    if (SettingsStore.isAutoSaveEnabled()) {
                        handleSaveFile();
                    }
                }
                // Save to history
                String fileNameForRecord = (currentFile != null) ? currentFile.getName() : "(untitled)";
                String filePathForRecord = (currentFile != null) ? currentFile.getAbsolutePath() : "";
                ScanRecord record = new ScanRecord(
                    fileNameForRecord,
                    filePathForRecord,
                    result.getFindings().stream().map(Finding::getSeverity).collect(Collectors.toList()),
                    java.time.LocalDateTime.now().toString()
                );

                historyService.saveRecord(record);
                historicAuditLogView.refresh();

                if (!result.isSuccess()) {
                    applicationStatusBar.setText(result.getErrorMessage());
                }
            }));
        }

    private String getTargetSampleCode() {
        return """
            package com.byteanarchists.codeguard.sample;
            
            import java.sql.Connection;
            import java.sql.DriverManager;
            
            public class Target {
                private static final String API_KEY = "3a9f8b2c5e7d1a6f0c4e8b3a7d2e9f1c";
                
                public void verifyUser(String inputId) throws Exception {
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db", "root", "secret");
                    String rawQuery = "SELECT * FROM users WHERE id = '" + inputId + "'";
                    conn.createStatement().execute(rawQuery);
                }
            }
            """;
    }


    private void handleSaveFile() {
        if (currentFile == null) {
            applicationStatusBar.setText("No file loaded to save.");
            return;
        }
        try {
            fileService.saveFile(currentFile, splitWorkspaceLayout.getEditorPanel().getCodeContent());
            applicationStatusBar.setInfoText("Saved successfully.");
        } catch (IOException e) {
            applicationStatusBar.setText("Save failed: " + e.getMessage());
        }
    }

    private void handleSaveAsFile() {
        Window owner = this.getScene().getWindow();
        String suggestedName = (currentFile != null) ? currentFile.getName() : "untitled.java";
        File destination = fileService.pickSaveLocation(owner, suggestedName);
        if (destination == null) {
            return; // user cancelled the dialog
        }
        try {
            fileService.saveFile(destination, splitWorkspaceLayout.getEditorPanel().getCodeContent());
            currentFile = destination;
            applicationStatusBar.setInfoText("Saved as " + destination.getName());
        } catch (IOException e) {
            applicationStatusBar.setText("Save As failed: " + e.getMessage());
        }
    }

    private void openFileFromHistory(String path) {
        File target = new File(path);
        if (!target.exists()) {
            applicationStatusBar.setText("Could not reopen " + target.getName() + " - the file no longer exists at that location.");
            return;
        }
        try {
            String content = fileService.readFile(target);
            currentFile = target;
            splitWorkspaceLayout.getEditorPanel().setCodeContent(content);
            navigateToSubpanelView("Scanner");
            applicationStatusBar.transitionToIdle();
        } catch (IOException e) {
            applicationStatusBar.setText("Error reopening file: " + e.getMessage());
        }
    }
}
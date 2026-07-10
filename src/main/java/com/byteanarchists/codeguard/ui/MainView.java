package com.byteanarchists.codeguard.ui;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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
    private final EmptyStateView filePickerPlaceholder;
    private final VBox interactiveWorkspaceContainer;
    private final WorkspaceSplitPane splitWorkspaceLayout;
    private final StatsSummaryBar workspaceStatsSummary;
    private final HistoryView historicAuditLogView;
    private final SettingsView applicationSettingsView;
    private final ControlBar workspaceControlToolbar;
    
    private File currentFile;
    private final FileService fileService = new FileServiceImpl();
    private final HistoryService historyService = new HistoryService();
    private final ScannerService scanningEngine = new FireworksScannerImpl();
    private final DateTimeFormatter logTimestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MainView() {
        setStyle("-fx-background-color: #282a36;");
        getStyleClass().add("theme-dark");

        applicationStatusBar = new StatusBar();
        splitWorkspaceLayout = new WorkspaceSplitPane();
        splitWorkspaceLayout.getReportPanel().setSaveAction(this::handleSaveFile);
        workspaceStatsSummary = new StatsSummaryBar();
        
        workspaceControlToolbar = new ControlBar(
            this::handleFileLoadExecution,
            this::executeSecurityPipelineAnalysis,
            this::handleSaveFile,
            this::handleSaveAsFile,
            this::handleCloseFile
        );

        interactiveWorkspaceContainer = new VBox();
        interactiveWorkspaceContainer.getChildren().addAll(workspaceControlToolbar, workspaceStatsSummary, splitWorkspaceLayout);
        VBox.setVgrow(splitWorkspaceLayout, javafx.scene.layout.Priority.ALWAYS);

        filePickerPlaceholder = new EmptyStateView(this::handleFileLoadExecution);
        historicAuditLogView = new HistoryView(this::openFileFromHistory);
        applicationSettingsView = new SettingsView();

        historicAuditLogView.setOnRecordSelected(this::restoreHistoricalSnapshotWindow);
        navigationRail = new NavRail(this::navigateToSubpanelView);

        setLeft(navigationRail);
        setBottom(applicationStatusBar);
        checkAndInitializeWorkspace();
    }

    // ------------------------------------------------------------
    //  Helper to keep UI clean (from HEAD – placeholder for now)
    // ------------------------------------------------------------
    private void syncUIWithCurrentFile() {
        if (currentFile == null) return;
        splitWorkspaceLayout.getEditorPanel().textProperty().addListener((obs, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                splitWorkspaceLayout.getReportPanel().setOpacity(0.5);
                applicationStatusBar.setInfoText("Modified: " + currentFile.getName() + " (Rescan required)");
            }
        });
    }

    // ------------------------------------------------------------
    //  Workspace initialization (from HEAD)
    // ------------------------------------------------------------
    private void checkAndInitializeWorkspace() {
        List<ScanRecord> scanHistory = historyService.loadHistory();
        if (scanHistory != null && !scanHistory.isEmpty()) {
            ScanRecord mostRecentScan = scanHistory.get(scanHistory.size() - 1);
            splitWorkspaceLayout.getEditorPanel().setCodeContent(mostRecentScan.getSourceCodeSnapshot());
            applicationStatusBar.setInfoText("Active Workbench Matrix: " + mostRecentScan.getFilename());
            setCenter(interactiveWorkspaceContainer);
        } else {
            setCenter(filePickerPlaceholder);
        }
    }

    // ------------------------------------------------------------
    //  Main scan execution (merged: HEAD logic + incoming auto‑save)
    // ------------------------------------------------------------
    private void executeSecurityPipelineAnalysis() {
        String codeSegment = splitWorkspaceLayout.getEditorPanel().getCodeContent();
        if (codeSegment.isEmpty()) return;
        String fileNameForRecord = (currentFile != null) ? currentFile.getName() : "(untitled)";
        String filePathForRecord = (currentFile != null) ? currentFile.getAbsolutePath() : "";

        applicationStatusBar.transitionToScanning();
        scanningEngine.runScanAsync(codeSegment).thenAccept(result -> Platform.runLater(() -> {
            if (result.isSuccess()) {
                workspaceStatsSummary.refreshCounts(result.getFindings());
                splitWorkspaceLayout.getReportPanel().renderFindings(result.getFindings());
                applicationStatusBar.transitionToSuccess(result.getFindings().size());

                // Highlight vulnerabilities
                result.getFindings().forEach(finding ->
                    splitWorkspaceLayout.getEditorPanel().highlightVulnerableLine(finding.getLineNumber())
                );

                // Auto‑save if enabled (from incoming)
                if (SettingsStore.isAutoSaveEnabled()) {
                    handleSaveFile();
                }
            }

            // Save history with full findings and source code snapshot (from HEAD)
            ScanRecord newRecord = new ScanRecord(
                fileNameForRecord,
                filePathForRecord,
                result.getFindings().stream().map(Finding::getSeverity).collect(Collectors.toList()),
                result.getFindings(),                           // full findings list
                LocalDateTime.now().format(logTimestampFormatter),
                codeSegment                                     // source code snapshot
            );

            // Deduplicate history (from HEAD)
            List<ScanRecord> historyList = new ArrayList<>(historyService.loadHistory());
            historyList.removeIf(item -> item.getFilename().equalsIgnoreCase(fileNameForRecord));
            historyList.add(newRecord);
            historyService.saveAll(historyList);

            historicAuditLogView.refresh();
            filePickerPlaceholder.refreshState();

            if (!result.isSuccess()) {
                applicationStatusBar.setText(result.getErrorMessage());
            }
        }));
    }

    // ------------------------------------------------------------
    //  Navigation (compact from HEAD)
    // ------------------------------------------------------------
    private void navigateToSubpanelView(String requestedTargetView) {
        switch (requestedTargetView) {
            case "Scanner" -> setCenter(splitWorkspaceLayout.getEditorPanel().getCodeContent().isEmpty() ? filePickerPlaceholder : interactiveWorkspaceContainer);
            case "History" -> { historicAuditLogView.refresh(); setCenter(historicAuditLogView); setBottom(null); }
            case "Settings" -> { setCenter(applicationSettingsView); setBottom(null); }
        }
    }

    // ------------------------------------------------------------
    //  Restore a historical snapshot (from HEAD)
    // ------------------------------------------------------------
    private void restoreHistoricalSnapshotWindow(ScanRecord historicRecord) {
        splitWorkspaceLayout.getEditorPanel().setCodeContent(historicRecord.getSourceCodeSnapshot());
        splitWorkspaceLayout.getReportPanel().renderFindings(historicRecord.getFindings());
        workspaceStatsSummary.refreshCounts(historicRecord.getFindings());
        setCenter(interactiveWorkspaceContainer);
        setBottom(applicationStatusBar);
        applicationStatusBar.setInfoText("Restored Audit Matrix: " + historicRecord.getFilename());
    }

    // ------------------------------------------------------------
    //  File load (from HEAD + reset logic)
    // ------------------------------------------------------------
    private void handleFileLoadExecution() {
        Window owner = this.getScene().getWindow();
        File selected = fileService.pickFile(owner);
        if (selected != null) {
            try {
                String content = fileService.readFile(selected);
                currentFile = selected;

                // Reset UI state (from HEAD)
                splitWorkspaceLayout.getEditorPanel().setCodeContent(content);
                splitWorkspaceLayout.getReportPanel().renderFindings(List.of());
                workspaceStatsSummary.refreshCounts(List.of());
                splitWorkspaceLayout.getEditorPanel().clearHighlights();

                setCenter(interactiveWorkspaceContainer);
                applicationStatusBar.transitionToIdle();
                applicationStatusBar.setInfoText("Active Workbench Matrix: " + selected.getName());
            } catch (IOException e) {
                applicationStatusBar.setText("Error loading file: " + e.getMessage());
            }
        }
    }

    // ------------------------------------------------------------
    //  Close file (from HEAD – simpler)
    // ------------------------------------------------------------
    private void handleCloseFile() {
        currentFile = null;
        filePickerPlaceholder.refreshState();
        setCenter(filePickerPlaceholder);
    }

    // ------------------------------------------------------------
    //  Save / Save As / Open from history (from incoming)
    // ------------------------------------------------------------
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
            return;
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

    // ------------------------------------------------------------
    //  Unused sample – kept from incoming (harmless)
    // ------------------------------------------------------------
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
}
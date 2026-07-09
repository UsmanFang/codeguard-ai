package com.byteanarchists.codeguard.ui;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        workspaceStatsSummary = new StatsSummaryBar();
        
        workspaceControlToolbar = new ControlBar(
            this::handleFileLoadExecution,
            this::executeSecurityPipelineAnalysis,
            this::handleSaveFile,       
            this::handleCloseFile
        );

        interactiveWorkspaceContainer = new VBox();
        interactiveWorkspaceContainer.getChildren().addAll(workspaceControlToolbar, workspaceStatsSummary, splitWorkspaceLayout);
        VBox.setVgrow(splitWorkspaceLayout, javafx.scene.layout.Priority.ALWAYS);

        filePickerPlaceholder = new EmptyStateView(this::handleFileLoadExecution);
        historicAuditLogView = new HistoryView();
        applicationSettingsView = new SettingsView();

        historicAuditLogView.setOnRecordSelected(this::restoreHistoricalSnapshotWindow);
        navigationRail = new NavRail(this::navigateToSubpanelView);

        setLeft(navigationRail);
        setBottom(applicationStatusBar);
        checkAndInitializeWorkspace();
    }

    
// Add this helper to MainView to keep the UI clean
private void syncUIWithCurrentFile() {
    if (currentFile == null) return;
    
    // If you want the report to clear when the user starts typing, 
    // add a listener to your EditorPanel
    splitWorkspaceLayout.getEditorPanel().textProperty().addListener((obs, oldVal, newVal) -> {
        if (!oldVal.equals(newVal)) {
            // Optional: Dim the report panel to indicate it's now stale
            splitWorkspaceLayout.getReportPanel().setOpacity(0.5); 
            applicationStatusBar.setInfoText("Modified: " + currentFile.getName() + " (Rescan required)");
        }
    });
}

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

private void executeSecurityPipelineAnalysis() {
    String codeSegment = splitWorkspaceLayout.getEditorPanel().getCodeContent();
    if (codeSegment.isEmpty()) return;
    String fileNameForRecord = (currentFile != null) ? currentFile.getName() : "(untitled)";

    applicationStatusBar.transitionToScanning();
    scanningEngine.runScanAsync(codeSegment).thenAccept(result -> Platform.runLater(() -> {
        if (result.isSuccess()) {
            workspaceStatsSummary.refreshCounts(result.getFindings());
            splitWorkspaceLayout.getReportPanel().renderFindings(result.getFindings());
            applicationStatusBar.transitionToSuccess(result.getFindings().size());
        }

        // UPDATED: Now passing result.getFindings() as the 3rd argument to match the new constructor
        ScanRecord newRecord = new ScanRecord(
            fileNameForRecord, 
            result.getFindings().stream().map(Finding::getSeverity).collect(Collectors.toList()), 
            result.getFindings(), // Pass the full findings list here
            LocalDateTime.now().format(logTimestampFormatter), 
            codeSegment
        );
        
        // DEDUPLICATION logic remains the same
        List<ScanRecord> historyList = new ArrayList<>(historyService.loadHistory());
        historyList.removeIf(item -> item.getFilename().equalsIgnoreCase(fileNameForRecord));
        historyList.add(newRecord);
        
        historyService.saveAll(historyList);
        historicAuditLogView.refresh();
        filePickerPlaceholder.refreshState();
    }));
}

    private void navigateToSubpanelView(String requestedTargetView) {
        switch (requestedTargetView) {
            case "Scanner" -> setCenter(splitWorkspaceLayout.getEditorPanel().getCodeContent().isEmpty() ? filePickerPlaceholder : interactiveWorkspaceContainer);
            case "History" -> { historicAuditLogView.refresh(); setCenter(historicAuditLogView); setBottom(null); }
            case "Settings" -> { setCenter(applicationSettingsView); setBottom(null); }
        }
    }

   // Update this method in MainView.java
private void restoreHistoricalSnapshotWindow(ScanRecord historicRecord) {
    // 1. Restore the code
    splitWorkspaceLayout.getEditorPanel().setCodeContent(historicRecord.getSourceCodeSnapshot());
    
    // 2. IMPORTANT: Restore the Findings associated with this specific record
    // Assuming your ScanRecord has a getFindings() method
    splitWorkspaceLayout.getReportPanel().renderFindings(historicRecord.getFindings());
    
    // 3. Restore the Stats
    workspaceStatsSummary.refreshCounts(historicRecord.getFindings());

    setCenter(interactiveWorkspaceContainer);
    setBottom(applicationStatusBar);
    applicationStatusBar.setInfoText("Restored Audit Matrix: " + historicRecord.getFilename());
}

 private void handleFileLoadExecution() {
    Window owner = this.getScene().getWindow();
    File selected = fileService.pickFile(owner);
    if (selected != null) {
        try {
            String content = fileService.readFile(selected);
            currentFile = selected;
            
            // --- FIX START: RESET UI STATE ---
            splitWorkspaceLayout.getEditorPanel().setCodeContent(content);
            splitWorkspaceLayout.getReportPanel().renderFindings(List.of()); // Clear old findings
            workspaceStatsSummary.refreshCounts(List.of());               // Clear old stats
            splitWorkspaceLayout.getEditorPanel().clearHighlights();      // Clear old line highlights
            // --- FIX END ---
            
            setCenter(interactiveWorkspaceContainer);
            applicationStatusBar.transitionToIdle();
            applicationStatusBar.setInfoText("Active Workbench Matrix: " + selected.getName());
        } catch (IOException e) {
            applicationStatusBar.setText("Error loading file: " + e.getMessage());
        }
    }
}

    private void handleSaveFile() { /* Existing Implementation */ }
    private void handleCloseFile() { currentFile = null; filePickerPlaceholder.refreshState(); setCenter(filePickerPlaceholder); }
}
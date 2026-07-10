package com.byteanarchists.codeguard.ui;

import java.io.File;
import java.io.IOException;
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
import com.byteanarchists.codeguard.util.SettingsStore;

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
    private ScanRecord currentRecord;
    private final FileService fileService = new FileServiceImpl();
    private final HistoryService historyService = new HistoryService();
    private final ScannerService scanningEngine = new FireworksScannerImpl();

    public MainView() {
        setStyle("-fx-background-color: #282a36;");
        getStyleClass().add("theme-dark");

        applicationStatusBar = new StatusBar();
        splitWorkspaceLayout = new WorkspaceSplitPane();
        workspaceStatsSummary = new StatsSummaryBar();
        
        // Wire save action to ReportPanel so that "Apply Patch" can trigger auto‑save
        splitWorkspaceLayout.getReportPanel().setSaveAction(this::handleSaveFile);

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

        filePickerPlaceholder = new EmptyStateView(this::handleFileLoadExecution, this::restoreFromScanRecord);
        historicAuditLogView = new HistoryView(this::restoreFromScanRecord);
        applicationSettingsView = new SettingsView();
        applicationSettingsView.setOnModelChanged(() -> workspaceControlToolbar.refreshModelLabel());

        navigationRail = new NavRail(this::navigateToSubpanelView);

        setLeft(navigationRail);
        setBottom(applicationStatusBar);
        setCenter(filePickerPlaceholder);
    }

    private void navigateToSubpanelView(String requestedTargetView) {
        switch (requestedTargetView) {
            case "Scanner" -> {
                if (splitWorkspaceLayout.getEditorPanel().getCodeContent().isEmpty()) {
                    setCenter(filePickerPlaceholder);
                } else {
                    setCenter(interactiveWorkspaceContainer);
                }
                setBottom(applicationStatusBar);
            }
            case "History" -> {
                historicAuditLogView.refresh();
                setCenter(historicAuditLogView);
                setBottom(null);
            }
            case "Settings" -> {
                setCenter(applicationSettingsView);
                setBottom(null);
            }
        }
    }

    private void handleCloseFile() {
        splitWorkspaceLayout.getEditorPanel().setCodeContent("");
        splitWorkspaceLayout.getReportPanel().renderFindings(java.util.List.of());
        currentFile = null;
        currentRecord = null;
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
                tryRestoreFromHistory(selected);
                setCenter(interactiveWorkspaceContainer);
                applicationStatusBar.transitionToIdle();
            } catch (IOException e) {
                applicationStatusBar.setText("Error loading file: " + e.getMessage());
            }
        }
    }

    private void tryRestoreFromHistory(File file) {
        if (file == null) return;
        List<ScanRecord> history = historyService.loadHistory();
        for (ScanRecord record : history) {
            if (file.getAbsolutePath().equals(record.getFilePath())) {
                long currentModified = file.lastModified();
                if (currentModified == record.getFileLastModified()) {
                    restoreFromScanRecord(record);
                    return;
                } else {
                    applicationStatusBar.setInfoText("File modified since last scan – please rescan.");
                    splitWorkspaceLayout.getReportPanel().renderFindings(java.util.List.of());
                    workspaceStatsSummary.refreshCounts(java.util.List.of());
                    splitWorkspaceLayout.getEditorPanel().clearHighlights();
                    currentRecord = null;
                    return;
                }
            }
        }
        splitWorkspaceLayout.getReportPanel().renderFindings(java.util.List.of());
        workspaceStatsSummary.refreshCounts(java.util.List.of());
        splitWorkspaceLayout.getEditorPanel().clearHighlights();
        currentRecord = null;
        applicationStatusBar.transitionToIdle();
    }

    private void restoreFromScanRecord(ScanRecord record) {
        String filePath = record.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            applicationStatusBar.setText("Cannot restore: file path missing.");
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            applicationStatusBar.setText("File not found: " + record.getFilename());
            return;
        }
        try {
            String content = fileService.readFile(file);
            currentFile = file;
            currentRecord = record;
            splitWorkspaceLayout.getEditorPanel().setCodeContent(content);

            List<Finding> findings = record.getFindings();
            List<Integer> applied = record.getAppliedLines();
            List<Integer> dismissed = record.getDismissedLines();
            splitWorkspaceLayout.getReportPanel().renderFindings(
                findings,
                this::updateReviewState,
                applied,
                dismissed
            );
            workspaceStatsSummary.refreshCounts(findings);
            for (Finding f : findings) {
                splitWorkspaceLayout.getEditorPanel().highlightVulnerableLine(f.getLineNumber());
            }

            if (findings != null && !findings.isEmpty()) {
                applicationStatusBar.updateScanResult(record.getFilename(), findings.size());
                applicationStatusBar.transitionToSuccess(findings.size());
            } else {
                applicationStatusBar.setInfoText("Restored code – no findings stored.");
                applicationStatusBar.transitionToIdle();
            }
            setCenter(interactiveWorkspaceContainer);
        } catch (IOException e) {
            applicationStatusBar.setText("Error restoring: " + e.getMessage());
        }
    }

    private void updateReviewState(int line, boolean isApplied) {
        if (currentRecord == null) return;
        List<Integer> appliedLines = new ArrayList<>(currentRecord.getAppliedLines());
        List<Integer> dismissedLines = new ArrayList<>(currentRecord.getDismissedLines());
        if (isApplied) {
            appliedLines.add(line);
            dismissedLines.remove(Integer.valueOf(line));
        } else {
            dismissedLines.add(line);
            appliedLines.remove(Integer.valueOf(line));
        }
        ScanRecord updated = new ScanRecord(
            currentRecord.getFilename(),
            currentRecord.getFilePath(),
            currentRecord.getSeverities(),
            currentRecord.getTimestamp(),
            currentRecord.getFindings(),
            currentRecord.getSourceCodeSnapshot(),
            currentRecord.getFileLastModified(),
            currentRecord.getUpdateCount(),
            appliedLines,
            dismissedLines
        );
        List<ScanRecord> history = historyService.loadHistory();
        history.removeIf(r -> r.getFilePath() != null && r.getFilePath().equals(currentRecord.getFilePath()));
        history.add(updated);
        historyService.saveAll(history);
        currentRecord = updated;
    }

    private void executeSecurityPipelineAnalysis() {
        String codeSegment = splitWorkspaceLayout.getEditorPanel().getCodeContent();
        if (codeSegment.isEmpty()) return;

        applicationStatusBar.transitionToScanning();

        scanningEngine.runScanAsync(codeSegment).thenAccept(result -> Platform.runLater(() -> {
            if (result.isSuccess()) {
                workspaceStatsSummary.refreshCounts(result.getFindings());
                splitWorkspaceLayout.getReportPanel().renderFindings(
                    result.getFindings(),
                    this::updateReviewState,
                    new ArrayList<>(),
                    new ArrayList<>()
                );
                if (currentFile != null && result.getFindings() != null) {
                    applicationStatusBar.updateScanResult(currentFile.getName(), result.getFindings().size());
                }
                applicationStatusBar.transitionToSuccess(result.getFindings().size());

                result.getFindings().forEach(finding ->
                    splitWorkspaceLayout.getEditorPanel().highlightVulnerableLine(finding.getLineNumber())
                );
            }

            // Save to history with update tracking
            String fileNameForRecord = (currentFile != null) ? currentFile.getName() : "(untitled)";
            String filePathForRecord = (currentFile != null) ? currentFile.getAbsolutePath() : "";
            long lastModified = (currentFile != null) ? currentFile.lastModified() : 0L;
            List<ScanRecord> history = historyService.loadHistory();
            ScanRecord existing = null;
            for (ScanRecord r : history) {
                if (filePathForRecord.equals(r.getFilePath())) {
                    existing = r;
                    break;
                }
            }
            int updateCount = (existing != null) ? existing.getUpdateCount() + 1 : 0;
            List<Integer> applied = (existing != null) ? existing.getAppliedLines() : new ArrayList<>();
            List<Integer> dismissed = (existing != null) ? existing.getDismissedLines() : new ArrayList<>();

            ScanRecord record = new ScanRecord(
                fileNameForRecord,
                filePathForRecord,
                result.getFindings().stream().map(Finding::getSeverity).collect(Collectors.toList()),
                java.time.LocalDateTime.now().toString(),
                result.getFindings(),
                codeSegment,
                lastModified,
                updateCount,
                applied,
                dismissed
            );

            if (existing != null) {
                history.remove(existing);
            }
            history.add(record);
            historyService.saveAll(history);
            currentRecord = record;
            historicAuditLogView.refresh();
            filePickerPlaceholder.refreshState();

            if (!result.isSuccess()) {
                applicationStatusBar.transitionToError(result.getErrorMessage());
            }
        }));
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
        if (destination == null) return;
        try {
            fileService.saveFile(destination, splitWorkspaceLayout.getEditorPanel().getCodeContent());
            currentFile = destination;
            applicationStatusBar.setInfoText("Saved as " + destination.getName());
        } catch (IOException e) {
            applicationStatusBar.setText("Save As failed: " + e.getMessage());
        }
    }
}
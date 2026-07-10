package com.byteanarchists.codeguard.api.model;

import java.util.List;
import java.util.ArrayList;

public class ScanRecord {
    private final String filename;
    private final String filePath;
    private final List<String> severities;
    private final List<Finding> findings;
    private final String timestamp;
    private final String sourceCodeSnapshot;
    private final long fileLastModified;
    private final int updateCount;              // number of times rescanned
    private final List<Integer> appliedLines;   // line numbers of applied findings
    private final List<Integer> dismissedLines; // line numbers of dismissed findings

    public ScanRecord() {
        this.filename = "";
        this.filePath = "";
        this.severities = new ArrayList<>();
        this.findings = new ArrayList<>();
        this.timestamp = "";
        this.sourceCodeSnapshot = "";
        this.fileLastModified = 0L;
        this.updateCount = 0;
        this.appliedLines = new ArrayList<>();
        this.dismissedLines = new ArrayList<>();
    }

    // Backward compatibility
    public ScanRecord(String filename, List<String> severities, String timestamp) {
        this(filename, "", severities, timestamp, new ArrayList<>(), "", 0L, 0, new ArrayList<>(), new ArrayList<>());
    }

    public ScanRecord(String filename, String filePath, List<String> severities, String timestamp) {
        this(filename, filePath, severities, timestamp, new ArrayList<>(), "", 0L, 0, new ArrayList<>(), new ArrayList<>());
    }

    // Full constructor
    public ScanRecord(String filename, String filePath, List<String> severities, String timestamp,
                      List<Finding> findings, String sourceCodeSnapshot, long fileLastModified,
                      int updateCount, List<Integer> appliedLines, List<Integer> dismissedLines) {
        this.filename = filename;
        this.filePath = filePath;
        this.severities = severities != null ? severities : new ArrayList<>();
        this.findings = findings != null ? findings : new ArrayList<>();
        this.timestamp = timestamp;
        this.sourceCodeSnapshot = sourceCodeSnapshot != null ? sourceCodeSnapshot : "";
        this.fileLastModified = fileLastModified;
        this.updateCount = updateCount;
        this.appliedLines = appliedLines != null ? appliedLines : new ArrayList<>();
        this.dismissedLines = dismissedLines != null ? dismissedLines : new ArrayList<>();
    }

    public String getFilename() { return filename; }
    public String getFilePath() { return filePath; }
    public List<String> getSeverities() { return severities; }
    public List<Finding> getFindings() { return findings; }
    public String getTimestamp() { return timestamp; }
    public String getSourceCodeSnapshot() { return sourceCodeSnapshot; }
    public long getFileLastModified() { return fileLastModified; }
    public int getUpdateCount() { return updateCount; }
    public List<Integer> getAppliedLines() { return appliedLines; }
    public List<Integer> getDismissedLines() { return dismissedLines; }
}
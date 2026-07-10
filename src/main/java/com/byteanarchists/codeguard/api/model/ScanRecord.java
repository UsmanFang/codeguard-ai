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

    // No‑arg constructor for Gson (needed for JSON deserialization)
    public ScanRecord() {
        this.filename = "";
        this.filePath = "";
        this.severities = new ArrayList<>();
        this.findings = new ArrayList<>();
        this.timestamp = "";
        this.sourceCodeSnapshot = "";
    }

    // Main constructor (used by MainView – includes filePath but not findings/snapshot)
    public ScanRecord(String filename, String filePath, List<String> severities, String timestamp) {
        this.filename = filename;
        this.filePath = filePath;
        this.severities = severities;
        this.findings = new ArrayList<>(); // default empty
        this.timestamp = timestamp;
        this.sourceCodeSnapshot = "";       // default empty
    }

    // Full constructor (for future use when we store full findings + source)
    public ScanRecord(String filename, String filePath, List<String> severities, 
                      List<Finding> findings, String timestamp, String sourceCodeSnapshot) {
        this.filename = filename;
        this.filePath = filePath;
        this.severities = severities;
        this.findings = findings;
        this.timestamp = timestamp;
        this.sourceCodeSnapshot = sourceCodeSnapshot;
    }

    // Legacy constructor (for older history entries that don't have filePath)
    // Kept for backward compatibility – filePath will be blank.
    public ScanRecord(String filename, List<String> severities, String timestamp) {
        this(filename, "", severities, timestamp);
    }

    public String getFilename() { return filename; }
    public String getFilePath() { return filePath; }
    public List<String> getSeverities() { return severities; }
    public List<Finding> getFindings() { return findings; }
    public String getTimestamp() { return timestamp; }
    public String getSourceCodeSnapshot() { return sourceCodeSnapshot; }
}
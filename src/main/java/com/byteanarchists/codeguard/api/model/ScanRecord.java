package com.byteanarchists.codeguard.api.model;

import java.util.List;
import java.util.ArrayList;

public class ScanRecord {
    private final String filename;
    private final List<String> severities;
    private final List<Finding> findings; // Added: Stores the full findings objects
    private final String timestamp;
    private final String sourceCodeSnapshot;

    // Default constructor for GSON/JSON persistence
    public ScanRecord() {
        this.filename = "";
        this.severities = new ArrayList<>();
        this.findings = new ArrayList<>();
        this.timestamp = "";
        this.sourceCodeSnapshot = "";
    }    

    // Updated Constructor
    public ScanRecord(String filename, List<String> severities, List<Finding> findings, String timestamp, String sourceCodeSnapshot) {
        this.filename = filename;
        this.severities = severities;
        this.findings = findings;
        this.timestamp = timestamp;
        this.sourceCodeSnapshot = sourceCodeSnapshot;
    }

    public String getFilename() { return filename; }
    public List<String> getSeverities() { return severities; }
    public List<Finding> getFindings() { return findings; } // Added Getter
    public String getTimestamp() { return timestamp; }
    public String getSourceCodeSnapshot() { return sourceCodeSnapshot; }
}
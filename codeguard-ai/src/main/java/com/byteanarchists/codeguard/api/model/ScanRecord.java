//{ fileName, timestamp, findingSummary } for History
package com.byteanarchists.codeguard.api.model;

import java.util.List;
import java.util.ArrayList;

public class ScanRecord {
    private final String filename;
    private final List<String> severities;
    private final String timestamp;

public ScanRecord() {
    this.filename = "";
    this.severities = new ArrayList<>();
    this.timestamp = "";
}    
    public ScanRecord(String filename, List<String> severities, String timestamp) {
        this.filename = filename;
        this.severities = severities;
        this.timestamp = timestamp;
    }

    public String getFilename() { return filename; }
    public List<String> getSeverities() { return severities; }
    public String getTimestamp() { return timestamp; }
}
//{ fileName, timestamp, findingSummary } for History
package com.byteanarchists.codeguard.api.model;

import java.util.List;
import java.util.ArrayList;

public class ScanRecord {
    private final String filename;
    private final String filePath;
    private final List<String> severities;
    private final String timestamp;

public ScanRecord() {
    this.filename = "";
    this.filePath = "";
    this.severities = new ArrayList<>();
    this.timestamp = "";
}    
    // Kept for backward compatibility. filePath is left blank, which
    // HistoryView treats as "can't click to reopen".
    public ScanRecord(String filename, List<String> severities, String timestamp) {
        this(filename, "", severities, timestamp);
    }

    // filePath is the full absolute path on disk, used by HistoryView to let
    // the user click a history card and reopen that exact file. Older history
    // entries saved before this field existed will deserialize filePath as
    // null via Gson - callers must treat null the same as blank.
    public ScanRecord(String filename, String filePath, List<String> severities, String timestamp) {
        this.filename = filename;
        this.filePath = filePath;
        this.severities = severities;
        this.timestamp = timestamp;
    }

    public String getFilename() { return filename; }
    public String getFilePath() { return filePath; }
    public List<String> getSeverities() { return severities; }
    public String getTimestamp() { return timestamp; }
}
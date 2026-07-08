//{ severity, title, description, fixSnippet, line }
package com.byteanarchists.codeguard.api.model;

public class Finding {
    private final String severity; // CRITICAL, HIGH, INFO
    private final String title;
    private final String description;
    private final String fixSnippet;
    private final int lineNumber;

    public Finding(String severity, String title, String description, String fixSnippet, int lineNumber) {
        this.severity = severity;
        this.title = title;
        this.description = description;
        this.fixSnippet = fixSnippet;
        this.lineNumber = lineNumber;
    }

    public String getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFixSnippet() { return fixSnippet; }
    public int getLineNumber() { return lineNumber; }
}
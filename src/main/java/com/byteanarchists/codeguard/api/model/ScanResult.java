//{ String issuesText, String fixedCode }
package com.byteanarchists.codeguard.api.model;

import java.util.List;

public class ScanResult {
    private final boolean success;
    private final List<Finding> findings;
    private final String errorMessage;

    public ScanResult(List<Finding> findings) {
        this.success = true;
        this.findings = findings;
        this.errorMessage = null;
    }

    public ScanResult(String errorMessage) {
        this.success = false;
        this.findings = List.of();
        this.errorMessage = errorMessage;
    }

    public ScanResult(String message, List<Finding> findings) {
        this.success = message.equals("SUCCESS");
        // Never let findings be null - error paths were passing null here,
        // which caused a NullPointerException later when callers did
        // result.getFindings().stream()/.size() regardless of success/failure.
        this.findings = (findings != null) ? findings : List.of();
        this.errorMessage = this.success ? null : message;
    }

    public boolean isSuccess() { return success; }
    public List<Finding> getFindings() { return findings; }
    public String getErrorMessage() { return errorMessage; }
}
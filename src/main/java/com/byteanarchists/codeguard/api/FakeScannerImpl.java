//Member 1 writes this NOW to test UI standalone
package com.byteanarchists.codeguard.api;

import com.byteanarchists.codeguard.api.model.Finding;
import com.byteanarchists.codeguard.api.model.ScanResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FakeScannerImpl implements ScannerService {
    @Override
    public CompletableFuture<ScanResult> runScanAsync(String fileContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Simulate Fireworks API round-trip latency
                Thread.sleep(2200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<Finding> simulatedFindings = new ArrayList<>();
            simulatedFindings.add(new Finding(
                "CRITICAL",
                "Hardcoded Credentials Exposed",
                "A hardcoded authentication token or private key string was found assigned directly within source assignments.",
                "private static final String API_KEY = System.getenv(\"FIREWORKS_API_KEY\");",
                12
            ));
            simulatedFindings.add(new Finding(
                "HIGH",
                "SQL Injection Vulnerability via String Concat",
                "User input values are appended raw directly to a statement runner without parameter initialization escaping definitions.",
                "String query = \"SELECT * FROM users WHERE id = ?\";\nPreparedStatement stmt = conn.prepareStatement(query);\nstmt.setString(1, inputId);",
                45
            ));
            simulatedFindings.add(new Finding(
                "INFO",
                "Redundant Debug Logging Flag Active",
                "Production runtimes contain explicit system console traces emitting detailed contextual memory arrays.",
                "// Remove or pass to structured logging layer framework\nlogger.info(\"Runtime process executed successfully.\");",
                89
            ));

            return new ScanResult(simulatedFindings);
        });
    }
}
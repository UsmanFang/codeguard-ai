//interface: scan(String code) -> ScanResult
package com.byteanarchists.codeguard.api;

import com.byteanarchists.codeguard.api.model.ScanResult;
import java.util.concurrent.CompletableFuture;

public interface ScannerService {
    CompletableFuture<ScanResult> runScanAsync(String fileContent);
}
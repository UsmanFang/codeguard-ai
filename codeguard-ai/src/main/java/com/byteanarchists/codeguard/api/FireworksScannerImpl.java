package com.byteanarchists.codeguard.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.byteanarchists.codeguard.api.model.ScanResult;
import com.byteanarchists.codeguard.prompt.SystemPrompts;
import com.byteanarchists.codeguard.util.ResponseParser;

public class FireworksScannerImpl implements ScannerService {

    private static final String API_URL = "https://api.fireworks.ai/inference/v1/chat/completions";
    // Model is no longer hardcoded here - it's resolved from ModelPreferenceStore,
    // which is what the Settings screen's model dropdown actually writes to.
    // Defaults to llama-v3p3-70b-instruct: current-generation model with a 128k
    // context window (the old "llama-v3-70b-instruct" tag only supports ~8.2k
    // tokens, which truncates on anything but a very small file).

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public CompletableFuture<ScanResult> runScanAsync(String fileContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String apiKey = ApiKeyStore.resolveApiKey();
                if (apiKey == null || apiKey.isEmpty()) {
                    return new ScanResult(
                        "ERROR: No Fireworks API key found. Add it in Settings, or set FIREWORKS_API_KEY " +
                        "as an environment variable / in a .env file.",
                        List.of());
                }

                String jsonBody = buildJsonPayload(fileContent);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .timeout(Duration.ofSeconds(60))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    return new ScanResult("API error: " + response.statusCode() + " - " + response.body(), List.of());
                }

                var findings = ResponseParser.parseJsonResponse(response.body());
                return new ScanResult("SUCCESS", findings);

            } catch (ResponseParser.ParseException pe) {
                return new ScanResult("Could not parse the AI's response as JSON: " + pe.getMessage(), List.of());
            } catch (Exception e) {
                return new ScanResult("Network error: " + e.getMessage(), List.of());
            }
        });
    }

    private String buildJsonPayload(String code) {
        String systemPrompt = SystemPrompts.CYBER_AUDITOR_PROMPT;

        // Escape JSON strings properly
        String escapedSystem = escapeJson(systemPrompt);
        String escapedCode = escapeJson(code);

        // response_format json_object forces Fireworks to return valid JSON
        // and stops most models from wrapping the array in ```json fences,
        // even though the system prompt already asks for that.
        return String.format("""
                {
                  "model": "%s",
                  "max_tokens": 4096,
                  "temperature": 0.2,
                  "response_format": {"type": "json_object"},
                  "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                  ]
                }
                """, ModelPreferenceStore.resolveModelPath(), escapedSystem, escapedCode);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
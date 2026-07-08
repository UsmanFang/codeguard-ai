package com.byteanarchists.codeguard.util;

import java.lang.reflect.Type;
import java.util.List;

import com.byteanarchists.codeguard.api.model.Finding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ResponseParser {

    private static final Gson gson = new Gson();

    /**
     * rawJson here is the FULL Fireworks /chat/completions HTTP response body, i.e.
     * {"id": ..., "choices": [ {"message": {"content": "...model's text..."}} ], ...}
     * The model's actual answer - the JSON array of findings - lives inside
     * choices[0].message.content as a *string*, and may still be wrapped in
     * ```json ... ``` fences even when the system prompt asks it not to be.
     * The previous version of this parser tried to treat rawJson itself as the
     * findings array/object, which meant it silently failed on every real API
     * response and always returned an empty list.
     */
    public static List<Finding> parseJsonResponse(String rawJson) {
        String modelContent = extractModelContent(rawJson);
        String cleaned = stripMarkdownFences(modelContent);

        try {
            JsonElement root = JsonParser.parseString(cleaned);

            if (root.isJsonArray()) {
                return parseArray(root.getAsJsonArray());
            }

            if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                // Model may have wrapped the array under a key like "findings" or
                // "vulnerabilities" despite instructions not to.
                for (String key : new String[]{"findings", "vulnerabilities", "results", "issues"}) {
                    if (obj.has(key) && obj.get(key).isJsonArray()) {
                        return parseArray(obj.getAsJsonArray(key));
                    }
                }
                // Or maybe the whole object is a single finding.
                if (obj.has("severity") && obj.has("title")) {
                    JsonArray wrapper = new JsonArray();
                    wrapper.add(obj);
                    return parseArray(wrapper);
                }
            }

            throw new ParseException("Unrecognized JSON shape from model: " + truncate(cleaned));

        } catch (com.google.gson.JsonSyntaxException e) {
            throw new ParseException("Model did not return valid JSON: " + truncate(cleaned));
        }
    }

    private static List<Finding> parseArray(JsonArray array) {
        Type listType = new TypeToken<List<Finding>>(){}.getType();
        List<Finding> findings = gson.fromJson(array, listType);
        return (findings != null) ? findings : List.of();
    }

    /** Pulls choices[0].message.content out of the full chat-completion response. */
    private static String extractModelContent(String rawJson) {
        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new ParseException("Fireworks response had no 'choices': " + truncate(rawJson));
            }
            JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
            if (message == null || !message.has("content") || message.get("content").isJsonNull()) {
                throw new ParseException("Fireworks response had no message content: " + truncate(rawJson));
            }
            return message.get("content").getAsString();
        } catch (IllegalStateException | com.google.gson.JsonSyntaxException e) {
            // rawJson wasn't even a well-formed chat-completion envelope. Fall back to
            // treating rawJson itself as the model content, in case the caller ever
            // passes in raw model output directly (e.g. tests, or FakeScannerImpl-style stubs).
            return rawJson;
        }
    }

    /** Strips ```json ... ``` or ``` ... ``` fences the model may add despite instructions. */
    private static String stripMarkdownFences(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence != -1) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private static String truncate(String s) {
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }

    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}

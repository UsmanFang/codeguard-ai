// Resolves/persists which Fireworks model FireworksScannerImpl should call.
//
// PREVIOUS BUG #1: the "AI Core Inference Engine" dropdown in SettingsView let you
// pick a model, but FireworksScannerImpl always called a hardcoded MODEL constant -
// the dropdown was pure decoration and never affected which model actually ran a scan.
//
// PREVIOUS BUG #2: the three models originally offered in the dropdown
// (llama-v3p3-70b-instruct, llama-v3p1-8b-instruct, mixtral-8x22b-instruct) have
// since been pulled from Fireworks' serverless tier - their model pages now show
// "Serverless: Not supported" (only available via paid on-demand GPU deployment),
// which is exactly what produced the "404 Model not found, inaccessible, and/or
// not deployed" error. Swapped in gpt-oss-120b / gpt-oss-20b, both confirmed
// "Serverless: Supported" with live per-token pricing as of this writing. If
// Fireworks changes availability again, check a model's own page at
// https://fireworks.ai/models/fireworks/<name> - the "Supported Functionality"
// section says whether "Serverless" is Supported or Not supported.
//
// This mirrors the ApiKeyStore pattern: SettingsView calls saveModel() when the
// user picks a model, FireworksScannerImpl calls resolveModel() to know which one
// to send. Persisted to the same ~/.codeguard/config.properties file as the API key.
package com.byteanarchists.codeguard.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class ModelPreferenceStore {

    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".codeguard");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");
    private static final String PROPERTY_KEY = "FIREWORKS_MODEL";

    public static final String DEFAULT_MODEL_ID = "gpt-oss-120b";

    // Maps the short model names shown in the Settings dropdown to the full
    // "accounts/fireworks/models/<name>" path the Fireworks API expects.
    private static final Map<String, String> MODEL_PATHS = Map.of(
            "gpt-oss-120b", "accounts/fireworks/models/gpt-oss-120b",
            "gpt-oss-20b", "accounts/fireworks/models/gpt-oss-20b"
    );

    /** Returns the full "accounts/fireworks/models/..." path to send to the API. */
    public static String resolveModelPath() {
        String shortName = resolveModelShortName();
        return MODEL_PATHS.getOrDefault(shortName, MODEL_PATHS.get(DEFAULT_MODEL_ID));
    }

    /** Returns the short model name (as shown in the Settings dropdown). */
    public static String resolveModelShortName() {
        if (!Files.exists(CONFIG_FILE)) return DEFAULT_MODEL_ID;
        try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
            Properties props = new Properties();
            props.load(in);
            String saved = props.getProperty(PROPERTY_KEY);
            return (saved != null && MODEL_PATHS.containsKey(saved)) ? saved : DEFAULT_MODEL_ID;
        } catch (IOException e) {
            return DEFAULT_MODEL_ID;
        }
    }

    /** Persists the chosen short model name so it's remembered next launch. */
    public static void saveModel(String shortName) {
        if (shortName == null || !MODEL_PATHS.containsKey(shortName)) return;
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            if (Files.exists(CONFIG_FILE)) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
                    props.load(in);
                }
            }
            props.setProperty(PROPERTY_KEY, shortName);
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE.toFile())) {
                props.store(out, "CodeGuard AI local settings - do not commit this file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Resolves/persists which Fireworks model FireworksScannerImpl should call.
//
// PREVIOUS BUG #1: the "AI Core Inference Engine" dropdown in SettingsView let you
// pick a model, but FireworksScannerImpl always called a hardcoded MODEL constant -
// the dropdown was pure decoration and never affected which model actually ran a scan.
//
// PREVIOUS BUG #2 (now historical): the original three dropdown models
// (llama-v3p3-70b-instruct, llama-v3p1-8b-instruct, mixtral-8x22b-instruct) were
// pulled from Fireworks' serverless tier, causing 404 "Model not found" errors.
// A later revision briefly used gpt-oss-120b / gpt-oss-20b instead.
//
// CURRENT MODELS: kimi-k2p7-code (Moonshot AI's coding-focused agentic model,
// 262k context) and deepseek-v4-pro (DeepSeek's flagship reasoning/coding model,
// 1M context) - both verified "Serverless: Supported" + "Function Calling:
// Supported" on their live Fireworks model pages. If you ever see a 404 again,
// check the model's own page at https://fireworks.ai/models/<provider>/<name> -
// the "Supported Functionality" table says whether "Serverless" is Supported or
// Not supported; swap in a Supported one the same way these were swapped in.
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

    // Default is the coding-focused model, since this app's whole job is scanning code.
    public static final String DEFAULT_MODEL_ID = "kimi-k2p7-code";

    // Maps the short model names shown in the Settings dropdown to the full
    // "accounts/fireworks/models/<name>" path the Fireworks API expects.
    private static final Map<String, String> MODEL_PATHS = Map.of(
            "kimi-k2p7-code", "accounts/fireworks/models/kimi-k2p7-code",
            "deepseek-v4-pro", "accounts/fireworks/models/deepseek-v4-pro"
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

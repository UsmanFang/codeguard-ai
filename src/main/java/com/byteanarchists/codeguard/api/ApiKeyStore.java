// Resolves the Fireworks API key from (in priority order):
//   1. FIREWORKS_API_KEY system environment variable
//   2. A ".env" file in the project's working directory (FIREWORKS_API_KEY=...)
//   3. A saved key in ~/.codeguard/config.properties (written by SettingsView)
// This exists because the original code only checked System.getenv(), which
// meant the app was unusable unless you manually exported an environment
// variable in the exact shell you launched it from - painful in an IDE.
package com.byteanarchists.codeguard.api;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ApiKeyStore {

    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".codeguard");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");
    private static final File ENV_FILE = new File(".env");

    public static String resolveApiKey() {
        String fromEnv = System.getenv("FIREWORKS_API_KEY");
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }

        String fromDotEnv = readDotEnv();
        if (fromDotEnv != null && !fromDotEnv.isBlank()) {
            return fromDotEnv.trim();
        }

        String fromSavedConfig = readSavedConfig();
        if (fromSavedConfig != null && !fromSavedConfig.isBlank()) {
            return fromSavedConfig.trim();
        }

        return null;
    }

    /** Persists the key so the app remembers it next launch (used by the Settings screen). */
    public static void saveApiKey(String apiKey) {
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            if (Files.exists(CONFIG_FILE)) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
                    props.load(in);
                }
            }
            props.setProperty("FIREWORKS_API_KEY", apiKey.trim());
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE.toFile())) {
                props.store(out, "CodeGuard AI local settings - do not commit this file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readDotEnv() {
        if (!ENV_FILE.exists()) return null;
        try {
            for (String line : Files.readAllLines(ENV_FILE.toPath())) {
                String trimmed = line.trim();
                if (trimmed.startsWith("FIREWORKS_API_KEY=")) {
                    return trimmed.substring("FIREWORKS_API_KEY=".length())
                            .replaceAll("^[\"']|[\"']$", "");
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    private static String readSavedConfig() {
        if (!Files.exists(CONFIG_FILE)) return null;
        try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("FIREWORKS_API_KEY");
        } catch (IOException e) {
            return null;
        }
    }
}

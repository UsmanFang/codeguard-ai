// Resolves the Fireworks API key from (in priority order):
//   1. FIREWORKS_API_KEY system environment variable
//   2. A ".env" file next to the running jar/exe (most reliable for a packaged app)
//   3. A ".env" file in the current working directory (fallback, e.g. IDE runs)
//   4. A saved key in ~/.codeguard/config.properties (written by SettingsView)
// This exists because the original code only checked System.getenv(), which
// meant the app was unusable unless you manually exported an environment
// variable in the exact shell you launched it from - painful in an IDE, and
// unworkable for a packaged .exe a judge just double-clicks with no shell at all.
package com.byteanarchists.codeguard.api;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URISyntaxException;
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
        // Preferred: the folder containing the actual running jar/exe. This is what
        // makes a packaged app-image work regardless of how it was launched (double-click,
        // pinned shortcut, etc.) - it doesn't depend on the JVM's working directory at all.
        File nextToJar = resolveEnvFileNextToRunningJar();
        if (nextToJar != null && nextToJar.exists()) {
            String fromJarDir = parseDotEnvFile(nextToJar);
            if (fromJarDir != null) return fromJarDir;
        }

        // Fallback: current working directory (covers IDE runs / mvn javafx:run).
        if (ENV_FILE.exists()) {
            return parseDotEnvFile(ENV_FILE);
        }

        return null;
    }

    private static File resolveEnvFileNextToRunningJar() {
        try {
            File codeSourceLocation = new File(
                ApiKeyStore.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            File parentDir = codeSourceLocation.isFile()
                ? codeSourceLocation.getParentFile()
                : codeSourceLocation;
            return (parentDir != null) ? new File(parentDir, ".env") : null;
        } catch (URISyntaxException | NullPointerException | SecurityException e) {
            // Running from an IDE / exploded classes / restricted environment -
            // fall back to the working-directory check instead of failing.
            return null;
        }
    }

    private static String parseDotEnvFile(File envFile) {
        try {
            for (String line : Files.readAllLines(envFile.toPath())) {
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

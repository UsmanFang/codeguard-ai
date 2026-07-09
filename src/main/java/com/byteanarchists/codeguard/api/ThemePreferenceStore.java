// Resolves/persists which AtlantaFX theme the app should launch with.
//
// PREVIOUS BUG: SettingsView's theme pills applied the chosen theme live via
// Application.setUserAgentStylesheet(...), but never saved that choice
// anywhere - so every relaunch silently reset back to Dracula.
package main.java.com.byteanarchists.codeguard.api;

import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.PrimerDark;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

public class ThemePreferenceStore {

    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".codeguard");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");
    private static final String PROPERTY_KEY = "SELECTED_THEME";

    public static final String DRACULA = "Dracula";
    public static final String NORD_DARK = "NordDark";
    public static final String PRIMER_DARK = "PrimerDark";
    public static final String DEFAULT_THEME = DRACULA;

    private static final Set<String> KNOWN_THEMES = Set.of(DRACULA, NORD_DARK, PRIMER_DARK);

    public static String resolveThemeName() {
        if (!Files.exists(CONFIG_FILE)) return DEFAULT_THEME;
        try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
            Properties props = new Properties();
            props.load(in);
            String saved = props.getProperty(PROPERTY_KEY);
            return (saved != null && KNOWN_THEMES.contains(saved)) ? saved : DEFAULT_THEME;
        } catch (IOException e) {
            return DEFAULT_THEME;
        }
    }

    public static String resolveStylesheet() {
        return stylesheetFor(resolveThemeName());
    }

    public static String stylesheetFor(String themeName) {
        return switch (themeName) {
            case NORD_DARK -> new NordDark().getUserAgentStylesheet();
            case PRIMER_DARK -> new PrimerDark().getUserAgentStylesheet();
            default -> new Dracula().getUserAgentStylesheet();
        };
    }

    public static void saveTheme(String themeName) {
        if (themeName == null || !KNOWN_THEMES.contains(themeName)) return;
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            if (Files.exists(CONFIG_FILE)) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
                    props.load(in);
                }
            }
            props.setProperty(PROPERTY_KEY, themeName);
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE.toFile())) {
                props.store(out, "CodeGuard AI local settings - do not commit this file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
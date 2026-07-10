// Remembers the last folder the user opened/saved a file from, so the
// "Open Target File" and "Save As" dialogs start there next time instead of
// always defaulting to the OS's default directory.
//
// Follows the exact same persistence pattern as ApiKeyStore and
// ModelPreferenceStore: one shared properties file at
// ~/.codeguard/config.properties, keyed by a unique property name.
package com.byteanarchists.codeguard.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class RecentDirectoryStore {

    private static final Path CONFIG_DIR = Path.of(System.getProperty("user.home"), ".codeguard");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.properties");
    private static final String PROPERTY_KEY = "LAST_OPENED_DIR";

    public static File resolveLastDirectory() {
        if (!Files.exists(CONFIG_FILE)) return null;
        try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
            Properties props = new Properties();
            props.load(in);
            String saved = props.getProperty(PROPERTY_KEY);
            if (saved == null || saved.isBlank()) return null;
            File dir = new File(saved);
            return (dir.exists() && dir.isDirectory()) ? dir : null;
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveLastDirectory(File file) {
        if (file == null) return;
        File dir = file.isDirectory() ? file : file.getParentFile();
        if (dir == null) return;
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            if (Files.exists(CONFIG_FILE)) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE.toFile())) {
                    props.load(in);
                }
            }
            props.setProperty(PROPERTY_KEY, dir.getAbsolutePath());
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE.toFile())) {
                props.store(out, "CodeGuard AI local settings - do not commit this file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
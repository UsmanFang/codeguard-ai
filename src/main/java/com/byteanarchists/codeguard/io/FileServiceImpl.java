package com.byteanarchists.codeguard.io;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileReader;
import java.io.FileWriter;

public class FileServiceImpl implements FileService {

    @Override
    public String readFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    @Override
    public File pickFile(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Source Code File");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Java Files", "*.java")
        );

        String lastDir = getLastDirectory();
        if (lastDir != null) {
            File dir = new File(lastDir);
            if (dir.exists() && dir.isDirectory()) {
                chooser.setInitialDirectory(dir);
            }
        }

        File selected = chooser.showOpenDialog(ownerWindow);
        if (selected != null) {
            saveLastDirectory(selected.getParent());
        }
        return selected;
    }

    @Override
    public void saveFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content);
    }

    private String getLastDirectory() {
        File dirFile = new File(".lastdir");
        if (dirFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dirFile))) {
                return reader.readLine();
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    private void saveLastDirectory(String path) {
        try (FileWriter writer = new FileWriter(".lastdir")) {
            writer.write(path);
        } catch (IOException e) {
            // ignore
        }
    }
}
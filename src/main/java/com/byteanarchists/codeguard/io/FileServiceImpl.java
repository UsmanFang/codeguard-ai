package com.byteanarchists.codeguard.io;

import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileServiceImpl implements FileService {

    @Override
    public String readFile(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("Target target source code file is missing or invalid.");
        }
        return Files.readString(file.toPath());
    }

    @Override
    public File pickFile(Window ownerWindow) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open Target Code File for Audit");
        
        // Match multi-extension targets configured for V2 expansion
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Supported Source Code (*.java, *.py, *.js)", "*.java", "*.py", "*.js"),
            new FileChooser.ExtensionFilter("Java Source Files (*.java)", "*.java"),
            new FileChooser.ExtensionFilter("Python Source Files (*.py)", "*.py"),
            new FileChooser.ExtensionFilter("JavaScript Source Files (*.js)", "*.js"),
            new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );

        // Remember last folder: start the dialog in whatever directory the
        // user opened a file from last time, instead of the OS default.
        File lastDir = com.byteanarchists.codeguard.io.RecentDirectoryStore.resolveLastDirectory();
        if (lastDir != null) {
            chooser.setInitialDirectory(lastDir);
        }

        File selected = chooser.showOpenDialog(ownerWindow);
        if (selected != null) {
            com.byteanarchists.codeguard.io.RecentDirectoryStore.saveLastDirectory(selected);
        }
        return selected;
    }

    @Override
    public void saveFile(File file, String content) throws IOException {
        if (file == null) {
            throw new IOException("Cannot write file output: specified destination path block is empty.");
        }
        Files.writeString(file.toPath(), content);
    }

    @Override
    public File pickSaveLocation(Window ownerWindow, String suggestedName) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save As");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Java Files", "*.java")
        );
        chooser.setInitialFileName(suggestedName);

        File lastDir = com.byteanarchists.codeguard.io.RecentDirectoryStore.resolveLastDirectory();
        if (lastDir != null) {
            chooser.setInitialDirectory(lastDir);
        }

        File selected = chooser.showSaveDialog(ownerWindow);
        if (selected != null) {
            com.byteanarchists.codeguard.io.RecentDirectoryStore.saveLastDirectory(selected);
        }
        return selected;
    }
}
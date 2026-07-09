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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Target Code File for Audit");
        
        // Match multi-extension targets configured for V2 expansion
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Supported Source Code (*.java, *.py, *.js)", "*.java", "*.py", "*.js"),
            new FileChooser.ExtensionFilter("Java Source Files (*.java)", "*.java"),
            new FileChooser.ExtensionFilter("Python Source Files (*.py)", "*.py"),
            new FileChooser.ExtensionFilter("JavaScript Source Files (*.js)", "*.js"),
            new FileChooser.ExtensionFilter("All Files (*.*)", "*.*")
        );
        
        return fileChooser.showOpenDialog(ownerWindow);
    }

    @Override
    public void saveFile(File file, String content) throws IOException {
        if (file == null) {
            throw new IOException("Cannot write file output: specified destination path block is empty.");
        }
        Files.writeString(file.toPath(), content);
    }
}
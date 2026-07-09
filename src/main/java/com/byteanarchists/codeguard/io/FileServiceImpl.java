package com.byteanarchists.codeguard.io;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        // You can add more extensions later: *.py, *.js, *.cpp
        return chooser.showOpenDialog(ownerWindow);
    }

    @Override
    public void saveFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content);
    }
}
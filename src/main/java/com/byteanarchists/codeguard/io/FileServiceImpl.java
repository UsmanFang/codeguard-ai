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
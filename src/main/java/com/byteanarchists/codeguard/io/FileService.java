package com.byteanarchists.codeguard.io;

import javafx.stage.Window;
import java.io.File;
import java.io.IOException;

public interface FileService {
    // Already present
    String readFile(File file) throws IOException;

    // New methods you need to add
    File pickFile(Window ownerWindow);
    void saveFile(File file, String content) throws IOException;

    // "Save As" support: opens a save dialog pre-filled with suggestedName,
    // starting in the last-used directory. Returns null if the user cancels.
    File pickSaveLocation(Window ownerWindow, String suggestedName);

}
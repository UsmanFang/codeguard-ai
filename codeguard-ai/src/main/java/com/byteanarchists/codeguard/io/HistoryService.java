package com.byteanarchists.codeguard.io;

import com.byteanarchists.codeguard.api.model.ScanRecord;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {
    private static final File HISTORY_FILE = new File("scan_history.json");
    private static final Gson gson = new Gson();

    public List<ScanRecord> loadHistory() {
        if (!HISTORY_FILE.exists()) return new ArrayList<>();
        try {
            String json = Files.readString(HISTORY_FILE.toPath());
            if (json.isBlank()) return new ArrayList<>();
            Type listType = new TypeToken<List<ScanRecord>>(){}.getType();
            List<ScanRecord> parsed = gson.fromJson(json, listType);
            return (parsed != null) ? parsed : new ArrayList<>();
        } catch (IOException | com.google.gson.JsonSyntaxException e) {
            // Corrupt or unreadable history file - don't crash the app over old scan logs.
            return new ArrayList<>();
        }
    }

    public void saveRecord(ScanRecord record) {
        List<ScanRecord> list = loadHistory();
        list.add(record);
        try {
            Files.writeString(HISTORY_FILE.toPath(), gson.toJson(list));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
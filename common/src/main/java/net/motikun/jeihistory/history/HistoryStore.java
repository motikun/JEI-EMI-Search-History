package net.motikun.jeihistory.history;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** All calls run on the client thread. Failed writes stay dirty until a retry succeeds. */
public final class HistoryStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryStore.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final int FORMAT_VERSION = 2;

    private final Path file;
    private final SearchHistory history = new SearchHistory();
    private final java.util.Set<String> deletedQueryHashes = new java.util.TreeSet<>();
    private String legacyContent;
    private boolean dirty;
    private boolean writeFailureLogged;
    private boolean writable = true;

    public HistoryStore(Path file) {
        this.file = file;
        load();
    }

    public List<String> entries() {
        return history.entries();
    }

    public void record(String text) {
        record(text, CommitIntent.EXPLICIT);
    }

    public void record(String text, CommitIntent intent) {
        if (text != null && !text.isBlank()) {
            String hash = hash(text);
            if (intent != CommitIntent.AUTOMATIC) dirty |= deletedQueryHashes.remove(hash);
            if (!deletedQueryHashes.contains(hash)) dirty |= history.add(text);
        }
        flush();
    }

    public boolean delete(String text) {
        if (!history.remove(text)) return false;
        deletedQueryHashes.add(hash(text));
        dirty = true;
        flush();
        return true;
    }

    private static String hash(String text) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                    .digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void flush() {
        if (!dirty || !writable) {
            return;
        }
        Path temporary = null;
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            if (legacyContent != null) {
                Path backup = Files.createTempFile(file.toAbsolutePath().getParent(), "history.json.v1-", ".bak");
                Files.writeString(backup, legacyContent, StandardCharsets.UTF_8);
                legacyContent = null;
            }
            temporary = Files.createTempFile(file.toAbsolutePath().getParent(), "history-", ".tmp");
            JsonObject document = new JsonObject();
            document.addProperty("version", FORMAT_VERSION);
            document.add("entries", GSON.toJsonTree(history.entries()));
            document.add("deletedQueryHashes", GSON.toJsonTree(deletedQueryHashes));
            Files.writeString(temporary, GSON.toJson(document) + "\n", StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
            dirty = false;
            writeFailureLogged = false;
        } catch (IOException e) {
            if (!writeFailureLogged) {
                LOGGER.warn("Could not save JEI search history to {}; will retry", file, e);
                writeFailureLogged = true;
            }
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException e) {
                    LOGGER.debug("Could not remove temporary history file {}", temporary, e);
                }
            }
        }
    }

    private void load() {
        if (Files.notExists(file)) {
            return;
        }
        String content;
        try {
            content = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // An unreadable file may contain valid history. Never overwrite it with an empty list.
            writable = false;
            LOGGER.warn("Could not read JEI search history {}; preserving it for this session", file, e);
            return;
        }
        try {
            JsonObject document = JsonParser.parseString(content).getAsJsonObject();
            JsonElement version = document.get("version");
            if (version == null || !version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber()
                    || !(version.getAsString().equals("1") || version.getAsString().equals("2"))) {
                throw new IllegalArgumentException("Unsupported history format version");
            }
            List<String> loaded = new ArrayList<>();
            for (JsonElement entry : document.getAsJsonArray("entries")) {
                if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException("History entry is not a string");
                }
                loaded.add(entry.getAsString());
            }
            var hashes = new java.util.TreeSet<String>();
            if (version.getAsInt() == 2) {
                for (JsonElement entry : document.getAsJsonArray("deletedQueryHashes")) {
                    if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()
                            || !entry.getAsString().matches("[0-9a-f]{64}")) {
                        throw new IllegalArgumentException("Invalid deleted query hash");
                    }
                    hashes.add(entry.getAsString());
                }
            }
            deletedQueryHashes.addAll(hashes);
            if (version.getAsInt() == 1) legacyContent = content;
            for (int i = loaded.size() - 1; i >= 0; i--) {
                if (!deletedQueryHashes.contains(hash(loaded.get(i)))) history.add(loaded.get(i));
            }
        } catch (RuntimeException e) {
            LOGGER.warn("Invalid JEI search history {}; moving it aside", file, e);
            try {
                Path backup = Files.createTempFile(file.toAbsolutePath().getParent(), "history.json.corrupt-", ".bak");
                Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException backupError) {
                writable = false;
                LOGGER.warn("Could not back up invalid history {}; will not overwrite it", file, backupError);
            }
        }
    }
}

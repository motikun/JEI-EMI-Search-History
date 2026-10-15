package net.motikun.jeihistory.history;

import java.util.ArrayList;
import java.util.List;

/** Most recently used, exact-match search strings. No search syntax is normalized. */
public final class SearchHistory {
    public static final int LIMIT = 100;
    private final List<String> entries = new ArrayList<>();

    public boolean add(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (!entries.isEmpty() && entries.get(0).equals(text)) {
            return false;
        }
        entries.remove(text);
        entries.add(0, text);
        if (entries.size() > LIMIT) {
            entries.remove(entries.size() - 1);
        }
        return true;
    }

    public boolean remove(String text) {
        return entries.remove(text);
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}

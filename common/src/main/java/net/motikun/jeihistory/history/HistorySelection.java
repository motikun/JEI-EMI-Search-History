package net.motikun.jeihistory.history;

import java.util.List;
import java.util.Optional;

/** A stable snapshot while editing; navigation never changes the search text. */
public final class HistorySelection {
    private List<String> entries = List.of();
    private int selected = -1;
    private int offset;
    private int visibleRows = 8;

    public void open(List<String> entries) {
        this.entries = List.copyOf(entries);
        selected = -1;
        offset = 0;
    }

    public void remove(String text) {
        int index = entries.indexOf(text);
        if (index < 0) return;
        boolean selectedRemoved = selected == index;
        var updated = new java.util.ArrayList<>(entries);
        updated.remove(index);
        entries = List.copyOf(updated);
        if (entries.isEmpty()) selected = -1;
        else if (selected == index) selected = Math.min(index, entries.size() - 1);
        else if (selected > index) selected--;
        offset = Math.min(offset, maxOffset());
        if (selectedRemoved) ensureSelectedVisible();
    }

    public void clearSelection() {
        selected = -1;
    }

    public void setVisibleRows(int rows) {
        int previousRows = visibleRows;
        visibleRows = Math.max(1, rows);
        offset = Math.min(offset, maxOffset());
        if (previousRows != visibleRows) {
            ensureSelectedVisible();
        }
    }

    public void move(int delta) {
        if (entries.isEmpty()) {
            return;
        }
        selected = selected < 0 ? 0 : Math.max(0, Math.min(selected + delta, entries.size() - 1));
        ensureSelectedVisible();
    }

    public void scroll(int delta) {
        offset = Math.max(0, Math.min(offset + delta, maxOffset()));
    }

    public Optional<String> selectedEntry() {
        return selected < 0 ? Optional.empty() : Optional.of(entries.get(selected));
    }

    public List<String> entries() {
        return entries;
    }

    public int selected() {
        return selected;
    }

    public int offset() {
        return offset;
    }

    private int maxOffset() {
        return Math.max(0, entries.size() - visibleRows);
    }

    private void ensureSelectedVisible() {
        if (selected >= 0) {
            if (selected < offset) {
                offset = selected;
            } else if (selected >= offset + visibleRows) {
                offset = selected - visibleRows + 1;
            }
        }
    }
}

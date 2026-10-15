package net.motikun.jeihistory.history;

public record HistoryPanelBounds(int x, int y, int width, int height, int rows) {
    public static final int ROW_HEIGHT = 14;
    public static final int PADDING = 4;
    private static final int GAP = 2;
    public static final HistoryPanelBounds EMPTY = new HistoryPanelBounds(0, 0, 0, 0, 0);

    public static HistoryPanelBounds calculate(int searchX, int searchY, int searchWidth, int searchHeight,
                                                int screenWidth, int screenHeight, int entryCount) {
        if (searchWidth <= 0 || screenWidth <= 2 * PADDING || screenHeight <= 2 * PADDING) {
            return EMPTY;
        }
        int above = searchY - GAP - PADDING;
        int below = screenHeight - PADDING - (searchY + searchHeight + GAP);
        boolean useAbove = above >= ROW_HEIGHT + 2 * PADDING;
        int available = useAbove ? above : below;
        int rows = Math.min(Math.min(8, Math.max(1, entryCount)), (available - 2 * PADDING) / ROW_HEIGHT);
        if (rows <= 0) {
            return EMPTY;
        }
        int width = Math.min(Math.max(180, searchWidth), screenWidth - 2 * PADDING);
        int height = rows * ROW_HEIGHT + 2 * PADDING;
        int x = Math.max(PADDING, Math.min(searchX, screenWidth - width - PADDING));
        int y = useAbove ? searchY - GAP - height : searchY + searchHeight + GAP;
        return new HistoryPanelBounds(x, y, width, height, rows);
    }

    public boolean contains(double mouseX, double mouseY) {
        return rows > 0 && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public int rowAt(double mouseX, double mouseY) {
        if (!contains(mouseX, mouseY) || mouseX < x + PADDING || mouseX >= x + width - PADDING
                || mouseY < y + PADDING || mouseY >= y + height - PADDING) {
            return -1;
        }
        return (int) ((mouseY - y - PADDING) / ROW_HEIGHT);
    }
}

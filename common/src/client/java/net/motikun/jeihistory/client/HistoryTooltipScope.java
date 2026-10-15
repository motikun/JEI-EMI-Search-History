package net.motikun.jeihistory.client;

/** Client-thread scope distinguishes our own tooltip from covered item tooltips. */
final class HistoryTooltipScope implements AutoCloseable {
    private static int depth;
    private HistoryTooltipScope() { depth++; }
    static HistoryTooltipScope enter() { return new HistoryTooltipScope(); }
    static boolean isDrawing() { return depth > 0; }
    @Override public void close() { depth--; }
}

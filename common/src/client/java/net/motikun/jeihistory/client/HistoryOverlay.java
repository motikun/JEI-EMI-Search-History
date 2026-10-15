package net.motikun.jeihistory.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.motikun.jeihistory.history.HistoryPanelBounds;
import net.motikun.jeihistory.history.HistorySelection;

/** Rendering has no input or persistence side effects. */
final class HistoryOverlay {
    private HistoryOverlay() {}

    static void draw(GuiGraphics graphics, HistoryPanelBounds bounds, HistorySelection selection, int mouseX, int mouseY) {
        if (bounds.rows() == 0) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        graphics.flush();
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);
        try {
            int x = bounds.x();
            int y = bounds.y();
            int right = x + bounds.width();
            int bottom = y + bounds.height();
            graphics.fill(x, y, right, bottom, 0xF0202020);
            graphics.fill(x, y, right, y + 1, 0xFF909090);
            graphics.fill(x, bottom - 1, right, bottom, 0xFF909090);
            graphics.fill(x, y, x + 1, bottom, 0xFF909090);
            graphics.fill(right - 1, y, right, bottom, 0xFF909090);
            int maxTextWidth = bounds.width() - 2 * HistoryPanelBounds.PADDING - 5;
            if (selection.entries().isEmpty()) {
                String empty = Component.translatable("gui.jeihistory.empty").getString();
                graphics.drawString(font, abbreviate(font, empty, maxTextWidth), x + 4, y + 7, 0xFFAAAAAA);
                return;
            }
            int hoveredRow = bounds.rowAt(mouseX, mouseY);
            String tooltip = null;
            for (int row = 0; row < bounds.rows(); row++) {
                int index = selection.offset() + row;
                if (index >= selection.entries().size()) {
                    break;
                }
                int rowY = y + HistoryPanelBounds.PADDING + row * HistoryPanelBounds.ROW_HEIGHT;
                if (index == selection.selected() || row == hoveredRow) {
                    graphics.fill(x + 2, rowY, right - 2, rowY + HistoryPanelBounds.ROW_HEIGHT,
                            index == selection.selected() ? 0xFF405A80 : 0xFF404040);
                }
                String text = selection.entries().get(index);
                String displayed = abbreviate(font, text, maxTextWidth);
                graphics.drawString(font, displayed, x + 4, rowY + 3, 0xFFFFFFFF);
                if (row == hoveredRow) {
                    tooltip = text;
                }
            }
            if (selection.entries().size() > bounds.rows()) {
                int trackHeight = bounds.height() - 8;
                int thumbHeight = Math.max(3, trackHeight * bounds.rows() / selection.entries().size());
                int thumbY = y + 4 + (trackHeight - thumbHeight) * selection.offset()
                        / (selection.entries().size() - bounds.rows());
                graphics.fill(right - 3, thumbY, right - 2, thumbY + thumbHeight, 0xFFAAAAAA);
            }
            if (tooltip != null) {
                graphics.flush();
                try (var scope = HistoryTooltipScope.enter()) {
                    var lines = new java.util.ArrayList<>(font.split(Component.literal(tooltip), 240));
                    lines.addAll(font.split(Component.translatable("gui.jeihistory.actions"), 240));
                    graphics.renderTooltip(font, lines, mouseX, mouseY);
                }
            }
        } finally {
            graphics.flush();
            graphics.pose().popPose();
        }
    }

    private static String abbreviate(Font font, String text, int width) {
        if (font.width(text) <= width) {
            return text;
        }
        String ellipsis = "…";
        return font.plainSubstrByWidth(text, Math.max(0, width - font.width(ellipsis))) + ellipsis;
    }
}

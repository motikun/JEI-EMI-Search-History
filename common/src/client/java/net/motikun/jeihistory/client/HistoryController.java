package net.motikun.jeihistory.client;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.motikun.jeihistory.history.HistoryPanelBounds;
import net.motikun.jeihistory.history.HistorySelection;
import net.motikun.jeihistory.history.HistoryStore;
import net.motikun.jeihistory.history.CommitIntent;

import java.util.Optional;

/** Owns only the current edit session. The recipe viewer retains ownership of the search field and filter. */
public final class HistoryController {
    private static final HistorySelection SELECTION = new HistorySelection();
    private static HistoryStore store;
    private static EditBox focusedField;
    private static Screen ownerScreen;
    private static String lastText = "";
    private static HistoryPanelBounds bounds = HistoryPanelBounds.EMPTY;
    private static int retryTicks;
    private static boolean edited;
    private static boolean explicit;

    private HistoryController() {}

    private static HistoryStore store() {
        if (store == null) {
            store = new HistoryStore(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("jeihistory/history.json"));
        }
        return store;
    }

    public static void focusChanged(EditBox field, boolean focused) {
        if (focused) {
            if (focusedField == field) {
                return;
            }
            finish();
            focusedField = field;
            ownerScreen = Minecraft.getInstance().screen;
            lastText = field.getValue();
            edited = false;
            explicit = false;
            SELECTION.open(store().entries());
            update(field);
        } else if (focusedField == field) {
            String text = field.getValue();
            focusedField = null;
            ownerScreen = null;
            bounds = HistoryPanelBounds.EMPTY;
            store().record(text, explicit ? CommitIntent.EXPLICIT : edited ? CommitIntent.EDITED : CommitIntent.AUTOMATIC);
        }
    }

    public static void valueChanged(EditBox field, String text) {
        if (isEditing(field) && !text.equals(lastText)) {
            edited = true;
            lastText = text;
            SELECTION.clearSelection();
        }
    }

    public static boolean suppressTooltip() {
        Minecraft minecraft = Minecraft.getInstance();
        double x = minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth() / minecraft.getWindow().getScreenWidth();
        double y = minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight() / minecraft.getWindow().getScreenHeight();
        return !HistoryTooltipScope.isDrawing() && isMouseOver(focusedField(), x, y);
    }

    public static void deleteClick(EditBox field, HistoryClick target, double mouseX, double mouseY) {
        if (target != null && clickTarget(field, mouseX, mouseY).filter(target::equals).isPresent()
                && store().delete(target.text())) {
            SELECTION.remove(target.text());
            // Deletion starts a new automatic-save baseline even if this query was edited earlier.
            if (field.getValue().equals(target.text())) edited = false;
            update(field);
        }
    }

    public static void update(EditBox field) {
        if (!isEditing(field)) {
            bounds = HistoryPanelBounds.EMPTY;
            return;
        }
        if (!field.getValue().equals(lastText)) {
            lastText = field.getValue();
            SELECTION.clearSelection();
        }
        Rect2i area = ((SearchFieldAccess) field).jeihistory$getArea();
        Minecraft minecraft = Minecraft.getInstance();
        bounds = HistoryPanelBounds.calculate(area.getX(), area.getY(), area.getWidth(), area.getHeight(),
                minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight(), SELECTION.entries().size());
        SELECTION.setVisibleRows(bounds.rows());
    }

    public static boolean isEditing(EditBox field) {
        return field != null && focusedField == field && field.isFocused() && ownerScreen != null
                && ownerScreen == Minecraft.getInstance().screen;
    }

    public static boolean isMouseOver(EditBox field, double mouseX, double mouseY) {
        update(field);
        return isEditing(field) && bounds.contains(mouseX, mouseY);
    }

    public static void draw(EditBox field, GuiGraphics graphics, int mouseX, int mouseY) {
        update(field);
        if (isEditing(field)) {
            HistoryOverlay.draw(graphics, bounds, SELECTION, mouseX, mouseY);
        }
    }

    public static EditBox focusedField() {
        return isEditing(focusedField) ? focusedField : null;
    }

    public static Optional<HistoryClick> clickTarget(EditBox field, double mouseX, double mouseY) {
        update(field);
        if (!isEditing(field) || !bounds.contains(mouseX, mouseY)) {
            return Optional.empty();
        }
        int row = bounds.rowAt(mouseX, mouseY);
        int index = row < 0 ? -1 : SELECTION.offset() + row;
        if (index >= 0 && index < SELECTION.entries().size()) {
            return Optional.of(new HistoryClick(index, SELECTION.entries().get(index)));
        }
        return Optional.empty();
    }

    public record HistoryClick(int index, String text) {}

    public static void acceptClick(EditBox field, HistoryClick target, double mouseX, double mouseY) {
        if (target != null && clickTarget(field, mouseX, mouseY).filter(target::equals).isPresent()) {
            accept(field, target.text());
        }
    }

    public static void moveSelection(EditBox field, int delta) {
        update(field);
        if (isEditing(field)) {
            SELECTION.move(delta);
        }
    }

    public static void acceptSelected(EditBox field) {
        update(field);
        if (isEditing(field)) {
            accept(field, SELECTION.selectedEntry().orElse(field.getValue()));
        }
    }

    public static boolean scroll(EditBox field, double mouseX, double mouseY, double delta) {
        if (!isMouseOver(field, mouseX, mouseY)) {
            return false;
        }
        if (delta != 0) {
            SELECTION.scroll(delta > 0 ? -1 : 1);
        }
        return true;
    }

    private static void accept(EditBox field, String text) {
        // setValue invokes the recipe viewer's existing responder, updating the actual ingredient filter.
        explicit = true;
        field.setValue(text);
        field.setFocused(false);
    }

    public static void screenClosing(Screen screen) {
        if (ownerScreen == screen) {
            finish();
        }
    }

    public static void tick() {
        if (focusedField != null && (ownerScreen != Minecraft.getInstance().screen
                || focusedField instanceof EmiSearchAccess emi && !emi.jeihistory$isEnabled())) {
            finish();
        }
        if (++retryTicks >= 100) {
            retryTicks = 0;
            if (store != null) {
                store.flush();
            }
        }
    }

    public static void finish() {
        if (focusedField != null) {
            EditBox field = focusedField;
            field.setFocused(false);
            // Fallback when another mod has already cleared the EditBox focus directly.
            if (focusedField == field) {
                focusChanged(field, false);
            }
        }
        if (store != null) {
            store.flush();
        }
    }
}

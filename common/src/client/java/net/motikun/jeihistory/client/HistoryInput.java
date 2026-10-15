package net.motikun.jeihistory.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;

/** Recipe-viewer-neutral keyboard navigation and mouse capture for history panels. */
public class HistoryInput {
    private final Map<Integer, Capture> captures = new HashMap<>();

    public boolean keyPressed(int keyCode, int scanCode) {
        EditBox field = HistoryController.focusedField();
        if (field == null) {
            return false;
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UP) {
            HistoryController.moveSelection(field, -1);
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN) {
            HistoryController.moveSelection(field, 1);
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER) {
            HistoryController.acceptSelected(field);
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            field.setFocused(false);
        } else {
            return false;
        }
        return true;
    }

    public boolean mousePressed(Screen screen, double mouseX, double mouseY, int button) {
        captures.remove(button);
        EditBox field = HistoryController.focusedField();
        if (field == null) {
            return false;
        }
        if (HistoryController.isMouseOver(field, mouseX, mouseY)) {
            var target = (button == InputConstants.MOUSE_BUTTON_LEFT || button == InputConstants.MOUSE_BUTTON_RIGHT)
                    ? HistoryController.clickTarget(field, mouseX, mouseY).orElse(null) : null;
            captures.put(button, new Capture(screen, field, target));
            return true;
        }
        if (!field.isMouseOver(mouseX, mouseY)) {
            field.setFocused(false);
        }
        return false;
    }

    public boolean mouseReleased(Screen screen, double mouseX, double mouseY, int button) {
        Capture capture = captures.remove(button);
        if (capture == null) {
            return false;
        }
        if (capture.screen() == screen && HistoryController.isEditing(capture.field())) {
            if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                HistoryController.deleteClick(capture.field(), capture.target(), mouseX, mouseY);
            } else if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                HistoryController.acceptClick(capture.field(), capture.target(), mouseX, mouseY);
            }
        }
        // Consume release even when the click was cancelled; never activate an item behind it.
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        return captures.containsKey(button) || HistoryController.isMouseOver(HistoryController.focusedField(), mouseX, mouseY);
    }

    public boolean scroll(double mouseX, double mouseY, double deltaY) {
        return HistoryController.scroll(HistoryController.focusedField(), mouseX, mouseY, deltaY);
    }

    public void updateVisibility(boolean visible) {
        if (!visible) {
            HistoryController.finish();
            captures.clear();
        }
    }

    public void reset() {
        captures.clear();
    }

    private record Capture(Screen screen, EditBox field, HistoryController.HistoryClick target) {}
}

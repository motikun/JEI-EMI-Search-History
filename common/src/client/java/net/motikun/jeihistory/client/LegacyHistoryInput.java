package net.motikun.jeihistory.client;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.Internal;
import net.minecraft.client.gui.components.EditBox;

/** Keep JEI's configurable history key bindings across input-router generations. */
public final class LegacyHistoryInput extends HistoryInput {
    @Override
    public boolean keyPressed(int keyCode, int scanCode) {
        EditBox field = HistoryController.focusedField();
        if (field == null) {
            return false;
        }
        var key = InputConstants.getKey(keyCode, scanCode);
        var keys = Internal.getKeyMappings();
        if (keys.getPreviousSearch().isActiveAndMatches(key)) {
            HistoryController.moveSelection(field, -1);
        } else if (keys.getNextSearch().isActiveAndMatches(key)) {
            HistoryController.moveSelection(field, 1);
        } else if (keys.getEnterKey().isActiveAndMatches(key)) {
            HistoryController.acceptSelected(field);
        } else if (keys.getEscapeKey().isActiveAndMatches(key)) {
            field.setFocused(false);
        } else {
            return false;
        }
        return true;
    }

}

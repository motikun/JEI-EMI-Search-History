package net.motikun.jeihistory.mixin;

import mezz.jei.gui.input.GuiTextFieldFilter;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.components.EditBox;
import net.motikun.jeihistory.client.HistoryController;
import net.motikun.jeihistory.client.SearchFieldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiTextFieldFilter.class, remap = false)
abstract class GuiTextFieldFilterMixin implements SearchFieldAccess {
    @Shadow private ImmutableRect2i area;

    @Override
    public Rect2i jeihistory$getArea() {
        return new Rect2i(area.getX(), area.getY(), area.getWidth(), area.getHeight());
    }

    @Inject(method = "setFocused", remap = true, at = @At("TAIL"))
    private void jeihistory$focusChanged(boolean focused, CallbackInfo ci) {
        HistoryController.focusChanged((GuiTextFieldFilter) (Object) this, focused);
    }

}

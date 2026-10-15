package net.motikun.jeihistory.mixin;

import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.screen.widget.EmiSearchWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.motikun.jeihistory.client.EmiSearchAccess;
import net.motikun.jeihistory.client.EmiClientEvents;
import net.motikun.jeihistory.client.HistoryController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmiSearchWidget.class, remap = false)
abstract class EmiSearchWidgetMixin extends EditBox implements EmiSearchAccess {
    protected EmiSearchWidgetMixin(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    @Override
    public Rect2i jeihistory$getArea() {
        return new Rect2i(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public boolean jeihistory$isEnabled() {
        return EmiConfig.enabled;
    }

    @Inject(method = "keyPressed", remap = true, at = @At("HEAD"), cancellable = true)
    private void jeihistory$bypassNativeHistory(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (HistoryController.focusedField() == this && EmiClientEvents.handleKeyPressed(keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", remap = true, at = @At("HEAD"), cancellable = true)
    private void jeihistory$preserveHistoryFocus(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (HistoryController.focusedField() == this
                && EmiClientEvents.handleMousePressed(Minecraft.getInstance().screen, x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "setFocused", remap = true, at = @At("TAIL"))
    private void jeihistory$focusChanged(boolean focused, CallbackInfo ci) {
        HistoryController.focusChanged(this, focused);
    }
}

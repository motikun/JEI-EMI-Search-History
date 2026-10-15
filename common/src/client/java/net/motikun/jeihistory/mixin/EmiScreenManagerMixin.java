package net.motikun.jeihistory.mixin;

import dev.emi.emi.screen.EmiScreenManager;
import net.minecraft.client.Minecraft;
import net.motikun.jeihistory.client.EmiClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** EMI calls these methods before NeoForge's screen events, including on inventory screens. */
@Mixin(value = EmiScreenManager.class, remap = false)
abstract class EmiScreenManagerMixin {
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private static void jeihistory$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (EmiClientEvents.handleKeyPressed(keyCode, scanCode)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private static void jeihistory$mousePressed(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (EmiClientEvents.handleMousePressed(Minecraft.getInstance().screen, x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private static void jeihistory$mouseReleased(double x, double y, int button, CallbackInfoReturnable<Boolean> cir) {
        if (EmiClientEvents.handleMouseReleased(Minecraft.getInstance().screen, x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private static void jeihistory$mouseDragged(double x, double y, int button, double deltaX, double deltaY,
                                               CallbackInfoReturnable<Boolean> cir) {
        if (EmiClientEvents.handleMouseDragged(x, y, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private static void jeihistory$mouseScrolled(double x, double y, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (EmiClientEvents.handleMouseScrolled(x, y, delta)) {
            cir.setReturnValue(true);
        }
    }
}

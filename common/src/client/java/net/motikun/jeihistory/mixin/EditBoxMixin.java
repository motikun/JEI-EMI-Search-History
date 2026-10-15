package net.motikun.jeihistory.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.motikun.jeihistory.client.HistoryController;
import net.motikun.jeihistory.client.SearchFieldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
abstract class EditBoxMixin {
    @Inject(method = "onValueChange", at = @At("TAIL"))
    private void jeihistory$valueChanged(String text, CallbackInfo ci) {
        if ((Object) this instanceof SearchFieldAccess) {
            HistoryController.valueChanged((EditBox) (Object) this, text);
        }
    }
}

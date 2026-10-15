package net.motikun.jeihistory.mixin;

import net.minecraft.client.Minecraft;
import net.motikun.jeihistory.client.HistoryController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @Inject(method = "stop", at = @At("HEAD"))
    private void jeihistory$finish(CallbackInfo ci) { HistoryController.finish(); }
}

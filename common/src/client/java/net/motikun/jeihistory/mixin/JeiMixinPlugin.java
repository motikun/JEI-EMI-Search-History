package net.motikun.jeihistory.mixin;

import net.motikun.jeihistory.JeiCompatibility;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Choose integrations before resolving optional JEI/EMI classes. */
public final class JeiMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if ((mixinClassName.endsWith("EditBoxMixin") || mixinClassName.endsWith("MinecraftMixin"))) return true;
        if (mixinClassName.endsWith("EmiSearchWidgetMixin") || mixinClassName.endsWith("EmiScreenManagerMixin")) {
            return JeiCompatibility.hasEmi();
        }
        if (!JeiCompatibility.hasJei()) {
            return false;
        }
        return true;
    }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}

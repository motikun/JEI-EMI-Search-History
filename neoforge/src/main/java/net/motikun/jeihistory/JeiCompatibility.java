package net.motikun.jeihistory;

import net.neoforged.fml.loading.FMLLoader;

public final class JeiCompatibility {
    private JeiCompatibility() {}

    public static boolean hasJei() {
        return FMLLoader.getLoadingModList().getMods().stream().anyMatch(mod -> mod.getModId().equals("jei"));
    }

    public static boolean hasEmi() {
        return FMLLoader.getLoadingModList().getMods().stream().anyMatch(mod -> mod.getModId().equals("emi"));
    }

}

package com.thevoidcult.compatUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

public class dragonsurvivalCompat {

    public static boolean isEnderDragonPlayer(Player player) {

        if (!ModList.get().isLoaded("dragonsurvival") ||
                !ModList.get().isLoaded("ds_ender_dragon_addon")) {
            return false;
        }

        if (!(player.level() instanceof ServerLevel)) {
            return false;
        }
        try {
            return Internal.check(player);
        } catch (Throwable t) {
            return false;
        }
    }

    private static class Internal {
        private static boolean check(Player player) {
            return by.dragonsurvivalteam.dragonsurvival.util.DragonUtils.getType(player)
                    .is(ResourceLocation.fromNamespaceAndPath("dragonsurvival", "dseda_ender_dragon"));
        }
    }
}
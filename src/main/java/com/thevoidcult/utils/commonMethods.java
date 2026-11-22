package com.thevoidcult.utils;

import com.thevoidcult.main.TheVoidCultConfigServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import static com.thevoidcult.events.ModEventBusEvents.spawnPortalMatter;

public class commonMethods {

    public static boolean randomTeleport(Level level, LivingEntity entity, boolean generatesPortalMatter) {
        if (!(level instanceof ServerLevel serverLevel)) return false;

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        int minY = serverLevel.getMinBuildHeight();
        int maxY = minY + serverLevel.getLogicalHeight() - 1;

        for (int i = 0; i < 16; ++i) {
            double targetX = x + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
            double targetY = Mth.clamp(y + (double) (entity.getRandom().nextInt(16) - 8), minY, maxY);
            double targetZ = z + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;

            EntityTeleportEvent.ChorusFruit event = new EntityTeleportEvent.ChorusFruit(entity, targetX, targetY, targetZ);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                continue;
            }

            if (entity.isPassenger()) {
                entity.stopRiding();
            }

            if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), false)) {
                SoundEvent soundEvent = entity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
                serverLevel.playSound(null, x, y, z, soundEvent, SoundSource.PLAYERS, 1.0F, 1.0F);
                entity.playSound(soundEvent, 1.0F, 1.0F);

                if (generatesPortalMatter && serverLevel.random.nextFloat() < TheVoidCultConfigServer.CHORUS_DROP_CHANCE.get()) {
                    spawnPortalMatter(serverLevel, entity.position());
                }

                return true;
            }
        }

        return false;
    }
}

package com.thevoidcult.compatUtils;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

public class dragonsurvivalCompat {
    public static boolean isEnderDragon(Entity entity) {
        if (!ModList.get().isLoaded("dragonsurvival")) return false;
        if (!ModList.get().isLoaded("ds_ender_dragon_addon")) return false;

        if (!(entity.level() instanceof ServerLevel serverLevel)) return false;

        try {
            String json = """
                {
                    "type": "minecraft:player",
                    "type_specific": {
                        "type": "dragonsurvival:dragon_predicate",
                        "dragon_species": "dragonsurvival:dseda_ender_dragon"
                    }
                }
                """;

            var registryAccess = serverLevel.registryAccess();
            var ops = registryAccess.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);

            com.google.gson.JsonElement jsonElement = com.google.gson.JsonParser.parseString(json);

            var result = EntityPredicate.CODEC.parse(ops, jsonElement).result();

            if (result.isPresent()) {
                EntityPredicate predicate = result.get();
                return predicate.matches(serverLevel, entity.position(), entity);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
};
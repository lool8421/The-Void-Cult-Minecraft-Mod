package com.thevoidcult.main;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TheVoidCultConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue ENDERMAN_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue CHORUS_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue PEARL_USE_DROP_CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Portal Matter Settings");

        ENDERMAN_DROP_CHANCE = builder
                .comment("Chance for an Enderman to drop Portal Matter when teleporting. (0.0 - 1.0)")
                .defineInRange("endermanDropChance", 0.05, 0.0, 1.0);

        CHORUS_DROP_CHANCE = builder
                .comment("Chance for a player/mob to drop Portal Matter when eating Chorus Fruit or Sin Fruit. (0.0 - 1.0)")
                .defineInRange("chorusDropChance", 0.25, 0.0, 1.0);

        PEARL_USE_DROP_CHANCE = builder
                .comment("Chance to get a portal matter when throwing an ender pearl")
                .defineInRange("pearlUseDropChance", 0.25, 0.0, 1.0);

        builder.pop();
        SPEC = builder.build();
    }
}

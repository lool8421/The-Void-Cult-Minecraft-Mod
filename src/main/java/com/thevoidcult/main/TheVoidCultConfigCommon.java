package com.thevoidcult.main;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TheVoidCultConfigCommon {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue DEFAULT_SPAWN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Datapack settings");


        DEFAULT_SPAWN = builder
                .comment("Specifies custom world spawn datapack to be loaded. \n0 = default \n1 = central island \n2 = outer islands \n3 = void ring (void).")
                .defineInRange("DefaultDatapack", 0, 0, 3);

        builder.pop();
        SPEC = builder.build();
    }
}

package com.thevoidcult.main;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TheVoidCultConfigCommon {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue DEFAULT_SPAWN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Datapack settings");


        DEFAULT_SPAWN = builder
                .comment("Specifies custom end world spawn datapack to be loaded automatically upon world creation. \n" +
                        "0 = unchanged world spawn " +
                        "\n1 = central island (this mode removes the initial ender dragon, escape portal and gateway)" +
                        "\n2 = outer islands" +
                        "\n3 = end void ring (end skyblock experience)")

                .defineInRange("DefaultDatapack", 0, 0, 3);

        builder.pop();
        SPEC = builder.build();
    }
}

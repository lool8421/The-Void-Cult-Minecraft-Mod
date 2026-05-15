package com.thevoidcult.main;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class TheVoidCultConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue ENDERMAN_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue CHORUS_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue PEARL_USE_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue ALTAR_MATTER_DROP_CHANCE;
    public static final ModConfigSpec.DoubleValue ALTAR_MATTER_DROP_CHANCE_PER_TIER;

    public static final ModConfigSpec.IntValue ALTAR_BASE_CAP;
    public static final ModConfigSpec.IntValue ALTAR_CAP_PER_TIER;
    public static final ModConfigSpec.IntValue ALTAR_CAP_PER_CRYSTAL;
    public static final ModConfigSpec.IntValue ALTAR_MAX_CRYSTALS;
    public static final ModConfigSpec.IntValue ALTAR_CRYSTAL_RANGE;
    public static final ModConfigSpec.IntValue ALTAR_OVERALL_CAP;
    public static final ModConfigSpec.IntValue CULTIST_WORK_COOLDOWN;
    public static final ModConfigSpec.IntValue CULTIST_WORK_DURATION;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PORTAL_MATTER_ENTITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends Double>> PORTAL_MATTER_DROP_CHANCES;

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

        ALTAR_MATTER_DROP_CHANCE = builder
                .comment("Chance to get a portal matter when a void altar produces something")
                .defineInRange("altarMatterDropChance", 0.025, 0.0, 1.0);

        ALTAR_MATTER_DROP_CHANCE_PER_TIER = builder
                .comment("Chance to get portal matter from successful rituals every tier")
                .defineInRange("altarMatterDropChancePerTier", 0.005, 0.0, 1.0);

        PORTAL_MATTER_ENTITIES = builder
                .comment("List of entity IDs that drop Portal Matter.")
                .defineList("portalMatterEntities", List.of("minecraft:endermite", "minecraft:enderman", "minecraft:shulker"), (obj) -> obj instanceof String);

        PORTAL_MATTER_DROP_CHANCES = builder
                .comment("The drop chances for the entities above (must match the order from portalMatterEntites)")
                .defineList("portalMatterChances", List.of(0.5, 0.2, 1.0), (obj) -> obj instanceof Double);


        builder.push("Void altar settings");
        builder.comment("You can configure altar limits in this section");

        ALTAR_BASE_CAP = builder
                .comment("Amount of allowed enderman cultists working at an altar at base")
                .defineInRange("altarBaseCap", 2, 0, 255);

        ALTAR_CAP_PER_TIER = builder
                .comment("Amount of allowed enderman cultists working at an altar per altar tier")
                .defineInRange("altarCapPerTier", 2, 2, 255);

        ALTAR_CAP_PER_CRYSTAL = builder
                .comment("Amount of extra allowed enderman cultists working at an altar per nearby end crystal")
                .defineInRange("altarCapPerCrystal", 1, 0, 255);

        ALTAR_MAX_CRYSTALS = builder
                .comment("Max amount of end crystals allowed to influence an altar")
                .defineInRange("altarMatterDropChance", 4, 0, 255);

        ALTAR_CRYSTAL_RANGE = builder
                .comment("Range of detection of end crystals by a void altar")
                .defineInRange("altarCrystalRange", 8, 0, 64);

        ALTAR_OVERALL_CAP = builder
                .comment("Overall hard cap of the allowed cultists at an altar")
                .defineInRange("altarOverallCap", 64, 1, 255);

        CULTIST_WORK_COOLDOWN = builder
                .comment("Work cooldown of enderman cultists (in ticks)")
                        .defineInRange("cultistWorkCooldown",600, 100, 1728000);

        CULTIST_WORK_DURATION = builder
                .comment("Work cooldown of enderman cultists (in ticks)")
                .defineInRange("cultistWorkDuration",100, 10, 36000);

        builder.comment("To edit ritual loot tables, overwrite loot tables with a datapack at data/thevoidcult/loot_table/thevoidcult_rituals/ritual_<type>_<tier>.json",
                "Example loot table name: ritual",
                "Possible types: wrath, greed, gluttony, envy, pride",
                "Tiers: 1 ~ 5");

        builder.pop();
        SPEC = builder.build();
    }
}

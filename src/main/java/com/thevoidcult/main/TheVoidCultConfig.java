package com.thevoidcult.main;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class TheVoidCultConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue ENDERMAN_TP_DROP_CHANCE;
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
    public static final ModConfigSpec.IntValue ALTAR_ITEM_CAPACITY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PORTAL_MATTER_ENTITIES;
    public static final ModConfigSpec.ConfigValue<List<? extends Double>> PORTAL_MATTER_DROP_CHANCES;


    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Portal Matter Settings");


        ENDERMAN_TP_DROP_CHANCE = builder
                .comment("Chance for an Enderman to drop Portal Matter when teleporting. (0.0 - 1.0)")
                .defineInRange("endermanTpDropChance", 0.025, 0.0, 1.0);

        CHORUS_DROP_CHANCE = builder
                .comment("Chance for a player/mob to drop Portal Matter when eating Chorus Fruit or Sin Fruit. (0.0 - 1.0)")
                .defineInRange("chorusDropChance", 0.25, 0.0, 1.0);

        PEARL_USE_DROP_CHANCE = builder
                .comment("Chance to get a portal matter when throwing an ender pearl")
                .defineInRange("pearlUseDropChance", 0.25, 0.0, 1.0);

        ALTAR_MATTER_DROP_CHANCE = builder
                .comment("Chance to get a portal matter when a void altar produces something")
                .defineInRange("altarMatterDropChance", 0.1, 0.0, 1.0);

        ALTAR_MATTER_DROP_CHANCE_PER_TIER = builder
                .comment("Chance to get portal matter from successful rituals every tier")
                .defineInRange("altarMatterDropChancePerTier", 0.02, 0.0, 1.0);

        PORTAL_MATTER_ENTITIES = builder
                .comment("List of entity registry names that drop Portal Matter.")
                .define("portalMatterEntities",
                        List.of("minecraft:endermite", "minecraft:enderman", "minecraft:shulker"),
                        obj -> obj instanceof String str && ResourceLocation.tryParse(str) != null
                );

        PORTAL_MATTER_DROP_CHANCES = builder
                .comment("The drop chances for the entities above (must match the order from portalMatterEntities)")
                .define("portalMatterChances",
                        List.of(2.0, 0.2, 0.5),
                        obj -> obj instanceof Double);

        builder.pop();

        builder.push("Void altar settings");

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
                .defineInRange("altarOverallCap", 16, 1, 255);

        CULTIST_WORK_COOLDOWN = builder
                .comment("Work cooldown of enderman cultists (in ticks)")
                        .defineInRange("cultistWorkCooldown",900, 100, 1728000);

        CULTIST_WORK_DURATION = builder
                .comment("Work cooldown of enderman cultists (in ticks)")
                .defineInRange("cultistWorkDuration",100, 10, 36000);

        ALTAR_ITEM_CAPACITY = builder
                .comment("Determines how many items can a void altar near itself before it stops working. A necessary evil to prevent lag.")
                        .defineInRange("altarItemCapacity", 50, 1, 1000);

        builder.comment("To edit ritual loot tables, overwrite loot tables with a datapack at: data/thevoidcult/loot_tables/rituals/ritual_<type>_<tier>.json\n" +
                "Example: ritual_wrath_1.json\n" +
                "Possible types: wrath, greed, gluttony, envy, pride\n" +
                "Tiers: 1 ~ 5").define("PointlessFieldSoNeoforgeLetsMeCreateTheMessageAboveBecauseItNeedsAnInputFieldToBeAbleToHaveComments", true);

        builder.pop();
        SPEC = builder.build();
    }
}

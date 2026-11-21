package com.thevoidcult.registers;

import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.main.TheVoidCultConfigCommon;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class RegisterOptionalDataPacks {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterOptionalDataPacks::onAddPackFinders);
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            int defaultMode = TheVoidCultConfigCommon.DEFAULT_SPAWN.get();

            PackSource optionalModSource = PackSource.create(
                    (packName) -> Component.literal("End spawn - added by The Void Cult").withStyle(ChatFormatting.GRAY),
                    false
            );

            registerEndPack(event, optionalModSource, "packs/spawn_end_center", "End Spawn: Central Island", defaultMode == 1);
            registerEndPack(event, optionalModSource, "packs/spawn_end_outer", "End Spawn: Outer Islands", defaultMode == 2);
            registerEndPack(event, optionalModSource, "packs/spawn_end_void", "End Spawn: Void Ring", defaultMode == 3);
        }
    }

    private static void registerEndPack(
            AddPackFindersEvent event,
            PackSource disabledSource,
            String path,
            String title,
            boolean enabledByDefault
    ) {
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(TheVoidCult.MOD_ID, path),
                PackType.SERVER_DATA,
                Component.literal(title),
                enabledByDefault ? PackSource.BUILT_IN : disabledSource,
                false,
                Pack.Position.TOP
        );
    }
}
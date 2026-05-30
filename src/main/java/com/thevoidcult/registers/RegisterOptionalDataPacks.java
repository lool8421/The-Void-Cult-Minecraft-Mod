package com.thevoidcult.registers;

import com.thevoidcult.main.TheVoidCult;
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

            PackSource optionalModSource = PackSource.create(
                    (packName) -> Component.literal("End spawn - added by The Void Cult").withStyle(ChatFormatting.GRAY),
                    false
            );


            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath(TheVoidCult.MOD_ID, "packs/spawn_in_end"),
                    PackType.SERVER_DATA,
                    Component.literal("Spawn in end dimension."),
                    optionalModSource,
                    false,
                    Pack.Position.TOP
            );
        }
    }
}
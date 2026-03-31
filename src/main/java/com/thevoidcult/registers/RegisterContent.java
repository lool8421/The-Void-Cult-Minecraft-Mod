package com.thevoidcult.registers;

import com.thevoidcult.main.TheVoidCult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CryingObsidianBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

public class RegisterContent {

    //items
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheVoidCult.MOD_ID);

    public static final DeferredItem<Item> PORTAL_MATTER = ITEMS.register("portal_matter",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENDER_CULTIST_HELMET = ITEMS.register("ender_cultist_helmet",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ZEALOUS_FRUIT = ITEMS.register("zealous_fruit",
            () -> new Item(new Item.Properties()));

    //blocks

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheVoidCult.MOD_ID);
    public static final DeferredBlock<Block> VOID_ALTAR = BLOCKS.register("void_altar", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(50.0F, 1200.0F)
            .lightLevel(state -> 7)));
    public static final DeferredItem<BlockItem> VOID_ALTAR_ITEM = ITEMS.registerSimpleBlockItem("void_altar", VOID_ALTAR);


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
    }


}

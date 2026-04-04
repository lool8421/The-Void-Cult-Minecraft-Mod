package com.thevoidcult.registers;

import com.thevoidcult.items.MobPearlItem;
import com.thevoidcult.main.TheVoidCult;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import java.util.function.Supplier;

public class RegisterContent {

    //items
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheVoidCult.MOD_ID);

    public static final DeferredItem<Item> PORTAL_MATTER = ITEMS.register("portal_matter",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENDER_CULTIST_HELMET = ITEMS.register("ender_cultist_helmet",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WRATH_FRUIT = ITEMS.register("fruit_of_wrath",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GREED_FRUIT = ITEMS.register("fruit_of_greed",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PRIDE_FRUIT = ITEMS.register("fruit_of_pride",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GLUTTONY_FRUIT = ITEMS.register("fruit_of_gluttony",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENVY_FRUIT = ITEMS.register("fruit_of_envy",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> MOB_PEARL = ITEMS.register("mob_pearl",
            () -> new MobPearlItem(new Item.Properties()));

    //blocks

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheVoidCult.MOD_ID);

    public static final DeferredBlock<Block> VOID_ALTAR = BLOCKS.register("void_altar", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(state -> 7).sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> VOID_ALTAR_ITEM = ITEMS.registerSimpleBlockItem("void_altar", VOID_ALTAR);

    public static final DeferredBlock<Block> WARPED_IRON_BLOCK = BLOCKS.register("warped_iron_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().strength(5.0F, 50.0F).lightLevel(state -> 7).sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredItem<BlockItem> WARPED_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_iron_block", WARPED_IRON_BLOCK);

    public static final DeferredBlock<Block> WARPED_GOLD_BLOCK = BLOCKS.register("warped_gold_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().strength(5.0F, 50.0F).lightLevel(state -> 7).sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> WARPED_GOLD_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_gold_block", WARPED_GOLD_BLOCK);

    public static final DeferredBlock<Block> WARPED_DIAMOND_BLOCK = BLOCKS.register("warped_diamond_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(5.0F, 50.0F).lightLevel(state -> 7).sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> WARPED_DIAMOND_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_diamond_block", WARPED_DIAMOND_BLOCK);

    public static final DeferredBlock<Block> WARPED_NETHERITE_BLOCK = BLOCKS.register("warped_netherite_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(25.0F, 1200.0F).lightLevel(state -> 7).sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredItem<BlockItem> WARPED_NETHERITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_netherite_block", WARPED_NETHERITE_BLOCK);


    //creative mode

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,TheVoidCult.MOD_ID);
    public static final Supplier<CreativeModeTab> THE_VOID_CULT_CREATIVE_TAB = CREATIVE_MODE_TAB.register("thevoidcult_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(PORTAL_MATTER.get()))
                    .title(Component.translatable("creativetab.thevoidcult.thevoidcult_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(PORTAL_MATTER);
                        output.accept(VOID_ALTAR_ITEM);
                        output.accept(WARPED_IRON_BLOCK_ITEM);
                        output.accept(WARPED_GOLD_BLOCK_ITEM);
                        output.accept(WARPED_DIAMOND_BLOCK_ITEM);
                        output.accept(WARPED_NETHERITE_BLOCK_ITEM);
                        output.accept(ENDER_CULTIST_HELMET);
                        output.accept(WRATH_FRUIT);
                        output.accept(GREED_FRUIT);
                        output.accept(PRIDE_FRUIT);
                        output.accept(GLUTTONY_FRUIT);
                        output.accept(ENVY_FRUIT);

                    })
                    .build()
            );


    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        CREATIVE_MODE_TAB.register(eventBus);
        
    }


}

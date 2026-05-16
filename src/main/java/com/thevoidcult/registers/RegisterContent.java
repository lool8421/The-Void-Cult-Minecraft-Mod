package com.thevoidcult.registers;

import com.thevoidcult.blockEntities.VoidAltarBlock;
import com.thevoidcult.blockEntities.VoidAltarBlockEntity;
import com.thevoidcult.items.*;
import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;


import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class RegisterContent {

    //data components
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TheVoidCult.MOD_ID);

    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator){
        return COMPONENTS.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EntityType<?>>> STORED_ENTITY =
            register("stored_entity", builder -> builder.persistent(BuiltInRegistries.ENTITY_TYPE.byNameCodec()));

    //armor materials

    //items
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TheVoidCult.MOD_ID);

    public static final DeferredItem<ArmorItem> ENDER_CULTIST_HELMET = ITEMS.register("ender_cultist_helmet",
            () -> new EnderCultistHelmetItem(
                    EnderCultistHelmetItem.ENDER_CULTIST_ARMOR_MATERIAL,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(25))
            ));

    public static final DeferredItem<Item> PORTAL_MATTER = ITEMS.register("portal_matter",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WRATH_FRUIT = ITEMS.register("fruit_of_wrath",
            () -> new SinFruitItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.4f).alwaysEdible().build()), SinsList.WRATH));
    public static final DeferredItem<Item> GREED_FRUIT = ITEMS.register("fruit_of_greed",
            () -> new SinFruitItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.4f).alwaysEdible().build()), SinsList.GREED));
    public static final DeferredItem<Item> PRIDE_FRUIT = ITEMS.register("fruit_of_pride",
            () -> new SinFruitItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.4f).alwaysEdible().build()), SinsList.PRIDE));
    public static final DeferredItem<Item> GLUTTONY_FRUIT = ITEMS.register("fruit_of_gluttony",
            () -> new SinFruitItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(12).saturationModifier(1.5f).alwaysEdible().build()), SinsList.GLUTTONY));
    public static final DeferredItem<Item> ENVY_FRUIT = ITEMS.register("fruit_of_envy",
            () -> new SinFruitItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.4f).alwaysEdible().build()), SinsList.ENVY));
    public static final DeferredItem<Item> MOB_PEARL = ITEMS.register("mob_pearl",
            () -> new MobPearlItem(new Item.Properties()));
    public static final DeferredItem<Item> CHORUS_PICKAXE =
            ITEMS.register("chorus_pickaxe", () -> new ChorusPickaxeItem(
                    ChorusPickaxeItem.CHORUS_PICKAXE_TIER,
                    new Item.Properties().stacksTo(1) // Tools shouldn't stack
            ));
    public static final DeferredItem<Item> CULT_LEADER_STAFF = ITEMS.register("cult_leader_staff",
            () -> new CultLeaderStaffItem(new Item.Properties()));

    //blocks

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TheVoidCult.MOD_ID);

    public static final DeferredBlock<Block> VOID_ALTAR = BLOCKS.register("void_altar", () -> new VoidAltarBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F).lightLevel(state -> 12).sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> VOID_ALTAR_ITEM = ITEMS.registerSimpleBlockItem("void_altar", VOID_ALTAR);

    public static final DeferredBlock<Block> WARPED_IRON_BLOCK = BLOCKS.register("warped_iron_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().strength(5.0F, 50.0F).lightLevel(state -> 12).sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredItem<BlockItem> WARPED_IRON_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_iron_block", WARPED_IRON_BLOCK);

    public static final DeferredBlock<Block> WARPED_GOLD_BLOCK = BLOCKS.register("warped_gold_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK).requiresCorrectToolForDrops().strength(5.0F, 50.0F).lightLevel(state -> 13).sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> WARPED_GOLD_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_gold_block", WARPED_GOLD_BLOCK);

    public static final DeferredBlock<Block> WARPED_DIAMOND_BLOCK = BLOCKS.register("warped_diamond_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(5.0F, 50.0F).lightLevel(state -> 14).sound(SoundType.STONE)));
    public static final DeferredItem<BlockItem> WARPED_DIAMOND_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_diamond_block", WARPED_DIAMOND_BLOCK);

    public static final DeferredBlock<Block> WARPED_NETHERITE_BLOCK = BLOCKS.register("warped_netherite_block", () -> new Block(BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(25.0F, 1200.0F).lightLevel(state -> 15).sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredItem<BlockItem> WARPED_NETHERITE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("warped_netherite_block", WARPED_NETHERITE_BLOCK);

    //mobs

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, TheVoidCult.MOD_ID);

    public static final Supplier<EntityType<EndermanCultistEntity>> ENDERMAN_CULTIST =
            ENTITY_TYPES.register("enderman_cultist", ()->EntityType.Builder.of(EndermanCultistEntity::new,MobCategory.CREATURE).sized(0.6f,2.9f).build("enderman_cultist"));

    //block entities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheVoidCult.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VoidAltarBlockEntity>> VOID_ALTAR_BE =
            BLOCK_ENTITIES.register("void_altar", () ->
                    BlockEntityType.Builder.of(VoidAltarBlockEntity::new, VOID_ALTAR.get()).build(null));

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
                        output.accept(CHORUS_PICKAXE);
                        output.accept(CULT_LEADER_STAFF);
                    })
                    .build()
            );



    public static void register(IEventBus eventBus){
        COMPONENTS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        CREATIVE_MODE_TAB.register(eventBus);
        ENTITY_TYPES.register(eventBus);
    }



}

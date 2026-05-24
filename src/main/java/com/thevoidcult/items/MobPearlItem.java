package com.thevoidcult.items;

import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;


public class MobPearlItem extends Item {


    public MobPearlItem(Properties properties){
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = stack.get(RegisterContent.STORED_ENTITY.get());

        // 1. Get the base name of the Pearl (from your lang file)
        MutableComponent baseName = Component.translatable(this.getDescriptionId(stack));

        if (type != null) {
            // 2. Get the name of the entity (e.g., "Warden" or "Pig")
            Component entityName = type.getDescription();

            // 3. Combine them: "Mob Pearl - Warden"
            return baseName.append(" - ").append(entityName);
        }

        // If empty, just return "Mob Pearl"
        return baseName;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();

        if (!level.isClientSide) {
            // Get the component data
            EntityType<?> type = stack.get(RegisterContent.STORED_ENTITY.get());

            if (type != null) {
                Entity entity = type.create(level);


                if (entity instanceof Mob mob) {
                    BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
                    mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);

                    if (level instanceof ServerLevel serverLevel) {
                        net.neoforged.neoforge.event.EventHooks.finalizeMobSpawn(
                                mob,
                                serverLevel,
                                level.getCurrentDifficultyAt(mob.blockPosition()),
                                MobSpawnType.SPAWN_EGG,
                                null
                        );
                    }
                    level.addFreshEntity(mob);

                    if (!context.getPlayer().getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }

    public static void registerDispenserBehavior() {
        DispenserBlock.registerBehavior(RegisterContent.MOB_PEARL.get(), new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                Level level = source.level();

                EntityType<?> type = stack.get(RegisterContent.STORED_ENTITY.get());

                if (type != null && !level.isClientSide()) {
                    Direction dir = source.state().getValue(DispenserBlock.FACING);
                    BlockPos pos = source.pos().relative(dir);

                    Entity entity = type.create(level);
                    if (entity instanceof Mob mob) {
                        mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, dir.toYRot(), 0.0F);
                        level.addFreshEntity(mob);
                        stack.shrink(1);
                        return stack;
                    }
                }
                // Fallback: If empty, just spit the item out
                return super.execute(source, stack);
            }
        });
    }

    public static void SetupDisplayType(FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            ItemProperties.register(RegisterContent.MOB_PEARL.get(),
                    ResourceLocation.fromNamespaceAndPath(TheVoidCult.MOD_ID, "filled"),
                    (stack, level, entity, seed) -> {
                        return stack.has(RegisterContent.STORED_ENTITY.get()) ? 1.0F : 0.0F;
                    });
        });
    }

}

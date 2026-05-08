package com.thevoidcult.items;

import com.thevoidcult.main.TheVoidCult;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

import java.util.List;


public class MobPearlItem extends Item {


    public MobPearlItem(Properties properties){
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.thevoidcult.MobPearlItem.desc.stored_mob").append(": "));

        EntityType<?> type = stack.get(RegisterContent.STORED_ENTITY.get());
        if (type != null) {
            tooltipComponents.add(Component.translatable(type.getDescriptionId())
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltipComponents.add(Component.translatable("item.thevoidcult.MobPearlItem.desc.empty")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
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
                // Get data from the component
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


}

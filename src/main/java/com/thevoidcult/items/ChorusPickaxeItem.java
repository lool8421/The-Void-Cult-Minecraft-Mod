package com.thevoidcult.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import java.util.List;

public class ChorusPickaxeItem extends PickaxeItem {

    public static final Tier CHORUS_PICKAXE_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            16,
            2.0f,
            0.0f,
            50,
            () -> Ingredient.of(Items.CHORUS_FRUIT)
    );

    public ChorusPickaxeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        // Optional: Make it vibrate/teleport slightly when hitting hard blocks
        return super.canAttackBlock(state, level, pos, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thevoidcult.chorus_pickaxe.desc")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        // Only run on the server side
        if (!level.isClientSide && entity instanceof Player player) {
            // 10% chance to teleport on every block break
            if (level.random.nextFloat() < 0.5f) {
                teleportRandomly(level, player);
            }
        }
        return super.mineBlock(stack, level, state, pos, entity);
    }

    private void teleportRandomly(Level level, Player player) {
        double x = player.getX() + (level.random.nextDouble() - 0.5D) * 16.0D;
        double y = player.getY() + (double)(level.random.nextInt(16) - 8);
        double z = player.getZ() + (level.random.nextDouble() - 0.5D) * 16.0D;

        if (player.randomTeleport(x, y, z, true)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            EntityTeleportEvent.ChorusFruit event = new EntityTeleportEvent.ChorusFruit(player, x, y, z);
        }
    }
}

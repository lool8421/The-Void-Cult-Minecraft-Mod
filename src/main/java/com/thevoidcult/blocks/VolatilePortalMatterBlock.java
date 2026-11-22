package com.thevoidcult.blocks;

import com.thevoidcult.utils.commonMethods;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class VolatilePortalMatterBlock extends Block {
    public VolatilePortalMatterBlock(Properties properties){
        super(properties);
    }

    private static boolean hasSilkTouch(Level level, ItemStack tool) {
        if (tool.isEmpty()) return false;
        var silkTouch = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH);
        return EnchantmentHelper.getItemEnchantmentLevel(silkTouch, tool) > 0;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !living.isSteppingCarefully()) {
            long now = level.getGameTime();
            long lastTp = living.getPersistentData().getLong("voidcult_volatile_tp");
            if (now - lastTp >= 10L) {
                living.getPersistentData().putLong("voidcult_volatile_tp", now);
                commonMethods.randomTeleport(level, living, false);
            }
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean hasSilk = hasSilkTouch(level, player.getMainHandItem());
        boolean destroyed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);

        if (destroyed && !level.isClientSide && !player.isCreative() && !hasSilk) {
            commonMethods.randomTeleport(level, player, false);
        }
        return destroyed;
    }


}

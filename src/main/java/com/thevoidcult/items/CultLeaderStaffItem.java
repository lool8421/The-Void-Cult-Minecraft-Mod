package com.thevoidcult.items;

import com.thevoidcult.blockEntities.VoidAltarBlockEntity;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;

public class CultLeaderStaffItem extends Item {
    public CultLeaderStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if(!EnderCultistHelmetItem.isEndermanFriendly(player)) {
            player.displayClientMessage(Component.translatable("message.thevoidcult.no_trust"), true);
            return InteractionResult.FAIL;
        }
        if (target instanceof EndermanCultistEntity cultist && !player.level().isClientSide) {
            // Tell the cultist to follow this player
            cultist.setLeadingPlayer(player.getUUID());
            player.displayClientMessage(Component.translatable("message.thevoidcult.following_start"), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thevoidcult.cult_leader_staff.desc")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Check for Shift + Right Click in the air
        if (player.isCrouching() && !level.isClientSide) {
            double radius = 32.0D;
            List<EndermanCultistEntity> followers = level.getEntitiesOfClass(EndermanCultistEntity.class,
                    player.getBoundingBox().inflate(radius),
                    cultist -> player.getUUID().equals(cultist.getLeadingPlayerUUID()));

            if (!followers.isEmpty()) {
                for (EndermanCultistEntity follower : followers) {
                    follower.setLeadingPlayer(null);
                    // Optional: Play a "dismissed" particle at each cultist
                    ((ServerLevel)level).sendParticles(ParticleTypes.SMOKE,
                            follower.getX(), follower.getY() + 1, follower.getZ(),
                            10, 0.2, 0.2, 0.2, 0.02);
                }

                player.displayClientMessage(Component.translatable("message.thevoidcult.followers_dismissed"), true);
                player.getCooldowns().addCooldown(this, 20); // 1 second cooldown to prevent spam

                level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 0.5f);

                return InteractionResultHolder.success(itemstack);
            }
        }

        return InteractionResultHolder.pass(itemstack);
    }
}
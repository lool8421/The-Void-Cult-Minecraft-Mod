package com.thevoidcult.items;

import com.thevoidcult.main.TheVoidCult;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public class SinFruitItem extends Item {
    public SinFruitItem(Properties properties){
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof EnderMan enderman) {
            if (target.isAlive()) {
                if (!player.level().isClientSide) {
                    enderman.heal(10); //TBD
                    target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                }

                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}

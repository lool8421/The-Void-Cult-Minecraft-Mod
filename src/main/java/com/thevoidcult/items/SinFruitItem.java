package com.thevoidcult.items;

import com.thevoidcult.compatUtils.dragonsurvivalCompat;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Map;


public class SinFruitItem extends Item {

    private final SinsList fruitType;

    public SinFruitItem(Properties properties, SinsList fruitType){
        super(properties);
        this.fruitType = fruitType;
    }

    private final Map<SinsList, String> cultistMessages = Map.of(
            SinsList.GREED, "greed",
            SinsList.GLUTTONY, "gluttony",
            SinsList.ENVY, "envy",
            SinsList.PRIDE, "pride",
            SinsList.WRATH, "wrath"
    );

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {

        if (target instanceof EnderMan enderman) {
            if (target.isAlive()) {
                if (!player.level().isClientSide) {
                    ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                    if(dragonsurvivalCompat.isEnderDragonPlayer(player) || player.isCreative() || helmet.is(RegisterContent.ENDER_CULTIST_HELMET.get())) {

                        EndermanCultistEntity endermanCultist = enderman.convertTo(RegisterContent.ENDERMAN_CULTIST.get(), true);
                        endermanCultist.changeType(this.fruitType);
                        endermanCultist.getEntityData().set(EndermanCultistEntity.DATA_CULTIST_TYPE, this.fruitType.ordinal());
                        endermanCultist.hasImpulse = true;

                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }

                        target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                    } else player.sendSystemMessage(Component.translatable("message.thevoidcult.failed_to_convert_enderman"));
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }
        else if (target instanceof EndermanCultistEntity endermanCultist) {
            if (target.isAlive()) {
                if (!player.level().isClientSide) {
                    if (endermanCultist.getSinType() != this.fruitType) {

                        endermanCultist.changeType(this.fruitType);
                        endermanCultist.getEntityData().set(EndermanCultistEntity.DATA_CULTIST_TYPE, this.fruitType.ordinal());
                        endermanCultist.hasImpulse = true;

                        if (!player.getAbilities().instabuild) {
                            stack.shrink(1);
                        }

                        target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                        return InteractionResult.CONSUME;
                    } else
                        player.sendSystemMessage(Component.translatable("message.thevoidcult.cultist_is_the_same_type"));
                    return InteractionResult.PASS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}

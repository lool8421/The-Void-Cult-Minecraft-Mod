package com.thevoidcult.items;

import com.thevoidcult.compatUtils.dragonsurvivalCompat;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
                    EndermanCultistEntity endermanCultist = (EndermanCultistEntity) enderman.convertTo(RegisterContent.ENDERMAN_CULTIST.get(), true);

                    player.sendSystemMessage(Component.literal(cultistMessages.get(this.fruitType)));

                    if(dragonsurvivalCompat.isEnderDragon(player))
                        player.sendSystemMessage(Component.literal("confirmed"));


                    target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                }
                return InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }
        return InteractionResult.CONSUME;
    }
}

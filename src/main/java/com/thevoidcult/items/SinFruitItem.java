package com.thevoidcult.items;

import com.thevoidcult.compatUtils.dragonsurvivalCompat;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.network.chat.Component;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;


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

                        player.getCooldowns().addCooldown(this, 20);
                        player.stopUsingItem();

                        target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                        return InteractionResult.SUCCESS;

                    } else player.sendSystemMessage(Component.translatable("message.thevoidcult.failed_to_convert_enderman"));
                    return InteractionResult.FAIL;
                }
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
                        player.getCooldowns().addCooldown(this, 20);
                        player.stopUsingItem();

                        target.level().gameEvent(target, GameEvent.EQUIP, target.position());
                        return InteractionResult.SUCCESS;
                    } else
                        player.sendSystemMessage(Component.translatable("message.thevoidcult.cultist_is_the_same_type"));
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitresult instanceof EntityHitResult entityHit && (entityHit.getEntity() instanceof EnderMan || entityHit.getEntity() instanceof EndermanCultistEntity)) {
            player.stopUsingItem();
            return InteractionResultHolder.pass(itemstack);
        }

        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, entity);
        if(entity instanceof Player player)
            player.getCooldowns().addCooldown(this, 20);

        if (!level.isClientSide) {
            switch (this.fruitType) {
                case WRATH -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 20*120, 0));
                    entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20*120, 1));
                }
                case PRIDE -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 20*120, 0));
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20*120, 0));
                }
                case GREED -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 20*120, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20*120, 2));
                }
                case GLUTTONY -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20*30, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*30, 2));
                }
                case ENVY -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.LUCK, 20*60, 2));
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20*60, 2));

                }
            }
            //teleport
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();

            for (int i = 0; i < 16; ++i) {
                double targetX = x + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;
                double targetY = Mth.clamp(y + (double) (entity.getRandom().nextInt(16) - 8), (double) level.getMinBuildHeight(), (double) (level.getMinBuildHeight() + ((ServerLevel) level).getLogicalHeight() - 1));
                double targetZ = z + (entity.getRandom().nextDouble() - 0.5D) * 16.0D;

                EntityTeleportEvent.ChorusFruit event = new EntityTeleportEvent.ChorusFruit(entity, targetX, targetY, targetZ);

                if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    continue;
                }

                if (entity.isPassenger()) {
                    entity.stopRiding();
                }

                if (entity.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
                    SoundEvent soundevent = entity instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;

                    level.playSound(null, x, y, z, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
                    entity.playSound(soundevent, 1.0F, 1.0F);

                    break;
                }
            }


        }

        return super.finishUsingItem(stack, level, entity);
    }

}

package com.thevoidcult.items;

import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import com.thevoidcult.utils.commonMethods;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import static com.thevoidcult.utils.commonMethods.randomTeleport;


public class SinFruitItem extends Item {

    private final SinsList fruitType;

    public SinFruitItem(Properties properties, SinsList fruitType){
        super(properties);
        this.fruitType = fruitType;
    }


    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {


        if (target instanceof EnderMan enderman && enderman.getPersistentAngerTarget() == null) {
            if (target.isAlive()) {
                if (!player.level().isClientSide) {

                    if(EnderCultistHelmetItem.isEndermanFriendly(player)) {

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
        else if (target instanceof EndermanCultistEntity endermanCultist && endermanCultist.getPersistentAngerTarget() == null) {
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

        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getViewVector(1.0F);
        double reach = player.entityInteractionRange();

        Vec3 reachVector = eyePosition.add(lookVector.x * reach, lookVector.y * reach, lookVector.z * reach);

        AABB searchBox = player.getBoundingBox().expandTowards(lookVector.scale(reach)).inflate(1.0D);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player, eyePosition, reachVector, searchBox,
                entity -> entity instanceof EnderMan || entity instanceof EndermanCultistEntity,
                reach * reach
        );

        if (entityHit != null) {
            return InteractionResultHolder.fail(itemstack);
        }

        return super.use(level, player, hand);
    }


    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {

        if(entity instanceof Player player) {
            player.getCooldowns().addCooldown(this, 20);
            commonMethods.randomTeleport(level, player, true);
        }

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
                case SLOTH -> {
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20*30, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20*30, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20*30, 3));
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 20*30, 3));
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20*30, 1));
                }
            }




        }

        return super.finishUsingItem(stack, level, entity);
    }

}

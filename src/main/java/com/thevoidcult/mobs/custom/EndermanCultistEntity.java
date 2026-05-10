package com.thevoidcult.mobs.custom;



import com.thevoidcult.items.SinFruitItem;
import com.thevoidcult.items.SinsList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;

import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;


public class EndermanCultistEntity extends PathfinderMob implements NeutralMob {

    private SinsList sinType;
    private int workCooldown;

    public EndermanCultistEntity(EntityType<? extends PathfinderMob> entityType, Level level){
        super(entityType, level);
        this.setPersistenceRequired();
        this.sinType = SinsList.NONE;
        this.workCooldown = 600;
        this.hasImpulse = true;
    }


    public SinsList getSinType(){
        return this.sinType;
    }

    public static final EntityDataAccessor<Integer> DATA_CULTIST_TYPE = SynchedEntityData.defineId(EndermanCultistEntity.class, EntityDataSerializers.INT);


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); // CRITICAL: Must be first
        builder.define(DATA_CULTIST_TYPE, SinsList.NONE.ordinal());
    }

    public SinsList getSyncedType() {
        return SinsList.values()[this.entityData.get(DATA_CULTIST_TYPE)];
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        // If the type changes (even on startup), tell the renderer to re-evaluate
        if (DATA_CULTIST_TYPE.equals(key) && this.level().isClientSide) {
            this.refreshDimensions();
        }
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide) {
            // Get the value we loaded from NBT and push it into the tracker again
            // to force a network synchronization packet.
            this.entityData.set(DATA_CULTIST_TYPE, this.entityData.get(DATA_CULTIST_TYPE));
        }
    }



    public void changeType(SinsList sin){
        this.sinType = sin;
    }


    private int remainingPersistentAngerTime = 0;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);;
    private UUID persistentAngerTarget;

    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState ritualAnimationState = new AnimationState();
    public final AnimationState angryAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    private int AnimationTimeout = 0;

    private void setupAnimationStates(){


        if(this.AnimationTimeout <= 0){
            this.AnimationTimeout = 60;
            this.idleAnimationState.start(this.tickCount);
            return;
        }
        else{
            --this.AnimationTimeout;
            return;
        }
    }

    @Override
    protected void registerGoals(){
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, (double)1.0F, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, (double)1.0F, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, new Class[0]));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Endermite.class, true, false));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal(this, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 7.0)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }


    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENDERMAN_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }

    public void die(DamageSource cause){
        super.die(cause);
    }


    @Override
    public void tick(){
        super.tick();
        if(this.level().isClientSide) {
            this.setupAnimationStates();

            this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);


            if(this.tickCount%4 == 0){
            SimpleParticleType sinParticle = switch (this.getSyncedType()) {
                case WRATH -> ParticleTypes.LAVA;
                case GLUTTONY -> ParticleTypes.FALLING_LAVA; // Green sparkles
                case GREED -> ParticleTypes.TOTEM_OF_UNDYING; // Or custom gold bits
                case ENVY -> ParticleTypes.HAPPY_VILLAGER; // That lime-green puff
                case PRIDE -> ParticleTypes.WITCH; // Royal purple/pink
                default -> ParticleTypes.PORTAL;
            };
            this.level().addParticle(sinParticle, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 0.5, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 0.5);
            }



        }else
        {
            --this.workCooldown;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        // Store the enum as a string (easier to read/debug)
        compound.putString("CultistType", this.sinType.name());
        // Store the cooldown as an int
        compound.putInt("WorkCooldown", this.workCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        // 1. Restore the Java Variable (The Logic)
        if (compound.contains("CultistType")) {
            try {
                this.sinType = SinsList.valueOf(compound.getString("CultistType"));
            } catch (IllegalArgumentException e) {
                this.sinType = SinsList.NONE;
            }
        }
        this.workCooldown = compound.getInt("WorkCooldown");

        this.entityData.set(DATA_CULTIST_TYPE, this.sinType.ordinal());
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public void setRemainingPersistentAngerTime(int time) {
        this.remainingPersistentAngerTime = time;
    }

    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID target) {
        this.persistentAngerTarget = target;
    }
}

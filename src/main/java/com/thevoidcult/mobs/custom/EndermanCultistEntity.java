package com.thevoidcult.mobs.custom;



import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
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
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.UUID;

//animal so it doesn't do weird crap related to peaceful mode
public class EndermanCultistEntity extends PathfinderMob implements NeutralMob {


    public EndermanCultistEntity(EntityType<? extends PathfinderMob> entityType, Level level){
        super(entityType, level);
    }



    private int remainingPersistentAngerTime = 0;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);;
    private UUID persistentAngerTarget;

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
                .add(Attributes.MAX_HEALTH, (double)40.0F)
                .add(Attributes.MOVEMENT_SPEED, (double)0.3F)
                .add(Attributes.ATTACK_DAMAGE, (double)7.0F)
                .add(Attributes.FOLLOW_RANGE, (double)64.0F)
                .add(Attributes.STEP_HEIGHT, (double)1.0F);
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
        if(this.level().isClientSide){
            this.setupAnimationStates();
        }else
        {

        }
    }
}

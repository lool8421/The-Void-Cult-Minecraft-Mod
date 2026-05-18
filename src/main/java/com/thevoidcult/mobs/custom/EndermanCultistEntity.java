package com.thevoidcult.mobs.custom;




import com.thevoidcult.blockEntities.VoidAltarBlockEntity;
import com.thevoidcult.items.EnderCultistHelmetItem;
import com.thevoidcult.items.SinsList;
import com.thevoidcult.main.TheVoidCultConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;

import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

import javax.annotation.Nullable;
import java.util.UUID;


public class EndermanCultistEntity extends PathfinderMob implements NeutralMob {

    private SinsList sinType;
    private int workCooldown;
    private BlockPos assignedAltarPos;
    private int ritualTime;
    private UUID leadingPlayerUUID = null;
    private int teleportCooldown = 60;
    private int matterCreationCooldown = 400;

    public EndermanCultistEntity(EntityType<? extends PathfinderMob> entityType, Level level){
        super(entityType, level);
        this.setPersistenceRequired();
        this.sinType = SinsList.NONE;
        this.workCooldown = TheVoidCultConfig.CULTIST_WORK_COOLDOWN.get();
        this.hasImpulse = true;
        this.ritualTime = 0;

        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (ritualTime > 0) {
                    Vec3 altarCenter = Vec3.atCenterOf(assignedAltarPos);
                    mob.getLookControl().setLookAt(altarCenter.x, altarCenter.y + 3, altarCenter.z);
                    super.tick();
                } else {
                    super.tick();
                }
            }
        };
    }

    public void setLeadingPlayer(@Nullable UUID uuid) {
        this.leadingPlayerUUID = uuid;

        if (uuid != null) {
            if (this.assignedAltarPos != null) {
                if (this.level().getBlockEntity(this.assignedAltarPos) instanceof VoidAltarBlockEntity altar) {
                    altar.removeCultist(this.getUUID());
                }
                this.assignedAltarPos = null;
            }
            this.ritualTime = 0; // Stop any active ritual immediately
            this.getNavigation().stop(); // Stop walking toward the altar
        }
    }

    @Nullable
    public UUID getLeadingPlayerUUID() {
        return leadingPlayerUUID;
    }

    public SinsList getSinType(){
        return this.sinType;
    }

    public static final EntityDataAccessor<Integer> DATA_CULTIST_TYPE = SynchedEntityData.defineId(EndermanCultistEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_RITUALIZING = SynchedEntityData.defineId(EndermanCultistEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CULTIST_TYPE, SinsList.NONE.ordinal());
        builder.define(IS_RITUALIZING, false);
    }

    public SinsList getSyncedType() {
        return SinsList.values()[this.entityData.get(DATA_CULTIST_TYPE)];
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_CULTIST_TYPE.equals(key) && this.level().isClientSide) {
            this.refreshDimensions();
        }

        if (IS_RITUALIZING.equals(key)) {
            if (this.isRitualizing()) {
                this.ritualAnimationState.startIfStopped(this.tickCount);
            } else {
                this.ritualAnimationState.stop();
            }
        }
    }

    public void setRitualizing(boolean ritualizing) {
        this.entityData.set(IS_RITUALIZING, ritualizing);
    }

    public boolean isRitualizing() {
        return this.entityData.get(IS_RITUALIZING);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        if (!this.level().isClientSide) {
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
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 60) {
            this.ritualAnimationState.start(this.tickCount);
        } else if (id == 62) {
            this.ritualAnimationState.stop();
        } else if (id == 61) {
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void setupAnimationStates() {
        if (this.isRitualizing() || this.isAggressive()) {
            this.idleAnimationState.stop();
            this.idleAnimationTimeout = 0;
            return;
        }

        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 60;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    @Override
    public void swing(InteractionHand hand, boolean updateSel) {
        super.swing(hand, updateSel);
        this.level().broadcastEntityEvent(this, (byte) 61); // Trigger Attack Anim
    }

    @Override
    protected void registerGoals() {

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                if (mob.getLastHurtByMob() instanceof Player player) {
                    if (EnderCultistHelmetItem.isEndermanFriendly(player)) {
                        return false;
                    }
                }
                return super.canUse();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Endermite.class, true));


        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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

    protected SoundEvent getAmbientSound(){
        if (this.isRitualizing()) {
            return null;
        }
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    public void die(DamageSource cause){
        super.die(cause);
        if (!this.level().isClientSide && this.assignedAltarPos != null) {
            if (this.level().getBlockEntity(this.assignedAltarPos) instanceof VoidAltarBlockEntity altar) {
                altar.removeCultist(this.getUUID());
            }
        }

    }


    @Override
    public void doPush(Entity entity) {

        super.doPush(entity);
    }

    private void validateOrFindAltar() {
        if (this.assignedAltarPos != null) {
            if (this.level().getBlockEntity(this.assignedAltarPos) instanceof VoidAltarBlockEntity altar) {
                if (!altar.isCultistAssigned(this.getUUID())) {
                    this.assignedAltarPos = null;
                }
            } else {
                this.assignedAltarPos = null;
            }
        }

        if (this.assignedAltarPos == null) {
            BlockPos bestPos = null;
            int highestTier = -1;

            for (BlockPos p : BlockPos.betweenClosed(this.blockPosition().offset(-16, -8, -16), this.blockPosition().offset(16, 8, 16))) {
                if (this.level().getBlockEntity(p) instanceof VoidAltarBlockEntity altar) {
                    if (altar.hasSpace()) {
                        int currentTier = altar.getAltarTier();
                        if (currentTier > highestTier) {
                            highestTier = currentTier;
                            bestPos = p.immutable();
                        }
                    }
                }
            }

            if (bestPos != null) {
                if (this.level().getBlockEntity(bestPos) instanceof VoidAltarBlockEntity bestAltar) {
                    if (bestAltar.inviteCultist(this)) {
                        this.assignedAltarPos = bestPos;
                        this.workCooldown = TheVoidCultConfig.CULTIST_WORK_COOLDOWN.get();
                    }
                }
            }
        }
    }

    public void setAssignedAltarPos(BlockPos assignedAltarPos) {
        this.assignedAltarPos = assignedAltarPos;
    }

    private void performWorkCycle() {
        Vec3 altarCenter = Vec3.atCenterOf(this.assignedAltarPos);
        double distSqr = this.distanceToSqr(altarCenter);

        if (distSqr > 16.0D) {
            this.getNavigation().moveTo(altarCenter.x, altarCenter.y, altarCenter.z, 1.0D);
        } else {
            this.ritualTime = TheVoidCultConfig.CULTIST_WORK_DURATION.get();
            this.getNavigation().stop();
        }
    }

    private void handleAltarState() {

        if (this.assignedAltarPos != null &&
                !(this.level().getBlockEntity(this.assignedAltarPos) instanceof VoidAltarBlockEntity)) {
            this.stopRitualManually();
            return;
        }

        if (this.blockPosition().closerThan(this.assignedAltarPos, 4.0D)) {

            if (this.ritualTime == TheVoidCultConfig.CULTIST_WORK_DURATION.get()) {
                this.getLookControl().setLookAt(assignedAltarPos.getX(), assignedAltarPos.getY()+3, assignedAltarPos.getZ()+3);
                this.setRitualizing(true);
                this.level().broadcastEntityEvent(this, (byte) 60);
            }

            this.doRitual();

            if (this.ritualTime <= 0 && this.isRitualizing()) {
                this.setRitualizing(false);
                this.level().broadcastEntityEvent(this, (byte) 62);
            }

        } else {

            if (this.isRitualizing()) {
                this.setRitualizing(false);
                this.level().broadcastEntityEvent(this, (byte) 62);
            }
            this.getNavigation().moveTo(assignedAltarPos.getX(), assignedAltarPos.getY(), assignedAltarPos.getZ(), 1.0D);
        }
    }

    public void setWorkCooldown(int x){
        this.workCooldown = x;
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() || this.isRitualizing();
    }

    private void stopRitualManually() {
        this.assignedAltarPos = null;
        this.ritualTime = 0;
        this.setRitualizing(false);
        this.level().broadcastEntityEvent(this, (byte) 62);
        this.getNavigation().stop();
    }

    private void doRitual(){
        this.ritualTime--;
        this.getNavigation().stop();
        this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        if (this.assignedAltarPos != null) {
            Vec3 altarCenter = Vec3.atCenterOf(this.assignedAltarPos);
            this.getLookControl().setLookAt(altarCenter.x, altarCenter.y + 3, altarCenter.z);
            this.yBodyRot = this.yHeadRot;
            this.yBodyRotO = this.yHeadRot;
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        altarCenter.x, altarCenter.y + 3, altarCenter.z,
                        2,
                        0.2, 0.2, 0.2,
                        0.05
                );
            }
        }
        if (this.ritualTime == 0) {
            if (this.assignedAltarPos != null && this.level().getBlockEntity(this.assignedAltarPos) instanceof VoidAltarBlockEntity altar) {
                altar.performWork(this.sinType);
                this.workCooldown = TheVoidCultConfig.CULTIST_WORK_COOLDOWN.get();
            }
        }
    }

    private void FollowLeader(){
        Player leader = this.level().getPlayerByUUID(this.leadingPlayerUUID);
        if (leader != null) {
            double distSqr = this.distanceToSqr(leader);

            if (distSqr > 576.0D) {
                this.teleportToEntity(leader);
            }
            else if (distSqr > 16.0D) {
                this.getNavigation().moveTo(leader, 1.2D);
            }
        }

        if (this.tickCount % 20 == 0 && this.leadingPlayerUUID != null) {
            if (leader != null && !EnderCultistHelmetItem.isEndermanFriendly(leader)) {
                this.setLeadingPlayer(null);

                leader.displayClientMessage(Component.translatable("message.thevoidcult.follower_stop"), true);
            }
        }

    }

    private boolean handleAggression() {
        LivingEntity target = this.getTarget();
        if(EnderCultistHelmetItem.isEndermanFriendly(target)) {
            this.setTarget(null);
            this.setLastHurtByMob(null);
            this.getNavigation().stop();
        }


        if (target != null && target.isAlive()) {
            if (this.isRitualizing()) {
                this.stopRitualManually();
                this.playSound(SoundEvents.ENDERMAN_STARE, 1.0F, 1.0F);
            }
            if(this.distanceToSqr(target) > 256.0D && this.random.nextInt(100) == 0){
                this.teleportToEntity(target);
            }

            return true;
        }

        return false;
    }
    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive()) {

            if (this.tickCount % 600 == 0) {
                this.heal(1.0F);
            }


            if (this.ritualTime > 0) {
                handleAltarState();
                return;
            }

            if (this.handleAggression()) {
                return;
            }

            if (this.leadingPlayerUUID != null) {
                FollowLeader();
                return;
            }

            if(this.workCooldown > 0) {
                --this.workCooldown;
            }
            else {
                if (this.tickCount % 100 == 0) {
                    validateOrFindAltar();
                }

                if (this.assignedAltarPos != null && this.workCooldown <= 0) {
                    performWorkCycle();
                }
            }
            if (this.tickCount % 100 == 0 && this.assignedAltarPos != null) {
                double distSqr = this.distanceToSqr(Vec3.atCenterOf(this.assignedAltarPos));

                if (distSqr > 1024.0D) {
                    this.getNavigation().moveTo(
                            this.assignedAltarPos.getX() + 2,
                            this.assignedAltarPos.getY(),
                            this.assignedAltarPos.getZ() + 2,
                            1.0D
                    );
                }
            }



        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("CultistType", this.sinType.name());
        compound.putInt("WorkCooldown", this.workCooldown);
        compound.putInt("RitualTime", this.ritualTime);

        if (this.assignedAltarPos != null) {
            compound.putLong("AssignedAltar", this.assignedAltarPos.asLong());
        }

        if (this.leadingPlayerUUID != null) {
            compound.putUUID("LeadingPlayer", this.leadingPlayerUUID);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("CultistType")) {
            try {
                this.sinType = SinsList.valueOf(compound.getString("CultistType"));
            } catch (IllegalArgumentException e) {
                this.sinType = SinsList.NONE;
            }
        }
        this.workCooldown = compound.getInt("WorkCooldown");
        this.ritualTime = compound.getInt("RitualTime");

        if (compound.contains("AssignedAltar")) {
            this.assignedAltarPos = BlockPos.of(compound.getLong("AssignedAltar"));
        }

        if (compound.hasUUID("LeadingPlayer")) {
            this.leadingPlayerUUID = compound.getUUID("LeadingPlayer");
        }

        this.entityData.set(DATA_CULTIST_TYPE, this.sinType.ordinal());
    }

    public void clearAssignedAltar(){
        this.assignedAltarPos = null;
        this.ritualTime = 0;
        this.getNavigation().stop();
    }

    @Override
    public boolean isSensitiveToWater() {
        return false;
    }

    protected void teleportRandomly() {

        if (!this.level().isClientSide() && this.isAlive()) {
            for(int i = 0; i < 64; ++i) {
                double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 16.0D;
                double d1 = this.getY() + (double)(this.random.nextInt(16) - 8);
                double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 16.0D;

                if (this.randomTeleport(d0, d1, d2, true)) {
                    EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(this, d0, d1, d2);
                    if (NeoForge.EVENT_BUS.post(event).isCanceled()) continue;
                    this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.CHORUS_FRUIT_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                    this.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                    break;
                }
            }
        }
    }

    protected void teleportToEntity(Entity entity) {

        if(teleportCooldown > 0) return;
        for (int i = 0; i < 16; ++i) {
            double d0 = entity.getX() + (this.random.nextDouble() - 0.5D) * 8.0D;
            double d1 = entity.getY() + (double)(this.random.nextInt(8) - 4);
            double d2 = entity.getZ() + (double)(this.random.nextFloat() - 0.5D) * 8.0D;

            if (this.randomTeleport(d0, d1, d2, true)) {
                this.teleportCooldown = 60;
                if(this.matterCreationCooldown <= 0) {
                    EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(this, d0, d1, d2);
                    if (NeoForge.EVENT_BUS.post(event).isCanceled()) continue;
                }
                this.matterCreationCooldown = 400;

                this.level().playSound(null, this.xo, this.yo, this.zo, SoundEvents.CHORUS_FRUIT_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
                this.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1.0F, 1.0F);
                break;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {

        if(this.isRitualizing()) this.stopRitualManually();

        if (source.getDirectEntity() instanceof AbstractArrow) {
            this.teleportRandomly();
            return false;
        }

        boolean flag = super.hurt(source, amount);

        if (!this.level().isClientSide() && flag && this.random.nextFloat() < 0.2f) {
            this.teleportRandomly();
        }

        return flag;
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

    @Override
    public void tick(){
        super.tick();


        if(this.level().isClientSide) {
            this.setupAnimationStates();

            this.level().addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 2.0, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 2.0);

            if(this.tickCount%4 == 0){
                SimpleParticleType sinParticle = switch (this.getSyncedType()) {
                    case WRATH -> ParticleTypes.LAVA;
                    case GLUTTONY -> ParticleTypes.FALLING_LAVA;
                    case GREED -> ParticleTypes.TOTEM_OF_UNDYING;
                    case ENVY -> ParticleTypes.WITCH;
                    case PRIDE -> ParticleTypes.HAPPY_VILLAGER;
                    default -> ParticleTypes.PORTAL;
                };
                this.level().addParticle(sinParticle, this.getRandomX(0.5), this.getRandomY() - 0.25, this.getRandomZ(0.5), (this.random.nextDouble() - 0.5) * 0.5, -this.random.nextDouble(), (this.random.nextDouble() - 0.5) * 0.5);
            }

        }else
        {
            if (this.isInWaterOrBubble()) {
                this.hurt(this.damageSources().magic(), 1.0F);
                if (this.random.nextFloat() < 0.2F) {
                    this.teleportRandomly();
                }
            }
            if(this.teleportCooldown > 0) --this.teleportCooldown;
            if(this.matterCreationCooldown > 0) --this.matterCreationCooldown;

        }
    }
}

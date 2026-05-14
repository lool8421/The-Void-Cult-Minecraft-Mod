package com.thevoidcult.blockEntities;

import com.thevoidcult.items.SinsList;
import com.thevoidcult.main.TheVoidCultConfig;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VoidAltarBlockEntity extends BlockEntity {

    public int AltarTier = 0;
    private int CultistLimit = 0;
    private int NearbyEndCrystals = 0;
    public final Set<UUID> workerIds = new HashSet<>();
    private boolean isInterfered = false;


    public int getAltarTier() {
        return this.AltarTier;
    }

    private void calculateAltarTier(Level level, BlockPos pos) {

        isAnotherAltarNearby(level, pos);
        if(this.isInterfered) {this.AltarTier = 0; return;}

        if (!checkLayer(level, pos.below(1), 1, Blocks.CRYING_OBSIDIAN)) {
            this.AltarTier = 0;
            return;
        }
        if (!checkLayer(level, pos.below(2), 2, RegisterContent.WARPED_IRON_BLOCK.get())) {
            this.AltarTier = 1;
            return;
        }
        if (!checkLayer(level, pos.below(3), 3, RegisterContent.WARPED_GOLD_BLOCK.get())) {
            this.AltarTier = 2;
            return;
        }
        if (!checkLayer(level, pos.below(4), 4, RegisterContent.WARPED_DIAMOND_BLOCK.get())) {
            this.AltarTier = 3;
            return;
        }
        if (!checkLayer(level, pos.below(5), 5, RegisterContent.WARPED_NETHERITE_BLOCK.get())) {
            this.AltarTier = 4;
            return;
        }

        this.AltarTier = 5;
    }

    public boolean hasSpace() {
        return this.workerIds.size() < this.CultistLimit;
    }

    private void isAnotherAltarNearby(Level level, BlockPos pos){
        BlockPos corner1 = pos.offset(-9, 0, -9);
        BlockPos corner2 = pos.offset(9, 0, 9);

        for (BlockPos otherPos : BlockPos.betweenClosed(corner1, corner2)) {
            if (otherPos.equals(pos)) continue;
            if (level.getBlockEntity(otherPos) instanceof VoidAltarBlockEntity) {
                this.isInterfered = true;
                return;
            }
        }
        this.isInterfered = false;
    }

    private boolean checkLayer(Level level, BlockPos center, int radius, Block targetBlock) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (!level.getBlockState(center.offset(x, 0, z)).is(targetBlock)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void countNearbyEndCrystals() {
        if (this.level == null) return;

        int radius = TheVoidCultConfig.ALTAR_CRYSTAL_RANGE.get();
        AABB scanArea = new AABB(this.worldPosition).inflate(radius);
        List<EndCrystal> crystals = this.level.getEntitiesOfClass(EndCrystal.class, scanArea);
        this.NearbyEndCrystals = Math.min(crystals.size(), TheVoidCultConfig.ALTAR_MAX_CRYSTALS.get());
    }

    private void updateCultistLimit(){
        int previousCap = this.CultistLimit;

        this.AltarTier = this.getAltarTier();
        if(this.AltarTier == 0) {this.CultistLimit = 0; purgeCultists(); return;}
        countNearbyEndCrystals();

        this.CultistLimit = TheVoidCultConfig.ALTAR_BASE_CAP.get();
        this.CultistLimit += TheVoidCultConfig.ALTAR_CAP_PER_TIER.get() * this.AltarTier;
        this.CultistLimit += TheVoidCultConfig.ALTAR_CAP_PER_CRYSTAL.get() * this.NearbyEndCrystals;
        this.CultistLimit = Math.clamp(this.CultistLimit, 0, TheVoidCultConfig.ALTAR_OVERALL_CAP.get());

        if(this.CultistLimit < previousCap) purgeCultists();
    }

    public int getCultistLimit(){
        updateCultistLimit();
        return this.CultistLimit;
    }

    private void purgeCultists(){
        if (this.workerIds.isEmpty()) return;

        if (this.AltarTier == 0) {
            this.workerIds.clear();
        }
        else {
            while (this.workerIds.size() > this.CultistLimit) {
                // Remove the first UUID found in the set
                UUID toRemove = this.workerIds.iterator().next();
                this.workerIds.remove(toRemove);
            }
        }
        this.setChanged();
        return;
    }

    public void removeCultist(UUID uuid) {
        if (this.workerIds.remove(uuid)) {
            this.setChanged();
        }
    }

    public boolean inviteCultist(EndermanCultistEntity cultist) {
        UUID cultistUUID = cultist.getUUID();

        if (this.workerIds.contains(cultistUUID)) {
            return true;
        }
        if (this.AltarTier <= 0) {
            return false;
        }
        if (this.workerIds.size() < this.CultistLimit) {
            this.workerIds.add(cultistUUID);

            this.setChanged();

            return true;
        }
        return false;
    }

    public VoidAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RegisterContent.VOID_ALTAR_BE.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad(); // Always call this first!
        if (this.level != null && !this.level.isClientSide) {
            this.calculateAltarTier(this.level, this.worldPosition);
            this.countNearbyEndCrystals();
        }
    }

    public int getActiveWorkerCount() {
        return this.workerIds.size();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("altarTier", this.AltarTier);
        tag.putInt("maxWorkers", this.CultistLimit);

        ListTag list = new ListTag();
        for (UUID id : workerIds) {
            list.add(NbtUtils.createUUID(id));
        }
        tag.put("Workers", list); // Saving with key "Workers"
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.AltarTier = tag.getInt("altarTier");
        this.CultistLimit = tag.getInt("maxWorkers");

        this.workerIds.clear(); // Always clear before loading!

        if (tag.contains("Workers", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Workers", Tag.TAG_INT_ARRAY);
            for (int i = 0; i < list.size(); i++) {
                UUID loadedId = NbtUtils.loadUUID(list.get(i));
                if (loadedId != null) {
                    this.workerIds.add(loadedId);
                }
            }
        }
    }

    public void releaseAllWorkers() {
        if (this.level instanceof ServerLevel serverLevel) {
            for (UUID uuid : this.workerIds) {
                Entity entity = serverLevel.getEntity(uuid);
                if (entity instanceof EndermanCultistEntity cultist) {
                    cultist.clearAssignedAltar();
                }
            }
        }
        this.workerIds.clear();
        this.setChanged();
    }

    private void spawnAmbientParticles(Level level, BlockPos pos) {
        RandomSource random = level.random;
        for (int i = 0; i < this.AltarTier; i++) {
            level.addParticle(ParticleTypes.PORTAL,
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5),
                    pos.getY() + 1.1,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5),
                    0, 0.1, 0);
        }
    }

    private Block getNextRequiredBlock(int currentTier) {
        return switch (currentTier) {
            case 0 -> Blocks.CRYING_OBSIDIAN;
            case 1 -> RegisterContent.WARPED_IRON_BLOCK.get();
            case 2 -> RegisterContent.WARPED_GOLD_BLOCK.get();
            case 3 -> RegisterContent.WARPED_DIAMOND_BLOCK.get();
            case 4 -> RegisterContent.WARPED_NETHERITE_BLOCK.get();
            default -> Blocks.AIR;
        };
    }

    public boolean isCultistAssigned(UUID uuid) {
        return this.workerIds.contains(uuid);
    }

    public void performWork(EndermanCultistEntity cultist, SinsList sinType) {
        if (!this.workerIds.contains(cultist.getUUID())) return;

        int tier = this.getAltarTier();

        Component debugMessage = Component.literal("Ritual complete - ")
                .append(Component.literal(sinType.name()).withStyle(ChatFormatting.DARK_PURPLE))
                .append(Component.literal(" Tier " + tier).withStyle(ChatFormatting.GOLD));

        if (this.level != null && !this.level.isClientSide) {
            for (Player player : this.level.players()) {
                if (player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) < 1024.0D) { // 32 block radius
                    player.displayClientMessage(debugMessage, false);
                }
            }
        }

    }

    public void sendAltarStatus(Player player) {
        player.displayClientMessage(Component.translatable("message.thevoidcult.altar_status_header")
                .withStyle(ChatFormatting.DARK_PURPLE), false);

        player.sendSystemMessage(Component.translatable("message.thevoidcult.nearby_crystals",
                this.NearbyEndCrystals,
                TheVoidCultConfig.ALTAR_MAX_CRYSTALS.get()));

        MutableComponent tierComp = Component.translatable("message.thevoidcult.altar_tier", this.getAltarTier());
        if (this.getAltarTier() == 0 && this.isInterfered) {
            tierComp.append(Component.translatable("message.thevoidcult.altar_interference").withStyle(ChatFormatting.RED));
        }
        player.sendSystemMessage(tierComp.withStyle(this.getAltarTier() == 0 ? ChatFormatting.RED : ChatFormatting.GOLD));

        if (this.getAltarTier() < 5) {
            Block nextBlock = getNextRequiredBlock(this.getAltarTier());
            int side = (2 * this.getAltarTier()) + 3;
            int requiredCount = side * side;

            player.sendSystemMessage(Component.translatable("message.thevoidcult.next_layer",
                            requiredCount,
                            Component.translatable(nextBlock.getDescriptionId()))
                    .withStyle(ChatFormatting.GRAY));
        }

        player.sendSystemMessage(Component.translatable("message.thevoidcult.assigned_cultists",
                this.getActiveWorkerCount(),
                this.getCultistLimit()));
        player.displayClientMessage(Component.translatable("message.thevoidcult.altar_status_footer")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VoidAltarBlockEntity be) {
        if (level.isClientSide) {
            // Client-side: Only play ambient particles based on the tier
            if (level.getGameTime() % 20 == 0 && be.AltarTier > 0) {
                be.spawnAmbientParticles(level, pos);
            }
            return;
        }


        if(level.getGameTime()%20 == 0){
        ((ServerLevel)level).sendParticles(ParticleTypes.REVERSE_PORTAL,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                be.AltarTier*3, 0.2, 0.2, 0.2, 0.05);
            if (level.getGameTime() % 100 == 0) {
                be.calculateAltarTier(level, pos);
                be.updateCultistLimit();
            }
        }
    }
}
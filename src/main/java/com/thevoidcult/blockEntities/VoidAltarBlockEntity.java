package com.thevoidcult.blockEntities;

import com.thevoidcult.items.SinsList;
import com.thevoidcult.main.TheVoidCultConfig;
import com.thevoidcult.mobs.custom.EndermanCultistEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class VoidAltarBlockEntity extends BlockEntity {

    public int AltarTier = 0;
    private int CultistLimit = 0;
    private int NearbyEndCrystals = 0;
    public final Set<UUID> workerIds = new HashSet<>();
    public final Set<BlockPos> effigyPos = new HashSet<>();
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
        return this.getOccupiedSpace() < this.CultistLimit;
    }

    public boolean hasSpaceForEffigy() {
        return this.getOccupiedSpace() + TheVoidCultConfig.EFFIGY_SLOT_USAGE.get() <= this.CultistLimit;
    }

    private void unlinkAllEffigies() {
        if (this.level != null) {
            for (BlockPos pos : this.effigyPos) {
                if (this.level.isLoaded(pos)
                        && this.level.getBlockEntity(pos) instanceof EnderEffigyBlockEntity effigy) {
                    effigy.setLinkedAltar(null);
                }
            }
        }
        this.effigyPos.clear();
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

        if(this.AltarTier == 0) {this.CultistLimit = 0; purgeExcessWorkers(); return;}
        countNearbyEndCrystals();

        this.CultistLimit = TheVoidCultConfig.ALTAR_BASE_CAP.get();
        this.CultistLimit += TheVoidCultConfig.ALTAR_CAP_PER_TIER.get() * this.AltarTier;
        this.CultistLimit += TheVoidCultConfig.ALTAR_CAP_PER_CRYSTAL.get() * this.NearbyEndCrystals;
        this.CultistLimit = Math.clamp(this.CultistLimit, 0, TheVoidCultConfig.ALTAR_OVERALL_CAP.get());

        if(this.CultistLimit < previousCap) purgeExcessWorkers();
    }

    public int getCultistLimit(){
        updateCultistLimit();
        return this.CultistLimit;
    }

    private void purgeExcessWorkers() {
        if (this.workerIds.isEmpty() && this.effigyPos.isEmpty()) {
            return;
        }

        if (this.AltarTier <= 0) {
            this.workerIds.clear();
            this.unlinkAllEffigies();
            this.setChanged();
            return;
        }

        boolean changed = false;

        while (this.getOccupiedSpace() > this.CultistLimit && !this.workerIds.isEmpty()) {
            UUID toRemove = this.workerIds.iterator().next();
            this.workerIds.remove(toRemove);
            changed = true;
        }

        while (this.getOccupiedSpace() > this.CultistLimit && !this.effigyPos.isEmpty()) {
            BlockPos pos = this.effigyPos.iterator().next();
            this.effigyPos.remove(pos);

            if (this.level != null && this.level.isLoaded(pos)
                    && this.level.getBlockEntity(pos) instanceof EnderEffigyBlockEntity effigy) {
                effigy.setLinkedAltar(null);
            }
            changed = true;
        }

        if (changed) {
            this.setChanged();
        }
    }

    public void removeCultist(UUID uuid) {
        if (this.workerIds.remove(uuid)) {
            this.setChanged();
        }
    }

    public void removeNonexistentCultists(ServerLevel level) { //runs every 30s just so the altar doesn't get clogged with ghost UUIDs
        List<UUID> toRemove = new ArrayList<>();

        for (UUID uuid : this.workerIds) {
            Entity entity = level.getEntity(uuid);
            if (entity == null || !entity.isAlive()) {
                toRemove.add(uuid);
            }
        }
        for (UUID uuid : toRemove) {
            this.removeCultist(uuid);
        }
    }

    private void inviteEffigy(EnderEffigyBlockEntity effigy) {
        BlockPos pos = effigy.getBlockPos().immutable();
        if (this.effigyPos.add(pos)) {
            effigy.setLinkedAltar(this.worldPosition);
            setChanged();
        }
    }

    public void inviteNearbyEffigies() {
        if (this.level == null || !this.hasSpaceForEffigy()) {
            return;
        }

        BlockPos minPos = this.worldPosition.offset(-2, -1, -2);
        BlockPos maxPos = this.worldPosition.offset(2, 1, 2);

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            if (!this.hasSpaceForEffigy()) {
                break;
            }

            if (this.level.getBlockEntity(pos) instanceof EnderEffigyBlockEntity effigy
                    && effigy.getLinkedAltar() == null) {
                inviteEffigy(effigy);
            }
        }
    }

    public boolean amILinked(EnderEffigyBlockEntity effigy) {
        return effigy != null && this.effigyPos.contains(effigy.getBlockPos());
    }

    public void removeEffigy(BlockPos pos) {
        if (this.effigyPos.remove(pos)) {
            setChanged();
        }
    }

    public void removeNonexistentEffigies(ServerLevel level) {
        List<BlockPos> toRemove = new ArrayList<>();

        for (BlockPos pos : this.effigyPos) {
            // Check isLoaded first so unloaded chunks don't falsely unlink valid effigies
            if (!level.isLoaded(pos)) {
                continue;
            }

            if (!(level.getBlockEntity(pos) instanceof EnderEffigyBlockEntity effigy)
                    || !this.worldPosition.equals(effigy.getLinkedAltar())) {
                toRemove.add(pos);
            }
        }

        if (!toRemove.isEmpty()) {
            for (BlockPos pos : toRemove) {
                this.effigyPos.remove(pos);
            }
            setChanged();
        }
    }

    public void inviteNearbyCultists(){

        if (this.hasSpace()) {
            AABB searchBox = new AABB(this.getBlockPos()).inflate(8.0, 4.0, 8.0);
            List<EndermanCultistEntity> nearbyCultists = level.getEntitiesOfClass(
                    EndermanCultistEntity.class,
                    searchBox,
                    cultist -> cultist.isAlive() && !cultist.hasAssignedAltar()
            );

            for(EndermanCultistEntity cultist : nearbyCultists){
                    if(!inviteCultist(cultist)) break;
            }

        }
    }

    public void tryAssignNearbyFollowers(Player player) {
        if (this.level == null) {
            return;
        }

        List<EndermanCultistEntity> followers = this.level.getEntitiesOfClass(
                EndermanCultistEntity.class,
                player.getBoundingBox().inflate(16.0D),
                c -> player.getUUID().equals(c.getLeadingPlayerUUID())
        );

        if (followers.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.thevoidcult.assigned_no_followers"), true);
            return;
        }

        int assignedCount = 0;
        boolean hitCapacity = false;

        for (EndermanCultistEntity candidate : followers) {
            if (this.inviteCultist(candidate)) {
                candidate.setWorkCooldown(TheVoidCultConfig.CULTIST_WORK_COOLDOWN.get());
                candidate.setLeadingPlayer(null);
                candidate.setAssignedAltarPos(this.worldPosition);
                assignedCount++;
            } else {
                hitCapacity = true;
                break;
            }
        }

        if (assignedCount > 0) {
            player.displayClientMessage(
                    Component.translatable("message.thevoidcult.assigned_count", assignedCount),
                    true
            );
        } else if (hitCapacity) {
            player.displayClientMessage(
                    Component.translatable("message.thevoidcult.assigned_max_altar"),
                    true
            );
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

        if (cultist.hasAssignedAltar() && !this.worldPosition.equals(cultist.getAssignedAltarPos())) {
            return false;
        }

        if (this.hasSpace()) {
            this.workerIds.add(cultistUUID);
            cultist.setAssignedAltarPos(this.worldPosition);
            cultist.setLeadingPlayer(null);
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

    public int getOccupiedSpace() {
        return this.workerIds.size() + this.effigyPos.size()*TheVoidCultConfig.EFFIGY_SLOT_USAGE.get();
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
        tag.put("Workers", list);

        long[] packedPositions = this.effigyPos.stream()
                .mapToLong(BlockPos::asLong)
                .toArray();
        tag.putLongArray("EffigyPositions", packedPositions);


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
                    this.workerIds.add(loadedId);
            }
        }

        this.effigyPos.clear();
        if (tag.contains("EffigyPositions")) {
            for (long packed : tag.getLongArray("EffigyPositions")) {
                this.effigyPos.add(BlockPos.of(packed));
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

    private void spawnPortalMatterBonus() {

        double portalMatterChance = TheVoidCultConfig.ALTAR_MATTER_DROP_CHANCE.get() + (TheVoidCultConfig.ALTAR_MATTER_DROP_CHANCE_PER_TIER.get() * this.AltarTier);

        if (level.getRandom().nextDouble() < portalMatterChance) {
            double spawnX = this.getBlockPos().getX() + 0.5;
            double spawnY = this.getBlockPos().getY() + 3.0;
            double spawnZ = this.getBlockPos().getZ() + 0.5;

            ItemStack bonus = new ItemStack(RegisterContent.PORTAL_MATTER.get(), 1);
            ItemEntity entity = new ItemEntity(level, spawnX, spawnY, spawnZ, bonus);
            entity.setDeltaMovement(0, 0, 0);

            level.addFreshEntity(entity);
        }
    }

    private void spawnLootFromTable(ResourceLocation tableLocation) {
        if (this.level == null || this.level.isClientSide) return;

        ServerLevel serverLevel = (ServerLevel) this.level;
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, tableLocation);
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(key);
        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
                .create(LootContextParamSets.EMPTY);

        List<ItemStack> items = lootTable.getRandomItems(params);

        double spawnX = this.worldPosition.getX() + 0.5;
        double spawnY = this.worldPosition.getY() + 3.0;
        double spawnZ = this.worldPosition.getZ() + 0.5;

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(serverLevel, spawnX, spawnY, spawnZ, stack.copy());
                itemEntity.setDeltaMovement(0, 0, 0);
                serverLevel.addFreshEntity(itemEntity);
            }
        }
    }

    public boolean performWork(SinsList sinType) {
        if (sinType == SinsList.NONE) return false;

        //some cap to items so it doesn't kill the server
        BlockPos pos = this.getBlockPos();
        AABB scanZone = new AABB(pos).inflate(1.0D, 5.0D, 1.0D);
        int nearbyItems = level.getEntitiesOfClass(ItemEntity.class, scanZone).size();
        if (nearbyItems >= TheVoidCultConfig.ALTAR_ITEM_CAPACITY.get()) {

            if (level instanceof ServerLevel serverLevel) {
                DustParticleOptions redDust = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
                serverLevel.sendParticles(redDust, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, 10, 0.15D, 0.05D, 0.15D, 0.0D);
            }
            return false;
        }
        if(this.AltarTier == 0) return false;

        String sinName = sinType.name().toLowerCase();

        ResourceLocation tableLocation = ResourceLocation.fromNamespaceAndPath("thevoidcult", "thevoidcult_rituals/ritual_" + sinName + "_" + this.AltarTier);

        if(level instanceof ServerLevel serverLevel)
            serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5D, pos.getY() + 1D, pos.getZ() + 0.5D, 10, 0.15D, 0.05D, 0.15D, 0.0D);
        this.spawnLootFromTable(tableLocation);
        this.spawnPortalMatterBonus();
        return true;
    }

    public void sendAltarStatus(Player player) {
        player.displayClientMessage(Component.translatable("message.thevoidcult.altar_status_header")
                .withStyle(ChatFormatting.DARK_PURPLE), false);

        player.sendSystemMessage(Component.translatable("message.thevoidcult.nearby_crystals",
                this.NearbyEndCrystals,
                TheVoidCultConfig.ALTAR_MAX_CRYSTALS.get()));

        MutableComponent tierComp = Component.translatable("message.thevoidcult.altar_tier", this.AltarTier);
        if (this.AltarTier == 0 && this.isInterfered) {
            tierComp.append(Component.translatable("message.thevoidcult.altar_interference").withStyle(ChatFormatting.RED));
        }
        player.sendSystemMessage(tierComp.withStyle(this.AltarTier == 0 ? ChatFormatting.RED : ChatFormatting.GOLD));

        if (this.AltarTier < 5) {
            Block nextBlock = getNextRequiredBlock(this.AltarTier);
            int side = (2 * this.AltarTier) + 3;
            int requiredCount = side * side;

            player.sendSystemMessage(Component.translatable("message.thevoidcult.next_layer",
                            requiredCount,
                            Component.translatable(nextBlock.getDescriptionId()))
                    .withStyle(ChatFormatting.GRAY));
        }

        player.sendSystemMessage(Component.translatable("message.thevoidcult.assigned_cultists",
                this.getOccupiedSpace(),
                this.getCultistLimit(),
                this.workerIds.size(),
                this.effigyPos.size()));

        player.displayClientMessage(Component.translatable("message.thevoidcult.altar_status_footer")
                .withStyle(ChatFormatting.DARK_PURPLE), false);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VoidAltarBlockEntity be) {


        if(level.getGameTime()%10 == 0){
        ((ServerLevel)level).sendParticles(ParticleTypes.WITCH,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                be.AltarTier*3, 0.2, 0.2, 0.2, 0.05);
            if (level.getGameTime() % 100 == 0) {
                be.calculateAltarTier(level, pos);
                be.updateCultistLimit();
                be.inviteNearbyEffigies();
                be.inviteNearbyCultists();
                if(level.getGameTime() % 300 == 0){
                    be.removeNonexistentCultists((ServerLevel)level);
                    be.removeNonexistentEffigies((ServerLevel)level);
                }
            }
        }
    }
}
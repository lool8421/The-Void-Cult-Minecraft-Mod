package com.thevoidcult.blockEntities;

import com.thevoidcult.items.SinsList;
import com.thevoidcult.main.TheVoidCultConfig;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.Properties;

public class EnderEffigyBlockEntity extends BlockEntity {

    private SinsList sinType = SinsList.NONE;
    @Nullable
    private BlockPos linkedAltar = null;
    private int workCooldown = TheVoidCultConfig.EFFIGY_WORK_COOLDOWN.get();

    public SinsList getSinType(){
        return this.sinType;
    }

    public void setSinType(SinsList newType) {
        this.sinType = newType;
        this.workCooldown = TheVoidCultConfig.EFFIGY_WORK_COOLDOWN.get();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
        this.syncToClient();
    }

    public BlockPos getLinkedAltar() {
        return this.linkedAltar;
    }

    public void setLinkedAltar(@Nullable BlockPos altarPos) {
        this.linkedAltar = altarPos;

        if (this.level != null && !this.level.isClientSide()) {
            boolean shouldBeActive = (altarPos != null);
            BlockState state = getBlockState();
            if (state.hasProperty(EnderEffigyBlock.ACTIVE) && state.getValue(EnderEffigyBlock.ACTIVE) != shouldBeActive) {
                this.level.setBlock(this.worldPosition, state.setValue(EnderEffigyBlock.ACTIVE, shouldBeActive), Block.UPDATE_ALL);
            }
        }

        this.syncToClient();
    }

    public EnderEffigyBlockEntity(BlockPos pos, BlockState state) {
        super(RegisterContent.ENDER_EFFIGY_BE.get(), pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("SinType", this.sinType.name());
        tag.putInt("WorkCooldown", this.workCooldown);
        if (this.linkedAltar != null) {
            tag.put("LinkedAltar", NbtUtils.writeBlockPos(this.linkedAltar));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("SinType")) {
            try {
                this.sinType = SinsList.valueOf(tag.getString("SinType"));
            } catch (IllegalArgumentException e) {
                this.sinType = SinsList.NONE;
            }
        }
        this.workCooldown = tag.getInt("WorkCooldown");
        if (tag.contains("LinkedAltar")) {
            NbtUtils.readBlockPos(tag, "LinkedAltar").ifPresent(pos -> this.linkedAltar = pos);
        } else {
            this.linkedAltar = null;
        }
    }

    private void syncToClient() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EnderEffigyBlockEntity effigy) {
        if (!level.isClientSide()) {
            boolean isAttuned = effigy.sinType != SinsList.NONE;
            boolean isActive = effigy.linkedAltar != null;
            if (state.hasProperty(EnderEffigyBlock.ACTIVE) && state.getValue(EnderEffigyBlock.ACTIVE) != isActive) {
                state = state.setValue(EnderEffigyBlock.ACTIVE, isActive);
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }

            if (!isActive) {
                return;
            }

            if (effigy.linkedAltar == null) {
                return;
            }

            if (level.getGameTime() % 20 == 0) {
                if (level.getBlockEntity(effigy.linkedAltar) instanceof VoidAltarBlockEntity altar) {
                    if (!altar.amILinked(effigy)) {
                        effigy.setLinkedAltar(null);
                        return;
                    }
                } else if (level.isLoaded(effigy.linkedAltar)) {
                    effigy.setLinkedAltar(null);
                    return;
                }
            }

            if (effigy.workCooldown > 0) {
                effigy.workCooldown--;
                return;
            }

            if (level.getBlockEntity(effigy.linkedAltar) instanceof VoidAltarBlockEntity altar && altar.amILinked(effigy)) {
                altar.performWork(effigy.sinType);
                effigy.workCooldown = TheVoidCultConfig.EFFIGY_WORK_COOLDOWN.get();
            } else if (level.isLoaded(effigy.linkedAltar)) {
                effigy.setLinkedAltar(null);
            }
            return;
        }

        if (state.getValue(EnderEffigyBlock.ACTIVE) && effigy.sinType != SinsList.NONE && level.random.nextFloat() < 0.25f) {
            Vector3f color = effigy.sinType.getParticleColor();
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.6 + level.random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.6;

            level.addParticle(new DustParticleOptions(color, 1.0f), x, y, z, 0.0, 0.02, 0.0);
        }
    }


}
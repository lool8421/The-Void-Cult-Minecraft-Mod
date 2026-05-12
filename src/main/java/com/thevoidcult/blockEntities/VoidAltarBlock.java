package com.thevoidcult.blockEntities;

import com.mojang.serialization.MapCodec;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class VoidAltarBlock extends BaseEntityBlock {
    // 1. Define the Codec
    public static final MapCodec<VoidAltarBlock> CODEC = simpleCodec(VoidAltarBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public VoidAltarBlock(Properties properties) {
        super(properties);
    }

    // 2. Ensure it has a model (BaseEntityBlock defaults to INVISIBLE)
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 3. Create the Block Entity instance
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RegisterContent.VOID_ALTAR_BE.get().create(pos, state);
    }

    // 4. Set up the Ticker
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // Only tick on the server side to save resources
        return level.isClientSide ? null : createTickerHelper(type, RegisterContent.VOID_ALTAR_BE.get(), VoidAltarBlockEntity::tick);
    }
}
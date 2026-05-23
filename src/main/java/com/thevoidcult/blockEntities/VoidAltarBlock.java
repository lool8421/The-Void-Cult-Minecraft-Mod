package com.thevoidcult.blockEntities;

import com.mojang.serialization.MapCodec;
import com.thevoidcult.items.EnderCultistHelmetItem;
import com.thevoidcult.items.SinsList;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class VoidAltarBlock extends BaseEntityBlock {
    // 1. Define the Codec
    public static final MapCodec<VoidAltarBlock> CODEC = simpleCodec(VoidAltarBlock::new);

    private static final VoxelShape BASE_1 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final VoxelShape BASE_2 = Block.box(2.0D, 2.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    private static final VoxelShape BASE_3 = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 6.0D, 12.0D);
    private static final VoxelShape PILLAR_1 = Block.box(3.0D, 6.0D, 3.0D, 5.0D, 15.0D, 5.0D);
    private static final VoxelShape PILLAR_2 = Block.box(11.0D, 6.0D, 11.0D, 13.0D, 15.0D, 13.0D);
    private static final VoxelShape PILLAR_3 = Block.box(3.0D, 6.0D, 11.0D, 5.0D, 15.0D, 13.0D);
    private static final VoxelShape PILLAR_4 = Block.box(11.0D, 6.0D, 3.0D, 13.0D, 15.0D, 5.0D);

    private static final VoxelShape SHAPE = Shapes.or(BASE_1, BASE_2, BASE_3, PILLAR_1, PILLAR_2, PILLAR_3, PILLAR_4);

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (level.getBlockEntity(pos) instanceof VoidAltarBlockEntity altar) {

                if (heldItem.is(RegisterContent.CULT_LEADER_STAFF.get())) {
                    return InteractionResult.SUCCESS;
                }

                if (heldItem.is(RegisterContent.PORTAL_MATTER.get())) {
                    SinsList[] sins = SinsList.values();
                    SinsList randomSin = sins[level.random.nextInt(sins.length)];

                    if (altar.performWork(randomSin)) {
                        if (!player.getAbilities().instabuild) {
                            heldItem.shrink(1);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    else
                        return InteractionResult.PASS;
                }
            }


            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VoidAltarBlockEntity altar) {
                altar.sendAltarStatus(player);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }



    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof VoidAltarBlockEntity altar) {
                altar.releaseAllWorkers();

                altar.workerIds.clear();
                altar.AltarTier = 0;
            }
            level.removeBlockEntity(pos);
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

}
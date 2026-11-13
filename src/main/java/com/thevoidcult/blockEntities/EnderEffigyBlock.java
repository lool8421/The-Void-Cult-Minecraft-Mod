package com.thevoidcult.blockEntities;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thevoidcult.items.SinsList;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class EnderEffigyBlock extends BaseEntityBlock {

    public static final MapCodec<EnderEffigyBlock> CODEC = simpleCodec(EnderEffigyBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape BASE_1 = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final VoxelShape BASE_2 = Block.box(6.0D, 2.0D, 6.0D, 10.0D, 15.0D, 10.0D);
    private static final VoxelShape SHAPE = Shapes.or(BASE_1, BASE_2);

    public EnderEffigyBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        //sneak disables auto-rotation towards altar
        if (!context.isSecondaryUseActive()) {
            Direction altarDir = findNearestAltarDirection(context.getLevel(), context.getClickedPos());
            if (altarDir != null) {
                return this.defaultBlockState().setValue(FACING, altarDir);
            }
        }

        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    private Direction findNearestAltarDirection(Level level, BlockPos pos) {
        BlockPos closestAltar = null;
        double closestDistSq = Double.MAX_VALUE;

        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
            if (level.getBlockEntity(checkPos) instanceof VoidAltarBlockEntity) {
                double distSq = pos.distSqr(checkPos);
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestAltar = checkPos.immutable();
                }
            }
        }

        if (closestAltar == null) return null;

        int dx = closestAltar.getX() - pos.getX();
        int dz = closestAltar.getZ() - pos.getZ();

        // Pick whichever horizontal axis has the larger delta
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (dz != 0) {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) return;

        if (level.getBlockEntity(pos) instanceof EnderEffigyBlockEntity effigy) {
            Vector3f color = effigy.getSinType().getParticleColor();

            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6;
            double y = pos.getY() + 0.6 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6;

            level.addParticle(new DustParticleOptions(color, 1.0f), x, y, z, 0.0, 0.02, 0.0);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return RegisterContent.ENDER_EFFIGY_BE.get().create(pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("block.thevoidcult.ender_effigy.desc1")
                .withStyle(ChatFormatting.GRAY));

        tooltipComponents.add(Component.translatable("block.thevoidcult.ender_effigy.desc2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

}
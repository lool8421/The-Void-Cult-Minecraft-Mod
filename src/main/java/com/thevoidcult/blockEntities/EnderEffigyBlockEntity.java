package com.thevoidcult.blockEntities;

import com.thevoidcult.registers.RegisterContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Properties;

public class EnderEffigyBlockEntity extends BlockEntity {
    public EnderEffigyBlockEntity(BlockPos pos, BlockState state) {
        super(RegisterContent.ENDER_EFFIGY_BE.get(), pos, state);
    }
}

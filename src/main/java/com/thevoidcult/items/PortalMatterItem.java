package com.thevoidcult.items;

import com.thevoidcult.blockEntities.VoidAltarBlockEntity;
import com.thevoidcult.registers.RegisterContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PortalMatterItem extends Item {
    public PortalMatterItem(Properties properties){
        super(properties);
    }


    public static void registerDispenserBehavior() {
        DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

        DispenserBlock.registerBehavior(RegisterContent.PORTAL_MATTER.get(), (blockSource, itemStack) -> {
            ServerLevel level = blockSource.level();

            Direction direction = blockSource.state().getValue(DispenserBlock.FACING);

            BlockPos targetPos = blockSource.pos().relative(direction);
            BlockEntity blockEntity = level.getBlockEntity(targetPos);

            if (blockEntity instanceof VoidAltarBlockEntity altar) {
                SinsList randomSin = SinsList.WORKING_SINS.get(level.random.nextInt(SinsList.WORKING_SINS.size()));

                if (altar.performWork(randomSin)) {
                    itemStack.shrink(1);
                    level.levelEvent(1000, blockSource.pos(), 0);
                    return itemStack;
                }
                level.levelEvent(1001, blockSource.pos(), 0);
                return itemStack;
            }

            return defaultBehavior.dispense(blockSource, itemStack);
        });
    }
}

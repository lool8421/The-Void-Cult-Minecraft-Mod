package com.thevoidcult.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MobPearlItem extends Item {

    public MobPearlItem(Properties properties){
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context){
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        blockpos = blockpos.relative(direction);

        if(!level.isClientSide()) {
            Mob mob = (Mob) EntityType.ZOMBIE.create(level);
            if (mob != null) {
                mob.moveTo(blockpos.getX()+0.5f, blockpos.getY(), blockpos.getZ()+0.5f, context.getRotation(), 0.0F);

                level.addFreshEntity(mob);
            }
        }
        return InteractionResult.CONSUME;
    }

}

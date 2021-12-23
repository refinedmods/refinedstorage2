package com.refinedmods.refinedstorage2.platform.fabric.item;

import com.refinedmods.refinedstorage2.platform.fabric.util.WrenchUtil;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (WrenchUtil.isWrenchable(state)) {
            return state.use(
                    context.getLevel(),
                    context.getPlayer(),
                    context.getHand(),
                    new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside())
            );
        }
        return super.useOn(context);
    }
}

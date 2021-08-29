package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.platform.fabric.init.BlockColorMap;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class GridBlock extends NetworkNodeContainerBlock {
    public GridBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Override
    protected boolean hasActive() {
        return true;
    }

    protected abstract BlockColorMap<?> getBlockColorMap();

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result = getBlockColorMap().updateColor(state, player.getStackInHand(hand), world, pos, player);
        if (result != ActionResult.PASS) {
            return result;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }
}

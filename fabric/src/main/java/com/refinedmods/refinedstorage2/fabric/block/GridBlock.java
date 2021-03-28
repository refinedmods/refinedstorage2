package com.refinedmods.refinedstorage2.fabric.block;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GridBlock extends NetworkNodeBlock {
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

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new GridBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result = RefinedStorage2Mod.BLOCKS.getGrid().updateColor(state, player.getStackInHand(hand), world, pos, player);
        if (result != ActionResult.PASS) {
            return result;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }
}

package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.block.BaseBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class BlockColorMap<T extends Block> extends ColorMap<T> {
    public ActionResult updateColor(BlockState state, ItemStack heldItem, World world, BlockPos pos, PlayerEntity player) {
        DyeColor color = heldItem.getItem() instanceof DyeItem ? ((DyeItem) heldItem.getItem()).getColor() : null;
        if (color == null || state.getBlock().equals(map.get(color))) {
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            world.setBlockState(pos, getNewState(map.get(color), state));
            if (((ServerPlayerEntity) player).interactionManager.getGameMode() != GameMode.CREATIVE) {
                heldItem.decrement(1);
            }
        }

        return ActionResult.SUCCESS;
    }

    public Block[] toArray() {
        return map.values().toArray(new Block[0]);
    }

    private BlockState getNewState(Block newBlock, BlockState oldState) {
        BlockState newState = newBlock.getDefaultState();
        if (newState.contains(BaseBlock.DIRECTION)) {
            newState = newState.with(BaseBlock.DIRECTION, oldState.get(BaseBlock.DIRECTION));
        }
        return newState;
    }
}

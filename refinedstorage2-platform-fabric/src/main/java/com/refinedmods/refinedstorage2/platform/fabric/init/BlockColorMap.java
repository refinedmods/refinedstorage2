package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.platform.fabric.block.BaseBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockColorMap<T extends Block> extends ColorMap<T> {
    public InteractionResult updateColor(BlockState state, ItemStack heldItem, Level world, BlockPos pos, Player player) {
        DyeColor color = heldItem.getItem() instanceof DyeItem dye ? dye.getDyeColor() : null;
        if (color == null || state.getBlock().equals(map.get(color))) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide()) {
            world.setBlockAndUpdate(pos, getNewState(map.get(color), state));
            if (((ServerPlayer) player).gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
                heldItem.shrink(1);
            }
        }

        return InteractionResult.SUCCESS;
    }

    public Block[] toArray() {
        return map.values().toArray(new Block[0]);
    }

    private BlockState getNewState(Block newBlock, BlockState oldState) {
        BlockState newState = newBlock.defaultBlockState();
        if (newState.hasProperty(BaseBlock.DIRECTION)) {
            newState = newState.setValue(BaseBlock.DIRECTION, oldState.getValue(BaseBlock.DIRECTION));
        }
        return newState;
    }
}

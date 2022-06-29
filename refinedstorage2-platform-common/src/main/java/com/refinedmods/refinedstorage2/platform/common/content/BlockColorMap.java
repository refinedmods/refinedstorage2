package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractNetworkNodeContainerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;

import java.util.Optional;

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
    public Optional<InteractionResult> updateColor(final BlockState state,
                                                   final ItemStack heldItem,
                                                   final Level level,
                                                   final BlockPos pos,
                                                   final Player player) {
        final DyeColor color = heldItem.getItem() instanceof DyeItem dye ? dye.getDyeColor() : null;
        if (color == null || state.getBlock().equals(get(color))) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            updateColorOnServer(state, heldItem, level, pos, (ServerPlayer) player, color);
        }
        return Optional.of(InteractionResult.sidedSuccess(level.isClientSide()));
    }

    private void updateColorOnServer(final BlockState state,
                                     final ItemStack heldItem,
                                     final Level level,
                                     final BlockPos pos,
                                     final ServerPlayer player,
                                     final DyeColor color) {
        final T newBlock = get(color);
        level.setBlockAndUpdate(pos, getNewState(newBlock, state));
        if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
            heldItem.shrink(1);
        }
    }

    public Block[] toArray() {
        return values().toArray(new Block[0]);
    }

    private BlockState getNewState(final Block newBlock, final BlockState oldState) {
        BlockState newState = newBlock.defaultBlockState();
        newState = transferBaseProperties(oldState, newState);
        newState = transferNetworkNodeProperties(oldState, newState);
        newState = transferControllerProperties(oldState, newState);
        return newState;
    }

    private BlockState transferBaseProperties(final BlockState oldState, final BlockState newState) {
        if (newState.hasProperty(AbstractBaseBlock.DIRECTION)) {
            return newState.setValue(AbstractBaseBlock.DIRECTION, oldState.getValue(AbstractBaseBlock.DIRECTION));
        }
        return newState;
    }

    private BlockState transferNetworkNodeProperties(final BlockState oldState, final BlockState newState) {
        if (newState.hasProperty(AbstractNetworkNodeContainerBlock.ACTIVE)) {
            return newState.setValue(
                    AbstractNetworkNodeContainerBlock.ACTIVE,
                    oldState.getValue(AbstractNetworkNodeContainerBlock.ACTIVE)
            );
        }
        return newState;
    }

    private BlockState transferControllerProperties(final BlockState oldState, final BlockState newState) {
        if (newState.hasProperty(ControllerBlock.ENERGY_TYPE)) {
            return newState.setValue(ControllerBlock.ENERGY_TYPE, oldState.getValue(ControllerBlock.ENERGY_TYPE));
        }
        return newState;
    }
}

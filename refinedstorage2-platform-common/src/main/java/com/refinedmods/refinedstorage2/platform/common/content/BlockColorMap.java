package com.refinedmods.refinedstorage2.platform.common.content;

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
import net.minecraft.world.level.block.state.properties.Property;

public class BlockColorMap<T extends Block> extends ColorMap<T> {
    public BlockColorMap() {
        this(DyeColor.LIGHT_BLUE);
    }

    public BlockColorMap(final DyeColor defaultColor) {
        super(defaultColor);
    }


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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private BlockState getNewState(final Block newBlock, final BlockState oldState) {
        BlockState newState = newBlock.defaultBlockState();
        for (final Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) {
                newState = newState.setValue((Property) property, oldState.getValue(property));
            }
        }
        return newState;
    }
}

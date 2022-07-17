package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class AbstractBaseBlock extends Block {
    protected AbstractBaseBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getDefaultState());
    }

    protected BlockState getDefaultState() {
        return getStateDefinition().any();
    }

    private static boolean rotate(final BlockState state, final Level level, final BlockPos pos) {
        final BlockState rotated = state.rotate(Rotation.CLOCKWISE_90);
        level.setBlockAndUpdate(pos, rotated);
        return !state.equals(rotated);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(final BlockState state,
                                 final Level level,
                                 final BlockPos pos,
                                 final Player player,
                                 final InteractionHand hand,
                                 final BlockHitResult hit) {
        return tryOpenScreen(state, level, pos, player)
            .orElseGet(() -> super.use(state, level, pos, player, hand, hit));
    }

    private Optional<InteractionResult> tryOpenScreen(final BlockState state,
                                                      final Level level,
                                                      final BlockPos pos,
                                                      final Player player) {
        final MenuProvider menuProvider = state.getMenuProvider(level, pos);
        if (menuProvider != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                Platform.INSTANCE.getMenuOpener().openMenu(serverPlayer, menuProvider);
            }
            return Optional.of(InteractionResult.SUCCESS);
        }
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider factory ? factory : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(final BlockState state,
                         final Level level,
                         final BlockPos pos,
                         final BlockState newState,
                         final boolean moved) {
        if (state.getBlock() != newState.getBlock()
            && !state.getBlock().getClass().equals(newState.getBlock().getClass())) {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockEntityWithDrops drops) {
                Containers.dropContents(level, pos, drops.getDrops());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    public static Optional<InteractionResult> tryUseWrench(final BlockState state,
                                                           final Level level,
                                                           final BlockHitResult hitResult,
                                                           final Player player,
                                                           final InteractionHand hand) {
        if (player.isSpectator() || !level.mayInteract(player, hitResult.getBlockPos())) {
            return Optional.empty();
        }
        final ItemStack itemInHand = player.getItemInHand(hand);
        final boolean holdingWrench = isWrench(itemInHand);
        if (!holdingWrench) {
            return Optional.empty();
        }
        final boolean isWrenchingOwnBlock = state.getBlock() instanceof AbstractBaseBlock;
        if (!isWrenchingOwnBlock) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            final boolean success = dismantleOrRotate(state, level, hitResult, player);
            if (success) {
                level.playSound(
                    null,
                    hitResult.getBlockPos(),
                    Sounds.INSTANCE.getWrench(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
                );
            }
        }
        return Optional.of(InteractionResult.sidedSuccess(level.isClientSide()));
    }

    public static Optional<InteractionResult> tryUpdateColor(final BlockState state,
                                                             final Level level,
                                                             final BlockPos pos,
                                                             final Player player,
                                                             final InteractionHand hand) {
        if (state.getBlock() instanceof ColorableBlock<?> colorableBlock) {
            return tryUpdateColor(colorableBlock.getBlockColorMap(), state, level, pos, player, hand);
        }
        return Optional.empty();
    }

    private static Optional<InteractionResult> tryUpdateColor(final BlockColorMap<?> blockColorMap,
                                                              final BlockState state,
                                                              final Level level,
                                                              final BlockPos pos,
                                                              final Player player,
                                                              final InteractionHand hand) {
        if (!player.isCrouching()) {
            return Optional.empty();
        }
        return blockColorMap.updateColor(state, player.getItemInHand(hand), level, pos, player);
    }

    private static boolean dismantleOrRotate(final BlockState state,
                                             final Level level,
                                             final BlockHitResult hitResult,
                                             final Player player) {
        if (player.isCrouching()) {
            dismantle(state, level, hitResult);
            return true;
        } else {
            return rotate(state, level, hitResult.getBlockPos());
        }
    }

    private static boolean isWrench(final ItemStack item) {
        return item.is(Platform.INSTANCE.getWrenchTag());
    }

    private static void dismantle(final BlockState state, final Level level, final BlockHitResult hitResult) {
        final ItemStack stack = state.getBlock().getCloneItemStack(level, hitResult.getBlockPos(), state);
        final BlockEntity blockEntity = level.getBlockEntity(hitResult.getBlockPos());
        if (blockEntity != null) {
            blockEntity.saveToItem(stack);
            // Ensure that we don't drop items
            level.removeBlockEntity(hitResult.getBlockPos());
        }
        level.setBlockAndUpdate(hitResult.getBlockPos(), Blocks.AIR.defaultBlockState());
        level.addFreshEntity(new ItemEntity(
            level,
            hitResult.getLocation().x,
            hitResult.getLocation().y,
            hitResult.getLocation().z,
            stack
        ));
    }
}

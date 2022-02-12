package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public abstract class BaseBlock extends Block {
    public static final EnumProperty<BiDirection> DIRECTION = EnumProperty.create("direction", BiDirection.class);

    protected BaseBlock(Properties properties) {
        super(properties);

        if (hasBiDirection()) {
            registerDefaultState(getStateDefinition().any().setValue(DIRECTION, BiDirection.NORTH));
        }
    }

    protected boolean hasBiDirection() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        if (hasBiDirection()) {
            builder.add(DIRECTION);
        }
    }

    private BiDirection getDirection(Direction playerFacing, float playerPitch) {
        if (playerPitch > 65) {
            return BiDirection.forUp(playerFacing);
        } else if (playerPitch < -65) {
            return BiDirection.forDown(playerFacing.getOpposite());
        } else {
            return BiDirection.forHorizontal(playerFacing.getOpposite());
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = defaultBlockState();

        if (hasBiDirection()) {
            state = state.setValue(DIRECTION, getDirection(ctx.getHorizontalDirection(), ctx.getPlayer() != null ? ctx.getPlayer().getXRot() : 0));
        }

        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        if (!hasBiDirection()) {
            return state;
        }
        BiDirection currentDirection = state.getValue(DIRECTION);
        return state.setValue(DIRECTION, currentDirection.rotate());
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return tryOpenScreen(state, level, pos, player).orElseGet(() -> super.use(state, level, pos, player, hand, hit));
    }

    private Optional<InteractionResult> tryOpenScreen(BlockState state, Level level, BlockPos pos, Player player) {
        MenuProvider menuProvider = state.getMenuProvider(level, pos);
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
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider factory ? factory : null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock() && !state.getBlock().getClass().equals(newState.getBlock().getClass())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BlockEntityWithDrops drops) {
                Containers.dropContents(level, pos, drops.getDrops());
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, moved);
        }
    }

    public static InteractionResult useWrench(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.isSpectator() || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        boolean holdingWrench = isWrench(itemInHand);
        if (!holdingWrench) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            rotateAndPlaySoundIfNecessary(state, level, pos, itemInHand);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static boolean isWrench(ItemStack item) {
        Tag<Item> wrench = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation("c", "wrenches"));
        return item.is(wrench);
    }

    private static void rotateAndPlaySoundIfNecessary(BlockState state, Level level, BlockPos pos, ItemStack itemInHand) {
        level.setBlockAndUpdate(pos, state.rotate(Rotation.CLOCKWISE_90));
        if (itemInHand.getItem() instanceof WrenchItem) {
            level.playSound(null, pos, Sounds.INSTANCE.getWrench(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}

package com.refinedmods.refinedstorage.platform.common.support;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.network.NetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.platform.common.content.Sounds;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeMenuProvider;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractBaseBlock extends Block {
    private static final TagKey<Item> WRENCH_TAG = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath("c", "tools/wrench")
    );

    protected AbstractBaseBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getDefaultState());
    }

    protected BlockState getDefaultState() {
        return getStateDefinition().any();
    }

    @Override
    public InteractionResult useWithoutItem(final BlockState state,
                                            final Level level,
                                            final BlockPos pos,
                                            final Player player,
                                            final BlockHitResult hitResult) {
        return tryOpenScreen(state, level, pos, player, hitResult.getLocation())
            .orElseGet(() -> super.useWithoutItem(state, level, pos, player, hitResult));
    }

    @Nullable
    protected VoxelShape getScreenOpenableShape(final BlockState state) {
        return null;
    }

    private Optional<InteractionResult> tryOpenScreen(final BlockState state,
                                                      final Level level,
                                                      final BlockPos pos,
                                                      final Player player,
                                                      final Vec3 hit) {
        final VoxelShape screenOpenableShape = getScreenOpenableShape(state);
        if (screenOpenableShape != null) {
            final AABB aabb = screenOpenableShape.bounds().move(pos);
            final boolean inBoundsX = hit.x >= aabb.minX && hit.x <= aabb.maxX;
            final boolean inBoundsY = hit.y >= aabb.minY && hit.y <= aabb.maxY;
            final boolean inBoundsZ = hit.z >= aabb.minZ && hit.z <= aabb.maxZ;
            final boolean inBounds = inBoundsX && inBoundsY && inBoundsZ;
            if (!inBounds) {
                return Optional.empty();
            }
        }
        final MenuProvider menuProvider = state.getMenuProvider(level, pos);
        if (menuProvider != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                tryOpenScreen(serverPlayer, menuProvider);
            }
            return Optional.of(InteractionResult.SUCCESS);
        }
        return Optional.empty();
    }

    private void tryOpenScreen(final ServerPlayer player, final MenuProvider menuProvider) {
        if (menuProvider instanceof NetworkNodeMenuProvider networkNodeMenuProvider
            && !networkNodeMenuProvider.canOpen(player)) {
            PlatformApi.INSTANCE.sendNoPermissionToOpenMessage(player, getName());
            return;
        }
        Platform.INSTANCE.getMenuOpener().openMenu(player, menuProvider);
    }

    @Override
    @SuppressWarnings("deprecation")
    public MenuProvider getMenuProvider(final BlockState state, final Level level, final BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof MenuProvider provider ? provider : null;
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
            if (blockEntity instanceof BlockEntityWithDrops blockEntityDrops) {
                Containers.dropContents(level, pos, blockEntityDrops.getDrops());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    public void setPlacedBy(final Level level,
                            final BlockPos pos,
                            final BlockState state,
                            @Nullable final LivingEntity entity,
                            final ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (entity instanceof Player player
            && level.getBlockEntity(pos) instanceof PlayerAwareBlockEntity playerAware) {
            playerAware.setPlacedBy(player.getGameProfile().getId());
        }
    }

    public final Optional<InteractionResult> tryUseWrench(final BlockState state,
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
        if (player instanceof ServerPlayer serverPlayer) {
            final boolean success = dismantleOrRotate(state, level, hitResult, serverPlayer);
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

    private boolean dismantleOrRotate(final BlockState state,
                                      final Level level,
                                      final BlockHitResult hitResult,
                                      final ServerPlayer player) {
        if (player.isCrouching()) {
            return dismantle(state, level, hitResult, player);
        } else {
            return rotate(state, level, hitResult.getBlockPos(), player);
        }
    }

    private boolean rotate(final BlockState state,
                           final Level level,
                           final BlockPos pos,
                           final ServerPlayer player) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof NetworkNodeContainerBlockEntity networkNodeContainerBlockEntity
            && !networkNodeContainerBlockEntity.canBuild(player)) {
            PlatformApi.INSTANCE.sendNoPermissionMessage(
                player,
                createTranslation("misc", "no_permission.build.rotate", getName())
            );
            return false;
        }
        final BlockState rotated = getRotatedBlockState(state, level, pos);
        level.setBlockAndUpdate(pos, rotated);
        return !state.equals(rotated);
    }

    @SuppressWarnings("deprecation")
    protected BlockState getRotatedBlockState(final BlockState state, final Level level, final BlockPos pos) {
        return state.rotate(Rotation.CLOCKWISE_90);
    }

    private boolean isWrench(final ItemStack item) {
        return item.is(WRENCH_TAG);
    }

    private boolean dismantle(final BlockState state,
                              final Level level,
                              final BlockHitResult hitResult,
                              final ServerPlayer player) {
        final BlockEntity blockEntity = level.getBlockEntity(hitResult.getBlockPos());
        if (blockEntity instanceof NetworkNodeContainerBlockEntity networkNodeContainerBlockEntity
            && !networkNodeContainerBlockEntity.canBuild(player)) {
            PlatformApi.INSTANCE.sendNoPermissionMessage(
                player,
                createTranslation("misc", "no_permission.build.dismantle", getName())
            );
            return false;
        }
        final ItemStack stack = Platform.INSTANCE.getCloneItemStack(state, level, hitResult, player);
        if (blockEntity != null) {
            blockEntity.saveToItem(stack, level.registryAccess());
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
        return true;
    }

    public final Optional<InteractionResult> tryUpdateColor(final BlockState state,
                                                            final Level level,
                                                            final BlockPos pos,
                                                            final Player player,
                                                            final InteractionHand hand) {
        if (this instanceof ColorableBlock<?, ?> colorableBlock) {
            return tryUpdateColor(colorableBlock.getBlockColorMap(), state, level, pos, player, hand);
        }
        return Optional.empty();
    }

    private Optional<InteractionResult> tryUpdateColor(
        final BlockColorMap<?, ?> blockColorMap,
        final BlockState state,
        final Level level,
        final BlockPos pos,
        final Player player,
        final InteractionHand hand
    ) {
        if (!player.isCrouching()) {
            return Optional.empty();
        }
        return blockColorMap.updateColor(state, player.getItemInHand(hand), level, pos, player);
    }
}

package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.Sounds;
import com.refinedmods.refinedstorage.common.networking.AbstractCableBlockEntity;
import com.refinedmods.refinedstorage.common.networking.CableConnections;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeMenuProvider;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
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
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractBaseBlock extends Block {
    private static final TagKey<Item> WRENCH_TAG = TagKey.create(
        Registries.ITEM,
        Identifier.fromNamespaceAndPath("c", "tools/wrench")
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
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof MenuProvider provider) {
            if (player instanceof ServerPlayer serverPlayer) {
                tryOpenScreen(serverPlayer, provider);
            }
            return Optional.of(InteractionResult.SUCCESS);
        }
        return Optional.empty();
    }

    private void tryOpenScreen(final ServerPlayer player, final MenuProvider menuProvider) {
        if (menuProvider instanceof NetworkNodeMenuProvider networkNodeMenuProvider
            && !networkNodeMenuProvider.canOpen(player)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, getName());
            return;
        }
        Platform.INSTANCE.getMenuOpener().openMenu(player, menuProvider);
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
            playerAware.setPlacedBy(player.getGameProfile().id());
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
        return Optional.of(InteractionResult.SUCCESS);
    }

    private boolean dismantleOrRotate(final BlockState state,
                                      final Level level,
                                      final BlockHitResult hitResult,
                                      final ServerPlayer player) {
        if (player.isCrouching()) {
            return dismantle(state, level, hitResult, player);
        } else {
            return rotate(state, level, hitResult.getBlockPos(), hitResult.getDirection(), player);
        }
    }

    private boolean rotate(final BlockState state,
                           final Level level,
                           final BlockPos pos,
                           final Direction direction,
                           final ServerPlayer player) {
        final NetworkNodeContainerProvider provider = Platform.INSTANCE.getContainerProvider(level, pos, direction);
        if (provider != null && !provider.canBuild(player)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionMessage(
                player,
                createTranslation("misc", "no_permission.build.rotate", getName())
            );
            return false;
        }
        final BlockState rotated = getRotatedBlockState(state, level, pos);
        level.setBlockAndUpdate(pos, rotated);
        return !state.equals(rotated);
    }

    @SuppressWarnings("deprecation") // deprecated on NeoForge
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
        final NetworkNodeContainerProvider provider = Platform.INSTANCE.getContainerProvider(
            level,
            hitResult.getBlockPos(),
            hitResult.getDirection()
        );
        if (provider != null && !provider.canBuild(player)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionMessage(
                player,
                createTranslation("misc", "no_permission.build.dismantle", getName())
            );
            return false;
        }
        final BlockEntity blockEntity = level.getBlockEntity(hitResult.getBlockPos());
        final ItemStack stack = Platform.INSTANCE.getCloneItemStack(state, level, hitResult, player);
        if (blockEntity != null) {
            if (!(blockEntity instanceof AbstractCableBlockEntity)) {
                final CompoundTag tag = blockEntity.saveWithoutMetadata(level.registryAccess());
                CableConnections.stripTag(tag);
                stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(blockEntity.getType(), tag));
            }
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

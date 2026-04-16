package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

class BlockBreakDestructorStrategy implements DestructorStrategy {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction direction;
    private final ItemStack tool;

    BlockBreakDestructorStrategy(
        final ServerLevel level,
        final BlockPos pos,
        final Direction direction,
        final ItemStack tool
    ) {
        this.level = level;
        this.pos = pos;
        this.direction = direction;
        this.tool = tool;
    }

    @Override
    public boolean apply(final Filter filter,
                         final Actor actor,
                         final Supplier<@Nullable Network> networkProvider,
                         final Player player) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();
        if (isFastExit(blockState)
            || blockState.getDestroySpeed(level, pos) == -1.0
            || !isAllowed(player, filter, blockState, block)
            || !Platform.INSTANCE.canBreakBlock(level, pos, blockState, player)) {
            return false;
        }
        final List<ItemStack> drops = Block.getDrops(
            blockState,
            level,
            pos,
            level.getBlockEntity(pos),
            player,
            tool
        );
        if (!insertDrops(actor, drops, getRootStorage(networkProvider), Action.SIMULATE)) {
            return false;
        }
        block.playerWillDestroy(level, pos, blockState, player);
        level.removeBlock(pos, false);
        insertDrops(actor, drops, getRootStorage(networkProvider), Action.EXECUTE);
        return true;
    }

    @Nullable
    private static RootStorage getRootStorage(final Supplier<@Nullable Network> networkProvider) {
        final Network network = networkProvider.get();
        if (network == null) {
            return null;
        }
        return network.getComponent(StorageNetworkComponent.class);
    }

    private static boolean isFastExit(final BlockState blockState) {
        return blockState.isAir() || blockState.getBlock() instanceof LiquidBlock;
    }

    private boolean isAllowed(
        final Player actingPlayer,
        final Filter filter,
        final BlockState state,
        final Block block
    ) {
        final ItemStack blockAsStack = Platform.INSTANCE.getBlockAsItemStack(
            block,
            state,
            direction,
            level,
            pos,
            actingPlayer
        );
        if (blockAsStack.isEmpty()) {
            return false;
        }
        final ItemResource blockAsResource = ItemResource.ofItemStack(blockAsStack);
        return filter.isAllowed(blockAsResource);
    }

    private boolean insertDrops(
        final Actor actor,
        final List<ItemStack> drops,
        @Nullable final RootStorage storage,
        final Action action
    ) {
        if (storage == null) {
            return false;
        }
        for (final ItemStack drop : drops) {
            final ItemResource resource = ItemResource.ofItemStack(drop);
            final boolean didNotInsertCompletely =
                storage.insert(resource, drop.getCount(), action, actor) != drop.getCount();
            if (didNotInsertCompletely) {
                final boolean didWeDisconnectStorageInTheProcessOfRemovingABlock = action == Action.EXECUTE;
                if (didWeDisconnectStorageInTheProcessOfRemovingABlock) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}

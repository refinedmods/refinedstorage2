package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.container.NetworkNodeContainerPriorities;
import com.refinedmods.refinedstorage2.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGridBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<GridNetworkNode>
    implements Grid, ExtendedMenuProvider {
    protected AbstractGridBlockEntity(final BlockEntityType<? extends AbstractGridBlockEntity> type,
                                      final BlockPos pos,
                                      final BlockState state,
                                      final long energyUsage) {
        super(type, pos, state, new GridNetworkNode(energyUsage));
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        PlatformApi.INSTANCE.writeGridScreenOpeningData(this, buf);
    }

    @Override
    public <T> List<TrackedResourceAmount<T>> getResources(final StorageChannelType<T> type,
                                                           final Class<? extends Actor> actorType) {
        return requireNonNull(getNode().getNetwork())
            .getComponent(StorageNetworkComponent.class)
            .getResources(type, actorType);
    }

    @Override
    public <T> GridOperations<T> createOperations(final PlatformStorageChannelType<T> storageChannelType,
                                                  final Actor actor) {
        final StorageChannel<T> storageChannel = requireNonNull(getNode().getNetwork())
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(storageChannelType);
        return storageChannelType.createGridOperations(storageChannel, actor);
    }

    @Override
    public boolean isGridActive() {
        return getNode().isActive();
    }

    @Override
    public Storage<ItemResource> getItemStorage() {
        return requireNonNull(getNode().getNetwork())
            .getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.ITEM);
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        getNode().addWatcher(watcher, actorType);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        getNode().removeWatcher(watcher);
    }

    @Override
    public final int getPriority() {
        return NetworkNodeContainerPriorities.GRID;
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}

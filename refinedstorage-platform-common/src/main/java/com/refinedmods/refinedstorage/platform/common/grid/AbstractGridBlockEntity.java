package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.container.NetworkNodeContainerPriorities;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGridBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<GridNetworkNode>
    implements Grid, NetworkNodeExtendedMenuProvider<GridData> {
    protected AbstractGridBlockEntity(final BlockEntityType<? extends AbstractGridBlockEntity> type,
                                      final BlockPos pos,
                                      final BlockState state,
                                      final long energyUsage) {
        super(type, pos, state, new GridNetworkNode(energyUsage));
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final GridNetworkNode node) {
        return PlatformApi.INSTANCE.createInWorldNetworkNodeContainer(
            this,
            node,
            MAIN_CONTAINER_NAME,
            NetworkNodeContainerPriorities.GRID,
            this,
            null
        );
    }

    @Override
    public GridData getMenuData() {
        return GridData.of(this);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, GridData> getMenuCodec() {
        return GridData.STREAM_CODEC;
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return requireNonNull(mainNode.getNetwork())
            .getComponent(StorageNetworkComponent.class)
            .getResources(actorType);
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        final Network network = requireNonNull(mainNode.getNetwork());
        final StorageChannel storageChannel = network.getComponent(StorageNetworkComponent.class);
        final PlatformSecurityNetworkComponent security = network.getComponent(PlatformSecurityNetworkComponent.class);
        final GridOperations operations = resourceType.createGridOperations(storageChannel, new PlayerActor(player));
        return new SecuredGridOperations(player, security, operations);
    }

    @Override
    public boolean isGridActive() {
        return mainNode.isActive();
    }

    @Override
    public Storage getItemStorage() {
        return requireNonNull(mainNode.getNetwork()).getComponent(StorageNetworkComponent.class);
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        mainNode.addWatcher(watcher, actorType);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        mainNode.removeWatcher(watcher);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}

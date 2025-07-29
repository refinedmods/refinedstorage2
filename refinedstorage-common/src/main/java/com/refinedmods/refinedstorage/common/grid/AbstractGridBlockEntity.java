package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.container.NetworkNodeContainerPriorities;
import com.refinedmods.refinedstorage.api.network.impl.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridOperations;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.ColoredConnectionStrategy;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGridBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<GridNetworkNode>
    implements Grid {
    protected AbstractGridBlockEntity(final BlockEntityType<? extends AbstractGridBlockEntity> type,
                                      final BlockPos pos,
                                      final BlockState state,
                                      final long energyUsage) {
        super(type, pos, state, new GridNetworkNode(energyUsage));
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final GridNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .priority(NetworkNodeContainerPriorities.GRID)
            .connectionStrategy(new ColoredConnectionStrategy(this::getBlockState, getBlockPos()))
            .build();
    }

    @Override
    public List<TrackedResourceAmount> getResources(final Class<? extends Actor> actorType) {
        return requireNonNull(mainNetworkNode.getNetwork())
            .getComponent(StorageNetworkComponent.class)
            .getResources(actorType);
    }

    @Override
    public Set<PlatformResourceKey> getAutocraftableResources() {
        return requireNonNull(mainNetworkNode.getNetwork())
            .getComponent(AutocraftingNetworkComponent.class)
            .getOutputs()
            .stream()
            .filter(PlatformResourceKey.class::isInstance)
            .map(PlatformResourceKey.class::cast)
            .collect(Collectors.toSet());
    }

    @Override
    public GridOperations createOperations(final ResourceType resourceType, final ServerPlayer player) {
        final Network network = requireNonNull(mainNetworkNode.getNetwork());
        final RootStorage rootStorage = network.getComponent(StorageNetworkComponent.class);
        final PlatformSecurityNetworkComponent security = network.getComponent(PlatformSecurityNetworkComponent.class);
        final GridOperations operations = resourceType.createGridOperations(rootStorage, new PlayerActor(player));
        return new SecuredGridOperations(player, security, operations);
    }

    @Override
    public boolean isGridActive() {
        return mainNetworkNode.isActive();
    }

    @Override
    public Storage getItemStorage() {
        return requireNonNull(mainNetworkNode.getNetwork()).getComponent(StorageNetworkComponent.class);
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        mainNetworkNode.addWatcher(watcher, actorType);
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        mainNetworkNode.removeWatcher(watcher);
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount,
                                                           final CancellationToken cancellationToken) {
        return Optional.ofNullable(mainNetworkNode.getNetwork())
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .map(component -> component.getPreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Optional<TreePreview>> getTreePreview(final ResourceKey resource, final long amount,
                                                                   final CancellationToken cancellationToken) {
        return Optional.ofNullable(mainNetworkNode.getNetwork())
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .map(component -> component.getTreePreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        return Optional.ofNullable(mainNetworkNode.getNetwork())
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .map(component -> component.getMaxAmount(resource, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify,
                                                         final CancellationToken cancellationToken) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return network.getComponent(AutocraftingNetworkComponent.class).startTask(resource, amount, actor, notify,
            cancellationToken);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}

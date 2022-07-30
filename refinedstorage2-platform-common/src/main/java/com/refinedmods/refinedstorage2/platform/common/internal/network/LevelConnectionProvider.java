package com.refinedmods.refinedstorage2.platform.common.internal.network;

import com.refinedmods.refinedstorage2.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.network.Connections;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.api.blockentity.AbstractNetworkNodeContainerBlockEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

public class LevelConnectionProvider implements ConnectionProvider {
    private final Level level;

    public LevelConnectionProvider(final Level level) {
        this.level = level;
    }

    @Override
    public Connections findConnections(final NetworkNodeContainer pivot,
                                       final Set<NetworkNodeContainer> existingConnections) {
        final ScanState scanState = new ScanState(convertToScanEntries(existingConnections));

        // TODO: Convert to queue
        // TODO: Does this need to be NetworkNodeContainerBlockEntity?
        depthScan(scanState, ((AbstractNetworkNodeContainerBlockEntity<?>) pivot).getBlockPos());

        return new Connections(
            convertToContainers(scanState.foundEntries),
            convertToContainers(scanState.newEntries),
            convertToContainers(scanState.removedEntries)
        );
    }

    private Set<NetworkNodeContainer> convertToContainers(final Set<ScanEntry> foundEntries) {
        return foundEntries.stream().map(ScanEntry::getContainer).collect(Collectors.toSet());
    }

    private Set<ScanEntry> convertToScanEntries(final Set<NetworkNodeContainer> existingConnections) {
        return existingConnections
            .stream()
            .map(container -> new ScanEntry(
                container,
                Objects.requireNonNull(((BlockEntity) container).getLevel()),
                ((BlockEntity) container).getBlockPos()
            ))
            .collect(Collectors.toSet());
    }

    @Override
    public List<NetworkNodeContainer> sort(final Set<NetworkNodeContainer> containers) {
        return containers
            .stream()
            .sorted(Comparator.comparing(container -> ((BlockEntity) container).getBlockPos()))
            .toList();
    }

    private void depthScan(final ScanState scanState, final BlockPos position) {
        if (getBlockEntity(position) instanceof AbstractNetworkNodeContainerBlockEntity<?> containerBlockEntity) {
            addEntry(scanState, new ScanEntry(containerBlockEntity, level, position));
        }
    }

    private void addEntry(final ScanState scanState, final ScanEntry entry) {
        if (scanState.foundEntries.contains(entry)) {
            return;
        }

        scanState.foundEntries.add(entry);

        if (!scanState.currentEntries.contains(entry)) {
            scanState.newEntries.add(entry);
        }

        scanState.removedEntries.remove(entry);

        final Set<NetworkNodeContainer> connections =
            findConnectionsAt((AbstractNetworkNodeContainerBlockEntity<?>) entry.getContainer());
        for (final NetworkNodeContainer connection : connections) {
            depthScan(scanState, ((AbstractNetworkNodeContainerBlockEntity<?>) connection).getBlockPos());
        }
    }

    private Set<NetworkNodeContainer> findConnectionsAt(final AbstractNetworkNodeContainerBlockEntity<?> container) {
        final Set<NetworkNodeContainer> containers = new HashSet<>();
        for (final Direction direction : Direction.values()) {
            if (!container.canPerformOutgoingConnection(direction)) {
                continue;
            }
            final BlockPos offsetPos = container.getBlockPos().relative(direction);
            if (getBlockEntity(offsetPos) instanceof AbstractNetworkNodeContainerBlockEntity<?> neighborContainer
                && neighborContainer.canAcceptIncomingConnection(direction)) {
                containers.add(neighborContainer);
            }
        }
        return containers;
    }

    @Nullable
    private BlockEntity getBlockEntity(final BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        // Avoid using EntityCreationType.IMMEDIATE.
        // By default, the block is removed first and then the block entity (see BaseBlock#onRemove).
        // But, when using mods like Carrier or Carpet that allow for moving block entities,
        // they remove the block entity first and then the block.
        // When removing a block with Carrier for example,
        // this causes a problematic situation that the block entity IS gone,
        // but that the #getBlockEntity() call here with type IMMEDIATE would recreate the block entity because
        // the block is still there.
        // If the block entity is returned here again even if it is removed, the preconditions in NetworkBuilder will
        // fail as the "removed" block entity/connection would still be present.
        return level.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK);
    }

    private static class ScanState {
        private final Set<ScanEntry> currentEntries;
        private final Set<ScanEntry> foundEntries = new HashSet<>();
        private final Set<ScanEntry> newEntries = new HashSet<>();
        private final Set<ScanEntry> removedEntries;

        ScanState(final Set<ScanEntry> currentEntries) {
            this.currentEntries = currentEntries;
            this.removedEntries = new HashSet<>(currentEntries);
        }
    }

    private static class ScanEntry {
        private final NetworkNodeContainer container;
        private final ResourceKey<Level> dimension;
        private final BlockPos position;

        ScanEntry(final NetworkNodeContainer container, final Level dimension, final BlockPos position) {
            this.container = container;
            this.dimension = dimension.dimension();
            this.position = position;
        }

        public NetworkNodeContainer getContainer() {
            return container;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final ScanEntry that = (ScanEntry) o;
            return Objects.equals(dimension, that.dimension) && Objects.equals(position, that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, position);
        }
    }
}

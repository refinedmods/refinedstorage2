package com.refinedmods.refinedstorage2.platform.common.internal.network;

import com.refinedmods.refinedstorage2.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.network.Connections;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelConnectionProvider implements ConnectionProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(LevelConnectionProvider.class);

    private final Level originLevel;

    public LevelConnectionProvider(final Level originLevel) {
        this.originLevel = originLevel;
    }

    @Override
    public Connections findConnections(final NetworkNodeContainer pivot,
                                       final Set<NetworkNodeContainer> existingConnections) {
        final Set<PlatformNetworkNodeContainer> existingPlatformConnections = existingConnections.stream()
            .filter(PlatformNetworkNodeContainer.class::isInstance)
            .map(PlatformNetworkNodeContainer.class::cast)
            .collect(Collectors.toSet());
        LOGGER.info("Finding connections for pivot {} with {} existing connections", pivot, existingConnections.size());
        final ScanState scanState = new ScanState(existingPlatformConnections);
        addInitialRequest(pivot, scanState);
        ScanRequest currentRequest;
        int requests = 0;
        while ((currentRequest = scanState.toCheck.poll()) != null) {
            visit(scanState, currentRequest);
            requests++;
        }
        LOGGER.info(
            "Processed {} requests for pivot {} with {} found entries ({} removed and {} new)",
            requests,
            pivot,
            scanState.foundEntries.size(),
            scanState.removedEntries.size(),
            scanState.newEntries.size()
        );
        return scanState.toConnections();
    }

    private void addInitialRequest(final NetworkNodeContainer pivot, final ScanState scanState) {
        if (!(pivot instanceof PlatformNetworkNodeContainer platformPivot)) {
            return;
        }
        final Level pivotLevel = platformPivot.getLevel();
        if (pivotLevel == null) {
            LOGGER.warn("Pivot level was null for {}", pivot);
            return;
        }
        scanState.toCheck.add(new ScanRequest(
            pivotLevel.dimension(),
            platformPivot.getBlockPos()
        ));
    }

    private void visit(final ScanState state, final ScanRequest request) {
        if (getBlockEntity(request.dimension, request.position) instanceof PlatformNetworkNodeContainer container) {
            visit(state, new ScanEntry(container, originLevel, request.position));
        }
    }

    private void visit(final ScanState state, final ScanEntry entry) {
        if (state.foundEntries.contains(entry)) {
            return;
        }
        state.foundEntries.add(entry);
        if (!state.currentEntries.contains(entry)) {
            state.newEntries.add(entry);
        }
        state.removedEntries.remove(entry);
        final Set<ScanRequest> connections = findConnectionsAt(entry.getContainer());
        state.toCheck.addAll(connections);
    }

    private Set<ScanRequest> findConnectionsAt(final PlatformNetworkNodeContainer from) {
        final Level level = from.getLevel();
        if (level == null) {
            return Collections.emptySet();
        }
        final Set<ScanRequest> requests = new HashSet<>();
        for (final Direction direction : Direction.values()) {
            if (!from.canPerformOutgoingConnection(direction)) {
                continue;
            }
            final BlockPos offsetPos = from.getBlockPos().relative(direction);
            if (getBlockEntity(level, offsetPos) instanceof PlatformNetworkNodeContainer neighborContainer
                && neighborContainer.canAcceptIncomingConnection(direction, from.getBlockState())) {
                requests.add(new ScanRequest(level.dimension(), offsetPos));
            }
        }
        return requests;
    }

    @Override
    public List<NetworkNodeContainer> sortDeterministically(final Set<NetworkNodeContainer> containers) {
        return containers
            .stream()
            .sorted(Comparator.comparing(container -> ((BlockEntity) container).getBlockPos()))
            .toList();
    }

    @Nullable
    private BlockEntity getBlockEntity(final ResourceKey<Level> dimension, final BlockPos pos) {
        final MinecraftServer server = originLevel.getServer();
        if (server == null) {
            return null;
        }
        final Level level = server.getLevel(dimension);
        if (level == null) {
            return null;
        }
        return getBlockEntity(level, pos);
    }

    @Nullable
    private BlockEntity getBlockEntity(final Level level, final BlockPos pos) {
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
        private final Queue<ScanRequest> toCheck = new ArrayDeque<>();

        ScanState(final Set<PlatformNetworkNodeContainer> existingConnections) {
            this.currentEntries = toScanEntries(existingConnections);
            this.removedEntries = new HashSet<>(currentEntries);
        }

        public Connections toConnections() {
            return new Connections(
                toContainers(foundEntries),
                toContainers(newEntries),
                toContainers(removedEntries)
            );
        }

        private Set<NetworkNodeContainer> toContainers(final Set<ScanEntry> entries) {
            return entries.stream().map(ScanEntry::getContainer).collect(Collectors.toSet());
        }

        private static Set<ScanEntry> toScanEntries(final Set<PlatformNetworkNodeContainer> existingConnections) {
            return existingConnections.stream().map(container -> new ScanEntry(
                container,
                Objects.requireNonNull(container.getLevel()),
                Objects.requireNonNull(container.getBlockPos())
            )).collect(Collectors.toSet());
        }
    }

    private static class ScanEntry {
        private final PlatformNetworkNodeContainer container;
        private final ResourceKey<Level> dimension;
        private final BlockPos position;

        ScanEntry(final PlatformNetworkNodeContainer container, final Level dimension, final BlockPos position) {
            this.container = container;
            this.dimension = dimension.dimension();
            this.position = position;
        }

        public PlatformNetworkNodeContainer getContainer() {
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

    private record ScanRequest(ResourceKey<Level> dimension, BlockPos position) {
    }
}

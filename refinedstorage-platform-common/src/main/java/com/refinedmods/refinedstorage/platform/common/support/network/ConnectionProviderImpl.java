package com.refinedmods.refinedstorage.platform.common.support.network;

import com.refinedmods.refinedstorage.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage.api.network.Connections;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.api.support.network.NetworkNodeContainerBlockEntity;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionProviderImpl implements ConnectionProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionProviderImpl.class);

    private final Level originLevel;

    public ConnectionProviderImpl(final Level originLevel) {
        this.originLevel = originLevel;
    }

    @Override
    public Connections findConnections(final NetworkNodeContainer pivot,
                                       final Set<NetworkNodeContainer> existingConnections) {
        final Set<InWorldNetworkNodeContainer> existingInWorldConnections = existingConnections.stream()
            .filter(InWorldNetworkNodeContainer.class::isInstance)
            .map(InWorldNetworkNodeContainer.class::cast)
            .collect(Collectors.toSet());
        LOGGER.debug(
            "Finding connections for pivot {} with {} existing connections",
            pivot,
            existingConnections.size()
        );
        final ScanState scanState = new ScanState(existingInWorldConnections);
        addStartContainer(pivot, scanState);
        InWorldNetworkNodeContainer currentContainer;
        int requests = 0;
        while ((currentContainer = scanState.toCheck.poll()) != null) {
            visit(scanState, new ScanEntry(currentContainer));
            requests++;
        }
        LOGGER.debug(
            "Processed {} requests for pivot {} with {} found entries ({} removed and {} new)",
            requests,
            pivot,
            scanState.foundEntries.size(),
            scanState.removedEntries.size(),
            scanState.newEntries.size()
        );
        return scanState.toConnections();
    }

    private void addStartContainer(final NetworkNodeContainer pivot, final ScanState scanState) {
        if (!(pivot instanceof InWorldNetworkNodeContainer platformPivot)) {
            return;
        }
        scanState.toCheck.add(platformPivot);
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
        final List<InWorldNetworkNodeContainer> connections = findConnectionsAt(entry.getContainer());
        state.toCheck.addAll(connections);
    }

    private List<InWorldNetworkNodeContainer> findConnectionsAt(final InWorldNetworkNodeContainer from) {
        final GlobalPos pos = from.getPosition();
        final ConnectionSinkImpl sink = new ConnectionSinkImpl(pos);
        from.addOutgoingConnections(sink);
        final List<InWorldNetworkNodeContainer> connections = new ArrayList<>();
        for (final ConnectionSinkImpl.Connection connection : sink.getConnections()) {
            connections.addAll(getConnections(from, connection));
        }
        return connections;
    }

    private Set<InWorldNetworkNodeContainer> getConnections(final InWorldNetworkNodeContainer from,
                                                            final ConnectionSinkImpl.Connection connection) {
        final BlockEntity connectionBlockEntity = getBlockEntitySafely(connection.pos());
        if (!(connectionBlockEntity instanceof NetworkNodeContainerBlockEntity networkNodeContainerBlockEntity)) {
            return Collections.emptySet();
        }
        if (connection.incomingDirection() == null) {
            return networkNodeContainerBlockEntity.getContainers();
        }
        return networkNodeContainerBlockEntity.getContainers()
            .stream()
            .filter(container -> container.canAcceptIncomingConnection(
                connection.incomingDirection(),
                from.getBlockState()
            ))
            .collect(Collectors.toSet());
    }

    @Override
    public List<NetworkNodeContainer> sortDeterministically(final Set<NetworkNodeContainer> containers) {
        return containers
            .stream()
            .sorted(Comparator.comparing(container -> ((InWorldNetworkNodeContainer) container).getLocalPosition()))
            .toList();
    }

    @Nullable
    private BlockEntity getBlockEntitySafely(final GlobalPos pos) {
        final MinecraftServer server = originLevel.getServer();
        if (server == null) {
            return null;
        }
        final Level level = server.getLevel(pos.dimension());
        if (level == null) {
            return null;
        }
        return getBlockEntitySafely(level, pos.pos());
    }

    @Nullable
    private BlockEntity getBlockEntitySafely(final Level level, final BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        // Avoid using EntityCreationType.IMMEDIATE.
        // By default, the block is removed first and then the block entity (see BaseBlock#onRemove).
        // But, when using mods like "Carrier", "Carpet" or "Carry On" that allow for moving block entities,
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
        private final Queue<InWorldNetworkNodeContainer> toCheck = new ArrayDeque<>();

        ScanState(final Set<InWorldNetworkNodeContainer> existingConnections) {
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

        private static Set<ScanEntry> toScanEntries(final Set<InWorldNetworkNodeContainer> existingConnections) {
            return existingConnections.stream().map(ScanEntry::new).collect(Collectors.toSet());
        }
    }

    private static class ScanEntry {
        private final InWorldNetworkNodeContainer container;
        private final GlobalPos pos;
        private final String name;

        ScanEntry(final InWorldNetworkNodeContainer container) {
            this.container = container;
            this.pos = container.getPosition();
            this.name = container.getName();
        }

        private InWorldNetworkNodeContainer getContainer() {
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
            final ScanEntry scanEntry = (ScanEntry) o;
            return Objects.equals(pos, scanEntry.pos) && Objects.equals(name, scanEntry.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, name);
        }
    }
}

package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage.api.network.Connections;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;

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

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
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
        final NetworkNodeContainerProvider provider = getContainerProviderSafely(
            connection.pos(),
            connection.incomingDirection()
        );
        if (provider == null) {
            return Collections.emptySet();
        }
        if (connection.incomingDirection() == null) {
            return provider.getContainers()
                .stream()
                .filter(container -> isBlockAllowed(container.getBlockState(), connection))
                .collect(Collectors.toSet());
        }
        return provider.getContainers()
            .stream()
            .filter(container -> isBlockAllowed(container.getBlockState(), connection))
            .filter(container -> container.canAcceptIncomingConnection(
                connection.incomingDirection(),
                from.getBlockState()
            ))
            .collect(Collectors.toSet());
    }

    private boolean isBlockAllowed(final BlockState state, final ConnectionSinkImpl.Connection connection) {
        if (connection.allowedBlockType() == null) {
            return true;
        }
        return state.getBlock().getClass().isAssignableFrom(connection.allowedBlockType());
    }

    @Override
    public List<NetworkNodeContainer> sortDeterministically(final Set<NetworkNodeContainer> containers) {
        return containers
            .stream()
            .sorted(Comparator.comparing(container -> ((InWorldNetworkNodeContainer) container).getLocalPosition()))
            .toList();
    }

    @Nullable
    private NetworkNodeContainerProvider getContainerProviderSafely(final GlobalPos pos,
                                                                    @Nullable final Direction direction) {
        final MinecraftServer server = originLevel.getServer();
        if (server == null) {
            return null;
        }
        final Level level = server.getLevel(pos.dimension());
        if (level == null) {
            return null;
        }
        return Platform.INSTANCE.getContainerProviderSafely(level, pos.pos(), direction);
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
        public boolean equals(@Nullable final Object o) {
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

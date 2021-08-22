package com.refinedmods.refinedstorage2.platform.fabric.internal.network.node;

import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.network.node.container.Connections;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.block.entity.NetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class FabricConnectionProvider implements ConnectionProvider {
    private final World world;

    public FabricConnectionProvider(World world) {
        this.world = world;
    }

    @Override
    public Connections findConnections(NetworkNodeContainer<?> pivot, Set<NetworkNodeContainer<?>> existingConnections) {
        ScanState scanState = new ScanState(convertToScanEntries(existingConnections));

        // TODO: Convert to queue
        depthScan(scanState, ((PlatformNetworkNodeContainer<?>) pivot).getContainerPosition());

        return new Connections(
                convertToContainers(scanState.foundEntries),
                convertToContainers(scanState.newEntries),
                convertToContainers(scanState.removedEntries)
        );
    }

    private Set<NetworkNodeContainer<?>> convertToContainers(Set<ScanEntry<?>> foundEntries) {
        return foundEntries.stream().map(ScanEntry::getContainer).collect(Collectors.toSet());
    }

    private Set<ScanEntry<?>> convertToScanEntries(Set<NetworkNodeContainer<?>> existingConnections) {
        return existingConnections
                .stream()
                .map(container -> new ScanEntry<>(
                        container,
                        ((PlatformNetworkNodeContainer<?>) container).getContainerWorld(),
                        ((PlatformNetworkNodeContainer<?>) container).getContainerPosition()
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public List<NetworkNodeContainer<?>> sort(Set<NetworkNodeContainer<?>> containers) {
        return containers
                .stream()
                .sorted(Comparator.comparing(container -> ((PlatformNetworkNodeContainer<?>) container).getContainerPosition()))
                .toList();
    }

    private void depthScan(ScanState scanState, BlockPos position) {
        if (world.getBlockEntity(position) instanceof NetworkNodeContainerBlockEntity<?> c) {
            addEntry(scanState, new ScanEntry<>(c.getContainer(), world, position));
        }
    }

    private void addEntry(ScanState scanState, ScanEntry<?> entry) {
        if (scanState.foundEntries.contains(entry)) {
            return;
        }

        scanState.foundEntries.add(entry);

        if (!scanState.currentEntries.contains(entry)) {
            scanState.newEntries.add(entry);
        }

        scanState.removedEntries.remove(entry);

        Set<NetworkNodeContainer<?>> connections = findConnectionsAt(world, ((PlatformNetworkNodeContainer<?>) entry.getContainer()).getContainerPosition());
        for (NetworkNodeContainer<?> connection : connections) {
            depthScan(scanState, ((PlatformNetworkNodeContainer<?>) connection).getContainerPosition());
        }
    }

    private Set<NetworkNodeContainer<?>> findConnectionsAt(World world, BlockPos pos) {
        Set<NetworkNodeContainer<?>> containers = new HashSet<>();
        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            if (world.getBlockEntity(offsetPos) instanceof NetworkNodeContainerBlockEntity<?> blockEntity) {
                containers.add(blockEntity.getContainer());
            }
        }
        return containers;
    }

    private static class ScanState {
        private final Set<ScanEntry<?>> currentEntries;
        private final Set<ScanEntry<?>> foundEntries = new HashSet<>();
        private final Set<ScanEntry<?>> newEntries = new HashSet<>();
        private final Set<ScanEntry<?>> removedEntries;

        public ScanState(Set<ScanEntry<?>> currentEntries) {
            this.currentEntries = currentEntries;
            this.removedEntries = new HashSet<>(currentEntries);
        }
    }

    private static class ScanEntry<T extends NetworkNode> {
        private final NetworkNodeContainer<T> container;
        private final RegistryKey<World> dimension;
        private final BlockPos position;

        public ScanEntry(NetworkNodeContainer<T> container, World dimension, BlockPos position) {
            this.container = container;
            this.dimension = dimension.getRegistryKey();
            this.position = position;
        }

        public NetworkNodeContainer<T> getContainer() {
            return container;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScanEntry<?> that = (ScanEntry<?>) o;
            return Objects.equals(dimension, that.dimension) && Objects.equals(position, that.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dimension, position);
        }
    }
}

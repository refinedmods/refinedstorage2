package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphListener;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.api.network.node.StorageNetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage.api.storage.Storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

public class MonitorNetworkNode extends AbstractNetworkNode implements GraphListener {
    private final Map<MonitorNodeId, TrackedNode> trackedById = new HashMap<>();
    private final Map<NetworkNode, TrackedNode> trackedByNode = new HashMap<>();
    private final Map<NetworkNodeType, TrackedType> trackedByType = new HashMap<>();
    private final Map<MonitorNodeTypeId, TrackedType> trackedTypeById = new HashMap<>();
    private final Map<MonitorNodeId, Set<NetworkNodeListener>> nodeListeners = new HashMap<>();
    private final Set<MonitorListener> listeners = new HashSet<>();
    private final Set<StorageNetworkNodeDetailsProvider> storageProviders = new HashSet<>();

    private long energyUsage;

    public MonitorNetworkNode(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage;
    }

    @Override
    public void setNetwork(@Nullable final Network newNetwork) {
        if (network != null) {
            network.getComponent(GraphNetworkComponent.class).removeListener(this);
        }
        reset();
        super.setNetwork(newNetwork);
        if (newNetwork != null) {
            final GraphNetworkComponent graph = newNetwork.getComponent(GraphNetworkComponent.class);
            graph.addListener(this);
            // Track the containers that are already present, so we stay consistent regardless of whether
            // nodes were added to the graph before or after we attached as a listener.
            graph.getContainers().forEach(this::onContainerAdded);
        }
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        listeners.forEach(listener -> listener.onActiveChanged(newActive));
    }

    public void addListener(final MonitorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(final MonitorListener listener) {
        listeners.remove(listener);
    }

    public void addNodeListener(final MonitorNodeId id, final NetworkNodeListener listener) {
        final TrackedNode tracked = trackedById.get(id);
        if (tracked == null) {
            return;
        }
        tracked.provider().addListener(listener);
        nodeListeners.computeIfAbsent(id, i -> new HashSet<>()).add(listener);
    }

    public void removeNodeListener(final MonitorNodeId id, final NetworkNodeListener listener) {
        final TrackedNode tracked = trackedById.get(id);
        if (tracked == null) {
            return;
        }
        final Set<NetworkNodeListener> listenersForNode = nodeListeners.get(id);
        if (listenersForNode == null || !listenersForNode.remove(listener)) {
            return;
        }
        if (listenersForNode.isEmpty()) {
            nodeListeners.remove(id);
        }
        tracked.provider().removeListener(listener);
    }

    @Override
    public void onContainerAdded(final NetworkNodeContainer container) {
        // Only nodes that can provide details are of interest to a monitor.
        if (container.getNode() instanceof NetworkNodeDetailsProvider provider) {
            track(container, provider);
        }
    }

    @Override
    public void onContainerRemoved(final NetworkNodeContainer container) {
        final TrackedNode tracked = trackedByNode.get(container.getNode());
        if (tracked != null) {
            untrack(tracked);
        }
    }

    private void track(final NetworkNodeContainer container, final NetworkNodeDetailsProvider provider) {
        final NetworkNode node = container.getNode();
        if (trackedByNode.containsKey(node)) {
            return;
        }
        final MonitorNodeId id = MonitorNodeId.create();
        final TrackedNode tracked = new TrackedNode(id, node, container, provider);
        trackedById.put(id, tracked);
        trackedByNode.put(node, tracked);
        if (provider instanceof StorageNetworkNodeDetailsProvider storageProvider) {
            storageProviders.add(storageProvider);
        }
        final MonitorNodeTypeId typeId = trackType(tracked);
        listeners.forEach(listener -> listener.onNodeTracked(id, typeId));
    }

    private MonitorNodeTypeId trackType(final TrackedNode tracked) {
        final TrackedType trackedType = trackedByType.computeIfAbsent(
            tracked.provider().getType(),
            this::createTrackedType
        );
        trackedType.nodes().add(tracked);
        return trackedType.id();
    }

    private TrackedType createTrackedType(final NetworkNodeType type) {
        final TrackedType trackedType = new TrackedType(MonitorNodeTypeId.create(), type, new HashSet<>());
        trackedTypeById.put(trackedType.id(), trackedType);
        return trackedType;
    }

    private void untrack(final TrackedNode tracked) {
        trackedById.remove(tracked.id());
        trackedByNode.remove(tracked.node());
        if (tracked.provider() instanceof StorageNetworkNodeDetailsProvider storageProvider) {
            storageProviders.remove(storageProvider);
        }
        detachNodeListeners(tracked);
        untrackType(tracked);
        listeners.forEach(listener -> listener.onNodeUntracked(tracked.id()));
    }

    private void untrackType(final TrackedNode tracked) {
        trackedByType.computeIfPresent(tracked.provider().getType(), (type, trackedType) -> {
            trackedType.nodes().remove(tracked);
            if (!trackedType.nodes().isEmpty()) {
                return trackedType;
            }
            trackedTypeById.remove(trackedType.id());
            return null;
        });
    }

    private void detachNodeListeners(final TrackedNode tracked) {
        final Set<NetworkNodeListener> listenersForNode = nodeListeners.remove(tracked.id());
        if (listenersForNode == null) {
            return;
        }
        listenersForNode.forEach(tracked.provider()::removeListener);
    }

    private void reset() {
        new ArrayList<>(trackedById.values()).forEach(this::untrack);
    }

    @Nullable
    public NetworkNode getNode(final MonitorNodeId id) {
        final TrackedNode tracked = trackedById.get(id);
        return tracked == null ? null : tracked.node();
    }

    @Nullable
    public NetworkNodeContainer getContainer(final MonitorNodeId id) {
        final TrackedNode tracked = trackedById.get(id);
        return tracked == null ? null : tracked.container();
    }

    @Nullable
    public NetworkNodeDetailsProvider getDetailsProvider(final MonitorNodeId id) {
        final TrackedNode tracked = trackedById.get(id);
        return tracked == null ? null : tracked.provider();
    }

    @Nullable
    public MonitorNodeId getId(final NetworkNode node) {
        final TrackedNode tracked = trackedByNode.get(node);
        return tracked == null ? null : tracked.id();
    }

    public Collection<NetworkNode> getNodes() {
        return Collections.unmodifiableSet(trackedByNode.keySet());
    }

    public Set<NetworkNode> getNodes(final NetworkNodeType type) {
        final TrackedType trackedType = trackedByType.get(type);
        if (trackedType == null) {
            return Collections.emptySet();
        }
        return trackedType.nodes()
            .stream()
            .map(TrackedNode::node)
            .collect(Collectors.toUnmodifiableSet());
    }

    public Set<NetworkNodeType> getTypes() {
        return Collections.unmodifiableSet(trackedByType.keySet());
    }

    @Nullable
    public NetworkNodeType getType(final MonitorNodeTypeId id) {
        final TrackedType trackedType = trackedTypeById.get(id);
        return trackedType == null ? null : trackedType.type();
    }

    @Nullable
    public MonitorNodeTypeId getTypeId(final NetworkNodeType type) {
        final TrackedType trackedType = trackedByType.get(type);
        return trackedType == null ? null : trackedType.id();
    }

    public int getNodeCount() {
        return trackedById.size();
    }

    public long getTotalEnergyUsage() {
        return trackedById.values()
            .stream()
            .mapToLong(tracked -> tracked.provider().getEnergyUsage())
            .sum();
    }

    public long getEnergyStored() {
        if (network == null) {
            return 0;
        }
        return network.getComponent(EnergyNetworkComponent.class).getStored();
    }

    public long getEnergyCapacity() {
        if (network == null) {
            return 0;
        }
        return network.getComponent(EnergyNetworkComponent.class).getCapacity();
    }

    public Set<StorageNetworkNodeDetailsProvider> getStorages() {
        return Collections.unmodifiableSet(storageProviders);
    }

    public long getStored(final Predicate<Storage> storageFilter) {
        return storageProviders
            .stream()
            .mapToLong(provider -> provider.getStored(storageFilter))
            .sum();
    }

    public long getCapacity(final Predicate<Storage> storageFilter) {
        return storageProviders
            .stream()
            .mapToLong(provider -> provider.getCapacity(storageFilter))
            .sum();
    }

    private record TrackedNode(
        MonitorNodeId id,
        NetworkNode node,
        NetworkNodeContainer container,
        NetworkNodeDetailsProvider provider
    ) {
    }

    private record TrackedType(MonitorNodeTypeId id, NetworkNodeType type, Set<TrackedNode> nodes) {
    }
}

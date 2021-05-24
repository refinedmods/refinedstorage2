package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkImpl;
import com.refinedmods.refinedstorage2.core.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

public class NetworkNodeHostImpl<T extends NetworkNode> implements NetworkNodeHost<T>, NetworkNodeHostVisitor {
    private final Rs2World world;
    private final Position position;
    private final T node;

    public NetworkNodeHostImpl(Rs2World world, Position position, T node) {
        this.world = world;
        this.position = position;
        this.node = node;
    }

    @Override
    public void initialize(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() != null) {
            return;
        }

        NetworkNodeHostVisitorOperatorImpl operator = new NetworkNodeHostVisitorOperatorImpl(hostRepository, Collections.emptySet());
        operator.apply(world, position);
        operator.visitAll();

        Preconditions.checkArgument(operator.getRemovedEntries().isEmpty(), "Cannot have removed entries");

        mergeNetworks(hostRepository, operator.getFoundEntries(), networkComponentRegistry);
    }

    private void mergeNetworks(NetworkNodeHostRepository hostRepository, Set<NetworkNodeHostEntry> foundEntries, NetworkComponentRegistry networkComponentRegistry) {
        Network pivotNetwork = findPivotNetwork(foundEntries).orElseGet(() -> createNetwork(hostRepository, networkComponentRegistry));
        for (NetworkNodeHostEntry entry : foundEntries) {
            entry.getHost().getNode().setNetwork(pivotNetwork);
            boolean nodeIsAlreadyInPivot = pivotNetwork.getComponent(GraphNetworkComponent.class).getHosts().contains(entry);
            if (!nodeIsAlreadyInPivot) {
                pivotNetwork.addHost(entry.getHost());
            }
        }
    }

    private NetworkImpl createNetwork(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        NetworkImpl network = new NetworkImpl(networkComponentRegistry);
        node.setNetwork(network);
        network.addHost(this);
        return network;
    }

    private Optional<Network> findPivotNetwork(Set<NetworkNodeHostEntry> foundEntries) {
        // Sorting is necessary to add some predictability to which pivot is selected
        List<NetworkNodeHostEntry> sortedEntries = foundEntries
                .stream()
                .sorted(Comparator.comparing(a -> a.getHost().getPosition()))
                .collect(Collectors.toList());

        for (NetworkNodeHostEntry entry : sortedEntries) {
            if (entry.getHost() == this) {
                continue;
            }
            Network entryNetwork = entry.getHost().getNode().getNetwork();
            if (entryNetwork != null) {
                return Optional.of(entryNetwork);
            }
        }

        return Optional.empty();
    }

    @Override
    public void remove(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() == null) {
            throw new IllegalStateException("Cannot remove node that has no network yet");
        }

        if (hostRepository.getHost(world, position).isPresent()) {
            throw new IllegalStateException("Host must not be present at removal");
        }

        Set<NetworkNodeHostEntry> hosts = node.getNetwork().getComponent(GraphNetworkComponent.class).getHosts();

        NetworkNodeHostEntry removedHost = NetworkNodeHostEntry.create(this);
        NetworkNodeHostEntry pivot = findPivot(removedHost, hosts);

        if (pivot == null) {
            node.setNetwork(null);
            return;
        }

        NetworkNodeHostVisitorOperatorImpl operator = new NetworkNodeHostVisitorOperatorImpl(hostRepository, hosts);
        operator.apply(pivot.getHost().getHostWorld(), pivot.getHost().getPosition());
        operator.visitAll();

        Preconditions.checkState(operator.getRemovedEntries().contains(removedHost), "The removed host isn't present in the removed entries");

        splitNetworks(hostRepository, operator.getRemovedEntries(), networkComponentRegistry);
    }

    private NetworkNodeHostEntry findPivot(NetworkNodeHostEntry currentEntry, Set<NetworkNodeHostEntry> hosts) {
        for (NetworkNodeHostEntry entry : hosts) {
            if (!entry.equals(currentEntry)) {
                return entry;
            }
        }
        return null;
    }

    private void splitNetworks(NetworkNodeHostRepository hostRepository, Set<NetworkNodeHostEntry> removedEntries, NetworkComponentRegistry networkComponentRegistry) {
        removedEntries.forEach(e -> {
            e.getHost().getNode().getNetwork().removeHost(e.getHost());
            e.getHost().getNode().setNetwork(null);
        });

        removedEntries
                .stream()
                .filter(e -> e.getHost() != this)
                .forEach(removedEntry -> removedEntry.getHost().initialize(hostRepository, networkComponentRegistry));
    }

    @Override
    public T getNode() {
        return node;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public Rs2World getHostWorld() {
        return world;
    }

    @Override
    public void visit(NetworkNodeHostVisitorOperator operator) {
        operator.apply(world, position);
        for (Direction direction : Direction.values()) {
            operator.apply(world, position.offset(direction));
        }
    }
}

package com.refinedmods.refinedstorage2.api.network.node.container;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.energy.CompositeEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

public class NetworkNodeContainerImpl<T extends NetworkNodeImpl> implements NetworkNodeContainer<T> {
    private final T node;

    public NetworkNodeContainerImpl(T node) {
        this.node = node;
    }

    @Override
    public boolean initialize(ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() != null) {
            return false;
        }

        Connections connections = connectionProvider.findConnections(this, Collections.emptySet());

        Preconditions.checkArgument(connections.removedEntries().isEmpty(), "Cannot have removed entries");

        mergeNetworksOfNodes(connectionProvider, connections.foundEntries(), networkComponentRegistry);

        return true;
    }

    private void mergeNetworksOfNodes(ConnectionProvider connectionProvider, Set<NetworkNodeContainer<?>> foundEntries, NetworkComponentRegistry networkComponentRegistry) {
        Network pivotNetwork = findPivotNetworkForMerge(connectionProvider, foundEntries).orElseGet(() -> createNetwork(networkComponentRegistry));

        Set<Network> mergedNetworks = new HashSet<>();

        for (NetworkNodeContainer<?> entry : foundEntries) {
            NetworkNode entryNode = entry.getNode();
            boolean isNotInPivotNetwork = !pivotNetwork.getComponent(GraphNetworkComponent.class).getContainers().contains(entry);
            if (isNotInPivotNetwork) {
                Network mergedNetwork = mergeNetworkOfNode(pivotNetwork, entry, entryNode);
                if (mergedNetwork != null) {
                    mergedNetworks.add(mergedNetwork);
                }
            }
        }

        mergedNetworks.forEach(mn -> mn.merge(pivotNetwork));
    }

    private Network mergeNetworkOfNode(Network newNetwork, NetworkNodeContainer<?> entry, NetworkNode entryNode) {
        Network oldNetwork = entryNode.getNetwork();

        entryNode.setNetwork(newNetwork);
        newNetwork.addContainer(entry);

        return oldNetwork;
    }

    private NetworkImpl createNetwork(NetworkComponentRegistry networkComponentRegistry) {
        NetworkImpl network = new NetworkImpl(networkComponentRegistry);
        node.setNetwork(network);
        network.addContainer(this);
        return network;
    }

    private Optional<Network> findPivotNetworkForMerge(ConnectionProvider connectionProvider, Set<NetworkNodeContainer<?>> foundEntries) {
        for (NetworkNodeContainer<?> entry : connectionProvider.sort(foundEntries)) {
            if (entry == this) {
                continue;
            }
            Network entryNetwork = entry.getNode().getNetwork();
            if (entryNetwork != null) {
                return Optional.of(entryNetwork);
            }
        }

        return Optional.empty();
    }

    @Override
    public void remove(ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() == null) {
            throw new IllegalStateException("Cannot remove node that has no network yet");
        }

        Set<NetworkNodeContainer<?>> containers = node.getNetwork().getComponent(GraphNetworkComponent.class).getContainers();

        NetworkNodeContainer<?> pivot = findPivotNodeForRemove(connectionProvider, containers);

        if (pivot == null) {
            node.getNetwork().remove();
            node.setNetwork(null);
            return;
        }

        Connections connections = connectionProvider.findConnections(pivot, containers);

        Preconditions.checkState(connections.removedEntries().contains(this), "The removed container isn't present in the removed entries");

        splitNetworks(connectionProvider, connections.removedEntries(), networkComponentRegistry, this);
    }

    private NetworkNodeContainer<?> findPivotNodeForRemove(ConnectionProvider connectionProvider, Set<NetworkNodeContainer<?>> containers) {
        for (NetworkNodeContainer<?> entry : connectionProvider.sort(containers)) {
            if (!entry.equals(this)) {
                return entry;
            }
        }
        return null;
    }

    private void splitNetworks(ConnectionProvider connectionProvider,
                               Set<NetworkNodeContainer<?>> removedEntries,
                               NetworkComponentRegistry networkComponentRegistry,
                               NetworkNodeContainer<T> removedEntry) {
        Network networkOfRemovedNode = node.getNetwork();

        removedEntries.forEach(e -> {
            e.getNode().getNetwork().removeContainer(e);
            e.getNode().setNetwork(null);
        });

        Set<Network> networksResultingOfSplit = removedEntries
                .stream()
                .filter(e -> !e.equals(removedEntry))
                .map(e -> {
                    boolean establishedNetwork = e.initialize(connectionProvider, networkComponentRegistry);
                    if (establishedNetwork) {
                        return e.getNode().getNetwork();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (!networksResultingOfSplit.isEmpty()) {
            networkOfRemovedNode.split(networksResultingOfSplit);
        }
    }

    @Override
    public T getNode() {
        return node;
    }

    protected boolean isActive() {
        // TODO controllers will stay on now when they run out of energy I guess?
        long energyUsage = getNode().getEnergyUsage();
        CompositeEnergyStorage energy = getNode().getNetwork().getComponent(EnergyNetworkComponent.class).getEnergyStorage();
        return energy.getStored() >= energyUsage;
    }

    @Override
    public void update() {
        boolean active = isActive();
        if (active != getNode().isActive()) {
            node.setActive(active);
        }
        if (active) {
            node.update();
        }
    }
}

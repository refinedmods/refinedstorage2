package com.refinedmods.refinedstorage2.api.network.node;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

public class NetworkBuilder {
    public static final NetworkBuilder INSTANCE = new NetworkBuilder();

    public boolean initialize(NetworkNodeContainer container, ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry) {
        if (container.getNode().getNetwork() != null) {
            return false;
        }

        Connections connections = connectionProvider.findConnections(container, Collections.emptySet());

        Preconditions.checkArgument(connections.removedEntries().isEmpty(), "Cannot have removed entries");

        mergeNetworksOfNodes(connectionProvider, container, connections.foundEntries(), networkComponentRegistry);

        return true;
    }

    private void mergeNetworksOfNodes(ConnectionProvider connectionProvider, NetworkNodeContainer pivot, Set<NetworkNodeContainer> foundEntries, NetworkComponentRegistry networkComponentRegistry) {
        Network pivotNetwork = findPivotNetworkForMerge(connectionProvider, pivot, foundEntries).orElseGet(() -> createNetwork(pivot, networkComponentRegistry));

        Set<Network> mergedNetworks = new HashSet<>();

        for (NetworkNodeContainer entry : foundEntries) {
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

    private Network mergeNetworkOfNode(Network newNetwork, NetworkNodeContainer entry, NetworkNode entryNode) {
        Network oldNetwork = entryNode.getNetwork();

        entryNode.setNetwork(newNetwork);
        newNetwork.addContainer(entry);

        return oldNetwork;
    }

    private NetworkImpl createNetwork(NetworkNodeContainer container, NetworkComponentRegistry networkComponentRegistry) {
        NetworkImpl network = new NetworkImpl(networkComponentRegistry);
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return network;
    }

    private Optional<Network> findPivotNetworkForMerge(ConnectionProvider connectionProvider, NetworkNodeContainer addedContainer, Set<NetworkNodeContainer> foundEntries) {
        for (NetworkNodeContainer entry : connectionProvider.sort(foundEntries)) {
            if (entry == addedContainer) {
                continue;
            }
            Network entryNetwork = entry.getNode().getNetwork();
            if (entryNetwork != null) {
                return Optional.of(entryNetwork);
            }
        }

        return Optional.empty();
    }

    public void remove(NetworkNodeContainer container, ConnectionProvider connectionProvider, NetworkComponentRegistry networkComponentRegistry) {
        Network network = container.getNode().getNetwork();
        if (network == null) {
            throw new IllegalStateException("Cannot remove node that has no network yet");
        }

        Set<NetworkNodeContainer> containers = network.getComponent(GraphNetworkComponent.class).getContainers();

        NetworkNodeContainer pivot = findPivotNodeForRemove(connectionProvider, container, containers);

        if (pivot == null) {
            network.remove();
            container.getNode().setNetwork(null);
            return;
        }

        Connections connections = connectionProvider.findConnections(pivot, containers);

        Preconditions.checkState(connections.removedEntries().contains(container), "The removed container isn't present in the removed entries");

        splitNetworks(connectionProvider, connections.removedEntries(), networkComponentRegistry, container);
    }

    private NetworkNodeContainer findPivotNodeForRemove(ConnectionProvider connectionProvider, NetworkNodeContainer removedContainer, Set<NetworkNodeContainer> containers) {
        for (NetworkNodeContainer entry : connectionProvider.sort(containers)) {
            if (!entry.equals(removedContainer)) {
                return entry;
            }
        }
        return null;
    }

    private void splitNetworks(ConnectionProvider connectionProvider,
                               Set<NetworkNodeContainer> removedEntries,
                               NetworkComponentRegistry networkComponentRegistry,
                               NetworkNodeContainer removedEntry) {
        Network networkOfRemovedNode = removedEntry.getNode().getNetwork();

        removedEntries.forEach(e -> {
            e.getNode().getNetwork().removeContainer(e);
            e.getNode().setNetwork(null);
        });

        Set<Network> networksResultingOfSplit = removedEntries
                .stream()
                .filter(e -> !e.equals(removedEntry))
                .map(e -> {
                    boolean establishedNetwork = initialize(e, connectionProvider, networkComponentRegistry);
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
}

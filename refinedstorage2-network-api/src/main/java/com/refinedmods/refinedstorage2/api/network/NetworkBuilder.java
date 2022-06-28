package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

public class NetworkBuilder {
    private final NetworkFactory networkFactory;

    public NetworkBuilder(final NetworkFactory networkFactory) {
        this.networkFactory = networkFactory;
    }

    public boolean initialize(final NetworkNodeContainer container, final ConnectionProvider connectionProvider) {
        if (container.getNode().getNetwork() != null) {
            return false;
        }
        final Connections connections = connectionProvider.findConnections(container, Collections.emptySet());
        Preconditions.checkArgument(connections.removedEntries().isEmpty(), "Cannot have removed entries");
        mergeNetworksOfNodes(connectionProvider, container, connections.foundEntries());
        return true;
    }

    private void mergeNetworksOfNodes(final ConnectionProvider connectionProvider,
                                      final NetworkNodeContainer pivot,
                                      final Set<NetworkNodeContainer> foundEntries) {
        final Network pivotNetwork = findPivotNetworkForMerge(connectionProvider, pivot, foundEntries)
                .orElseGet(() -> createNetwork(pivot));

        final Set<Network> mergedNetworks = new HashSet<>();

        for (final NetworkNodeContainer entry : foundEntries) {
            final NetworkNode entryNode = entry.getNode();
            final boolean isNotInPivotNetwork = !pivotNetwork.getComponent(GraphNetworkComponent.class)
                    .getContainers().contains(entry);
            if (isNotInPivotNetwork) {
                final Network mergedNetwork = mergeNetworkOfNode(pivotNetwork, entry, entryNode);
                if (mergedNetwork != null) {
                    mergedNetworks.add(mergedNetwork);
                }
            }
        }

        mergedNetworks.forEach(mn -> mn.merge(pivotNetwork));
    }

    @Nullable
    private Network mergeNetworkOfNode(final Network newNetwork,
                                       final NetworkNodeContainer entry,
                                       final NetworkNode entryNode) {
        final Network oldNetwork = entryNode.getNetwork();
        entryNode.setNetwork(newNetwork);
        newNetwork.addContainer(entry);
        return oldNetwork;
    }

    private Network createNetwork(final NetworkNodeContainer container) {
        final Network network = networkFactory.create();
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return network;
    }

    private Optional<Network> findPivotNetworkForMerge(final ConnectionProvider connectionProvider,
                                                       final NetworkNodeContainer addedContainer,
                                                       final Set<NetworkNodeContainer> foundEntries) {
        for (final NetworkNodeContainer entry : connectionProvider.sort(foundEntries)) {
            if (entry == addedContainer) {
                continue;
            }
            final Network entryNetwork = entry.getNode().getNetwork();
            if (entryNetwork != null) {
                return Optional.of(entryNetwork);
            }
        }
        return Optional.empty();
    }

    public void remove(final NetworkNodeContainer container, final ConnectionProvider connectionProvider) {
        final Network network = container.getNode().getNetwork();
        if (network == null) {
            throw new IllegalStateException("Cannot remove node that has no network yet");
        }

        final Set<NetworkNodeContainer> containers = network.getComponent(GraphNetworkComponent.class).getContainers();

        final NetworkNodeContainer pivot = findPivotNodeForRemove(connectionProvider, container, containers);

        if (pivot == null) {
            network.remove();
            container.getNode().setNetwork(null);
            return;
        }

        final Connections connections = connectionProvider.findConnections(pivot, containers);
        Preconditions.checkState(
                connections.removedEntries().contains(container),
                "The removed container isn't present in the removed entries"
        );
        splitNetworks(connectionProvider, connections.removedEntries(), container);
    }

    @Nullable
    private NetworkNodeContainer findPivotNodeForRemove(final ConnectionProvider connectionProvider,
                                                        final NetworkNodeContainer removedContainer,
                                                        final Set<NetworkNodeContainer> containers) {
        for (final NetworkNodeContainer entry : connectionProvider.sort(containers)) {
            if (!entry.equals(removedContainer)) {
                return entry;
            }
        }
        return null;
    }

    private void splitNetworks(final ConnectionProvider connectionProvider,
                               final Set<NetworkNodeContainer> removedEntries,
                               final NetworkNodeContainer removedEntry) {
        final Network networkOfRemovedNode = removedEntry.getNode().getNetwork();
        if (networkOfRemovedNode == null) {
            throw new IllegalStateException("Network of removed node cannot be empty");
        }

        removedEntries.forEach(e -> {
            if (e.getNode().getNetwork() == null) {
                throw new IllegalStateException("Network of resulting removed node cannot be empty");
            }
            e.getNode().getNetwork().removeContainer(e);
            e.getNode().setNetwork(null);
        });

        final Set<Network> networksResultingOfSplit = removedEntries
                .stream()
                .filter(e -> !e.equals(removedEntry))
                .map(e -> {
                    final boolean establishedNetwork = initialize(e, connectionProvider);
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

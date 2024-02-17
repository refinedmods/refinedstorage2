package com.refinedmods.refinedstorage2.api.network.impl;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.network.ConnectionProvider;
import com.refinedmods.refinedstorage2.api.network.Connections;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class NetworkBuilderImpl implements NetworkBuilder {
    private static final Comparator<NetworkNodeContainer> LOWEST_PRIORITY_FIRST = Comparator.comparingInt(
        NetworkNodeContainer::getPriority
    );
    private static final Comparator<NetworkNodeContainer> HIGHEST_PRIORITY_FIRST = LOWEST_PRIORITY_FIRST.reversed();

    private final NetworkFactory networkFactory;

    public NetworkBuilderImpl(final NetworkFactory networkFactory) {
        this.networkFactory = networkFactory;
    }

    @Override
    public boolean initialize(final NetworkNodeContainer container, final ConnectionProvider connectionProvider) {
        if (container.getNode().getNetwork() != null) {
            return false;
        }
        final Connections connections = connectionProvider.findConnections(container, Collections.emptySet());
        CoreValidations.validateEmpty(
            connections.removedEntries(),
            "Cannot have removed entries when starting from empty existing connections"
        );
        mergeNetworksOfNodes(connectionProvider, container, connections.foundEntries(), false);
        return true;
    }

    private void mergeNetworksOfNodes(final ConnectionProvider connectionProvider,
                                      final NetworkNodeContainer pivot,
                                      final Set<NetworkNodeContainer> foundEntries,
                                      final boolean pivotAlreadyHasNetwork) {
        final Network pivotNetwork = pivotAlreadyHasNetwork
            ? CoreValidations.validateNotNull(pivot.getNode().getNetwork(), "Pivot must have network")
            : findPivotNetworkForMerge(connectionProvider, pivot, foundEntries).orElseGet(() -> createNetwork(pivot));

        final Set<Network> mergedNetworks = new HashSet<>();

        for (final NetworkNodeContainer entry : connectionProvider.sortDeterministically(foundEntries)) {
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
        for (final NetworkNodeContainer entry : connectionProvider.sortDeterministically(foundEntries)) {
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

    @Override
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
        CoreValidations.validateContains(
            connections.removedEntries(),
            container,
            "The removed container should be present in the removed entries, but isn't"
        );
        splitNetworks(connectionProvider, connections.removedEntries(), container);
    }

    @Override
    public void update(final NetworkNodeContainer container, final ConnectionProvider connectionProvider) {
        final Network network = container.getNode().getNetwork();
        if (network == null) {
            throw new IllegalStateException("Cannot update node that has no network yet");
        }

        final Set<NetworkNodeContainer> containers = network.getComponent(GraphNetworkComponent.class).getContainers();

        final Connections connections = connectionProvider.findConnections(container, containers);
        splitNetworks(connectionProvider, connections.removedEntries(), container);
        mergeNetworksOfNodes(connectionProvider, container, connections.foundEntries(), true);
    }

    @Nullable
    private NetworkNodeContainer findPivotNodeForRemove(final ConnectionProvider connectionProvider,
                                                        final NetworkNodeContainer removedContainer,
                                                        final Set<NetworkNodeContainer> containers) {
        for (final NetworkNodeContainer entry : connectionProvider.sortDeterministically(containers)) {
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

        connectionProvider.sortDeterministically(removedEntries).stream().sorted(HIGHEST_PRIORITY_FIRST).forEach(e -> {
            if (e.getNode().getNetwork() == null) {
                throw new IllegalStateException("Network of resulting removed node cannot be empty");
            }
            e.getNode().getNetwork().removeContainer(e);
            e.getNode().setNetwork(null);
        });

        final Set<Network> networksResultingOfSplit = connectionProvider.sortDeterministically(removedEntries)
            .stream()
            .filter(e -> !e.equals(removedEntry))
            .sorted(HIGHEST_PRIORITY_FIRST)
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

package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkImpl;
import com.refinedmods.refinedstorage2.core.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

public class NetworkNodeContainerImpl<T extends NetworkNode> implements NetworkNodeContainer<T> {
    protected Rs2World world;
    protected final Position position;
    private final T node;

    public NetworkNodeContainerImpl(Position position, T node) {
        this.position = position;
        this.node = node;
    }

    @Override
    public boolean initialize(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() != null) {
            return false;
        }

        ConnectionScanner scanner = new ConnectionScanner(containerRepository, Collections.emptySet());
        scanner.scan(world, position);

        Preconditions.checkArgument(scanner.getRemovedEntries().isEmpty(), "Cannot have removed entries");

        mergeNetworksOfNodes(scanner.getFoundEntries(), networkComponentRegistry);

        return true;
    }

    private void mergeNetworksOfNodes(Set<NetworkNodeContainerEntry<?>> foundEntries, NetworkComponentRegistry networkComponentRegistry) {
        Network pivotNetwork = findPivotNetworkForMerge(foundEntries).orElseGet(() -> createNetwork(networkComponentRegistry));

        Set<Network> mergedNetworks = new HashSet<>();

        for (NetworkNodeContainerEntry<?> entry : foundEntries) {
            NetworkNode entryNode = entry.getContainer().getNode();
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

    private Network mergeNetworkOfNode(Network newNetwork, NetworkNodeContainerEntry<?> entry, NetworkNode entryNode) {
        Network oldNetwork = entryNode.getNetwork();

        entryNode.setNetwork(newNetwork);
        newNetwork.addContainer(entry.getContainer());

        return oldNetwork;
    }

    private NetworkImpl createNetwork(NetworkComponentRegistry networkComponentRegistry) {
        NetworkImpl network = new NetworkImpl(networkComponentRegistry);
        node.setNetwork(network);
        network.addContainer(this);
        return network;
    }

    private Optional<Network> findPivotNetworkForMerge(Set<NetworkNodeContainerEntry<?>> foundEntries) {
        for (NetworkNodeContainerEntry<?> entry : sortEntries(foundEntries)) {
            if (entry.getContainer() == this) {
                continue;
            }
            Network entryNetwork = entry.getContainer().getNode().getNetwork();
            if (entryNetwork != null) {
                return Optional.of(entryNetwork);
            }
        }

        return Optional.empty();
    }

    private List<NetworkNodeContainerEntry<?>> sortEntries(Set<NetworkNodeContainerEntry<?>> containers) {
        return containers
                .stream()
                .sorted(Comparator.comparing(a -> a.getContainer().getPosition()))
                .toList();
    }

    @Override
    public void remove(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() == null) {
            throw new IllegalStateException("Cannot remove node that has no network yet");
        }

        if (containerRepository.getContainer(world, position).isPresent()) {
            throw new IllegalStateException("Container must not be present at removal");
        }

        Set<NetworkNodeContainerEntry<?>> containers = node.getNetwork().getComponent(GraphNetworkComponent.class).getContainers();

        NetworkNodeContainerEntry<T> removedContainer = NetworkNodeContainerEntry.create(this);
        NetworkNodeContainerEntry<?> pivot = findPivotNodeForRemove(removedContainer, containers);

        if (pivot == null) {
            node.getNetwork().remove();
            node.setNetwork(null);
            return;
        }

        ConnectionScanner scanner = new ConnectionScanner(containerRepository, containers);
        scanner.scan(pivot.getContainer().getContainerWorld(), pivot.getContainer().getPosition());

        Preconditions.checkState(scanner.getRemovedEntries().contains(removedContainer), "The removed container isn't present in the removed entries");

        splitNetworks(containerRepository, scanner.getRemovedEntries(), networkComponentRegistry, removedContainer);
    }

    private NetworkNodeContainerEntry<?> findPivotNodeForRemove(NetworkNodeContainerEntry<T> currentEntry, Set<NetworkNodeContainerEntry<?>> containers) {
        for (NetworkNodeContainerEntry<?> entry : sortEntries(containers)) {
            if (!entry.equals(currentEntry)) {
                return entry;
            }
        }
        return null;
    }

    private void splitNetworks(NetworkNodeContainerRepository containerRepository,
                               Set<NetworkNodeContainerEntry<?>> removedEntries,
                               NetworkComponentRegistry networkComponentRegistry,
                               NetworkNodeContainerEntry<T> removedEntry) {
        Network networkOfRemovedNode = node.getNetwork();

        removedEntries.forEach(e -> {
            e.getContainer().getNode().getNetwork().removeContainer(e.getContainer());
            e.getContainer().getNode().setNetwork(null);
        });

        Set<Network> networksResultingOfSplit = removedEntries
                .stream()
                .filter(e -> !e.equals(removedEntry))
                .map(e -> {
                    boolean establishedNetwork = e.getContainer().initialize(containerRepository, networkComponentRegistry);
                    if (establishedNetwork) {
                        return e.getContainer().getNode().getNetwork();
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

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public Rs2World getContainerWorld() {
        return world;
    }

    @Override
    public void setContainerWorld(Rs2World world) {
        this.world = world;
        this.node.setWorld(world);
    }

    @Override
    public List<NetworkNodeContainer<?>> getConnections(NetworkNodeContainerRepository containerRepository) {
        List<NetworkNodeContainer<?>> connections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            containerRepository.getContainer(world, position.offset(direction)).ifPresent(container -> {
                if (container.canConnectWith(this, direction.getOpposite())) {
                    connections.add(container);
                }
            });
        }
        return connections;
    }

    @Override
    public boolean canConnectWith(NetworkNodeContainer<?> other, Direction incomingDirection) {
        return true;
    }
}

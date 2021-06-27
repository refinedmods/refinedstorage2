package com.refinedmods.refinedstorage2.core.network.host;

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

public class NetworkNodeHostImpl<T extends NetworkNode> implements NetworkNodeHost<T> {
    protected final Rs2World world;
    protected final Position position;
    private final T node;

    public NetworkNodeHostImpl(Rs2World world, Position position, T node) {
        this.world = world;
        this.position = position;
        this.node = node;
    }

    @Override
    public boolean initialize(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() != null) {
            return false;
        }

        ConnectionScanner scanner = new ConnectionScanner(hostRepository, Collections.emptySet());
        scanner.scan(world, position);

        Preconditions.checkArgument(scanner.getRemovedEntries().isEmpty(), "Cannot have removed entries");

        mergeNetworksOfNodes(scanner.getFoundEntries(), networkComponentRegistry);

        return true;
    }

    private void mergeNetworksOfNodes(Set<NetworkNodeHostEntry<?>> foundEntries, NetworkComponentRegistry networkComponentRegistry) {
        Network pivotNetwork = findPivotNetworkForMerge(foundEntries).orElseGet(() -> createNetwork(networkComponentRegistry));

        Set<Network> mergedNetworks = new HashSet<>();

        for (NetworkNodeHostEntry<?> entry : foundEntries) {
            NetworkNode entryNode = entry.getHost().getNode();
            boolean isNotInPivotNetwork = !pivotNetwork.getComponent(GraphNetworkComponent.class).getHosts().contains(entry);
            if (isNotInPivotNetwork) {
                Network mergedNetwork = mergeNetworkOfNode(pivotNetwork, entry, entryNode);
                if (mergedNetwork != null) {
                    mergedNetworks.add(mergedNetwork);
                }
            }
        }

        mergedNetworks.forEach(mn -> mn.merge(pivotNetwork));
    }

    private Network mergeNetworkOfNode(Network newNetwork, NetworkNodeHostEntry<?> entry, NetworkNode entryNode) {
        Network oldNetwork = entryNode.getNetwork();

        entryNode.setNetwork(newNetwork);
        newNetwork.addHost(entry.getHost());

        return oldNetwork;
    }

    private NetworkImpl createNetwork(NetworkComponentRegistry networkComponentRegistry) {
        NetworkImpl network = new NetworkImpl(networkComponentRegistry);
        node.setNetwork(network);
        network.addHost(this);
        return network;
    }

    private Optional<Network> findPivotNetworkForMerge(Set<NetworkNodeHostEntry<?>> foundEntries) {
        for (NetworkNodeHostEntry<?> entry : sortEntries(foundEntries)) {
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

    private List<NetworkNodeHostEntry<?>> sortEntries(Set<NetworkNodeHostEntry<?>> foundEntries) {
        return foundEntries
                .stream()
                .sorted(Comparator.comparing(a -> a.getHost().getPosition()))
                .collect(Collectors.toList());
    }

    @Override
    public void remove(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        if (node.getNetwork() == null) {
            throw new IllegalStateException("Cannot remove node that has no network yet");
        }

        if (hostRepository.getHost(world, position).isPresent()) {
            throw new IllegalStateException("Host must not be present at removal");
        }

        Set<NetworkNodeHostEntry<?>> hosts = node.getNetwork().getComponent(GraphNetworkComponent.class).getHosts();

        NetworkNodeHostEntry<T> removedHost = NetworkNodeHostEntry.create(this);
        NetworkNodeHostEntry<?> pivot = findPivotNodeForRemove(removedHost, hosts);

        if (pivot == null) {
            node.getNetwork().remove();
            node.setNetwork(null);
            return;
        }

        ConnectionScanner scanner = new ConnectionScanner(hostRepository, hosts);
        scanner.scan(pivot.getHost().getHostWorld(), pivot.getHost().getPosition());

        Preconditions.checkState(scanner.getRemovedEntries().contains(removedHost), "The removed host isn't present in the removed entries");

        splitNetworks(hostRepository, scanner.getRemovedEntries(), networkComponentRegistry, removedHost);
    }

    private NetworkNodeHostEntry<?> findPivotNodeForRemove(NetworkNodeHostEntry<T> currentEntry, Set<NetworkNodeHostEntry<?>> hosts) {
        for (NetworkNodeHostEntry<?> entry : sortEntries(hosts)) {
            if (!entry.equals(currentEntry)) {
                return entry;
            }
        }
        return null;
    }

    private void splitNetworks(NetworkNodeHostRepository hostRepository,
                               Set<NetworkNodeHostEntry<?>> removedEntries,
                               NetworkComponentRegistry networkComponentRegistry,
                               NetworkNodeHostEntry<T> removedEntry) {
        Network networkOfRemovedNode = node.getNetwork();

        removedEntries.forEach(e -> {
            e.getHost().getNode().getNetwork().removeHost(e.getHost());
            e.getHost().getNode().setNetwork(null);
        });

        Set<Network> networksResultingOfSplit = removedEntries
                .stream()
                .filter(e -> !e.equals(removedEntry))
                .map(e -> {
                    boolean establishedNetwork = e.getHost().initialize(hostRepository, networkComponentRegistry);
                    if (establishedNetwork) {
                        return e.getHost().getNode().getNetwork();
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
    public Rs2World getHostWorld() {
        return world;
    }

    @Override
    public List<NetworkNodeHost<?>> getConnections(NetworkNodeHostRepository hostRepository) {
        List<NetworkNodeHost<?>> connections = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            hostRepository.getHost(world, position.offset(direction)).ifPresent(host -> {
                if (host.canConnectWith(this, direction.getOpposite())) {
                    connections.add(host);
                }
            });
        }
        return connections;
    }

    @Override
    public boolean canConnectWith(NetworkNodeHost<?> other, Direction incomingDirection) {
        return true;
    }
}

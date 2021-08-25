package com.refinedmods.refinedstorage2.api.network.node.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FakeConnectionProvider implements ConnectionProvider {
    private final Map<NetworkNodeContainer<?>, List<NetworkNodeContainer<?>>> connections = new HashMap<>();
    private final List<NetworkNodeContainer<?>> allowed = new ArrayList<>();

    public FakeConnectionProvider with(NetworkNodeContainer<?>... containers) {
        for (NetworkNodeContainer<?> container : containers) {
            with(container);
        }
        return this;
    }

    public FakeConnectionProvider with(NetworkNodeContainer<?> container) {
        if (allowed.contains(container)) {
            throw new IllegalArgumentException();
        }
        allowed.add(container);
        return this;
    }

    public FakeConnectionProvider connect(NetworkNodeContainer<?> from, NetworkNodeContainer<?> to) {
        if (!allowed.contains(from) || !allowed.contains(to)) {
            throw new IllegalArgumentException();
        }
        doConnect(from, to);
        doConnect(to, from);
        return this;
    }

    private void doConnect(NetworkNodeContainer<?> from, NetworkNodeContainer<?> to) {
        connections.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
    }

    @Override
    public Connections findConnections(NetworkNodeContainer<?> pivot, Set<NetworkNodeContainer<?>> existingConnections) {
        if (!allowed.contains(pivot)) {
            throw new IllegalArgumentException();
        }

        Set<NetworkNodeContainer<?>> foundEntries = new HashSet<>();

        depthScan(foundEntries, pivot);

        return new Connections(
                foundEntries,
                foundEntries.stream().filter(e -> !existingConnections.contains(e)).collect(Collectors.toSet()),
                existingConnections.stream().filter(e -> !foundEntries.contains(e)).collect(Collectors.toSet())
        );
    }

    private void depthScan(Set<NetworkNodeContainer<?>> foundEntries, NetworkNodeContainer<?> from) {
        if (!foundEntries.add(from)) {
            return;
        }

        connections.getOrDefault(from, Collections.emptyList()).forEach(container -> depthScan(foundEntries, container));
    }

    @Override
    public List<NetworkNodeContainer<?>> sort(Set<NetworkNodeContainer<?>> containers) {
        return containers
                .stream()
                .sorted(Comparator.comparingInt(allowed::indexOf))
                .collect(Collectors.toList());
    }
}

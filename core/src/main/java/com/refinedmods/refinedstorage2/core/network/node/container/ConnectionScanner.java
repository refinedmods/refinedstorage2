package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.api.core.Position;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ConnectionScanner {
    private final Set<NetworkNodeContainerEntry<?>> currentEntries;
    private final Set<NetworkNodeContainerEntry<?>> foundEntries = new HashSet<>();
    private final Set<NetworkNodeContainerEntry<?>> newEntries = new HashSet<>();
    private final Set<NetworkNodeContainerEntry<?>> removedEntries;
    private final Queue<Runnable> visitors = new ArrayDeque<>();

    public ConnectionScanner(Set<NetworkNodeContainerEntry<?>> currentEntries) {
        this.currentEntries = currentEntries;
        this.removedEntries = new HashSet<>(currentEntries);
    }

    public void scan(NetworkNodeContainerRepository containerRepository, Position position) {
        addVisitorIfPossibleAt(containerRepository, position);

        Runnable currentVisitor;
        while ((currentVisitor = visitors.poll()) != null) {
            currentVisitor.run();
        }
    }

    private void addVisitorIfPossibleAt(NetworkNodeContainerRepository containerRepository, Position position) {
        containerRepository.getContainer(position).ifPresent(container -> addEntry(containerRepository, NetworkNodeContainerEntry.create(container)));
    }

    private void addEntry(NetworkNodeContainerRepository containerRepository, NetworkNodeContainerEntry<?> entry) {
        if (foundEntries.contains(entry)) {
            return;
        }

        foundEntries.add(entry);

        if (!currentEntries.contains(entry)) {
            newEntries.add(entry);
        }

        removedEntries.remove(entry);

        List<NetworkNodeContainer<?>> connections = entry.getContainer().getConnections(containerRepository);
        for (NetworkNodeContainer<?> connection : connections) {
            addVisitorIfPossibleAt(containerRepository, connection.getPosition());
        }
    }

    public Set<NetworkNodeContainerEntry<?>> getFoundEntries() {
        return foundEntries;
    }

    public Set<NetworkNodeContainerEntry<?>> getNewEntries() {
        return newEntries;
    }

    public Set<NetworkNodeContainerEntry<?>> getRemovedEntries() {
        return removedEntries;
    }
}

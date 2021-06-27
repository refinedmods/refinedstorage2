package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ConnectionScanner {
    private final NetworkNodeHostRepository hostRepository;
    private final Set<NetworkNodeHostEntry<?>> currentEntries;
    private final Set<NetworkNodeHostEntry<?>> foundEntries = new HashSet<>();
    private final Set<NetworkNodeHostEntry<?>> newEntries = new HashSet<>();
    private final Set<NetworkNodeHostEntry<?>> removedEntries;
    private final Queue<Runnable> visitors = new ArrayDeque<>();

    public ConnectionScanner(NetworkNodeHostRepository hostRepository, Set<NetworkNodeHostEntry<?>> currentEntries) {
        this.hostRepository = hostRepository;
        this.currentEntries = currentEntries;
        this.removedEntries = new HashSet<>(currentEntries);
    }

    public void scan(Rs2World world, Position position) {
        addVisitorIfPossibleAt(world, position);

        Runnable currentVisitor;
        while ((currentVisitor = visitors.poll()) != null) {
            currentVisitor.run();
        }
    }

    private void addVisitorIfPossibleAt(Rs2World world, Position position) {
        hostRepository.getHost(world, position).ifPresent(host -> addEntry(NetworkNodeHostEntry.create(host)));
    }

    private void addEntry(NetworkNodeHostEntry<?> entry) {
        if (foundEntries.contains(entry)) {
            return;
        }

        foundEntries.add(entry);

        if (!currentEntries.contains(entry)) {
            newEntries.add(entry);
        }

        removedEntries.remove(entry);

        List<NetworkNodeHost<?>> connections = entry.getHost().getConnections(hostRepository);
        for (NetworkNodeHost<?> connection : connections) {
            addVisitorIfPossibleAt(connection.getHostWorld(), connection.getPosition());
        }
    }

    public Set<NetworkNodeHostEntry<?>> getFoundEntries() {
        return foundEntries;
    }

    public Set<NetworkNodeHostEntry<?>> getNewEntries() {
        return newEntries;
    }

    public Set<NetworkNodeHostEntry<?>> getRemovedEntries() {
        return removedEntries;
    }
}

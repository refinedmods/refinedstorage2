package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class NetworkNodeHostVisitorOperatorImpl implements NetworkNodeHostVisitorOperator {
    private final NetworkNodeHostRepository hostRepository;
    private final Set<NetworkNodeHostEntry<?>> currentEntries;
    private final Set<NetworkNodeHostEntry<?>> foundEntries = new HashSet<>();
    private final Set<NetworkNodeHostEntry<?>> newEntries = new HashSet<>();
    private final Set<NetworkNodeHostEntry<?>> removedEntries;
    private final Queue<NetworkNodeHostVisitor> visitors = new ArrayDeque<>();

    public NetworkNodeHostVisitorOperatorImpl(NetworkNodeHostRepository hostRepository, Set<NetworkNodeHostEntry<?>> currentEntries) {
        this.hostRepository = hostRepository;
        this.currentEntries = currentEntries;
        this.removedEntries = new HashSet<>(currentEntries);
    }

    public void visitAll() {
        NetworkNodeHostVisitor currentVisitor;
        while ((currentVisitor = visitors.poll()) != null) {
            currentVisitor.visit(this);
        }
    }

    @Override
    public void apply(Rs2World world, Position position) {
        hostRepository.getHost(world, position).ifPresent(host -> {
            NetworkNodeHostEntry<?> entry = NetworkNodeHostEntry.create(host);
            if (foundEntries.add(entry)) {
                addEntry(entry);
            }
        });
    }

    private void addEntry(NetworkNodeHostEntry<?> entry) {
        if (!currentEntries.contains(entry)) {
            newEntries.add(entry);
        }
        removedEntries.remove(entry);

        if (entry.getHost() instanceof NetworkNodeHostVisitor) {
            visitors.add((NetworkNodeHostVisitor) entry.getHost());
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

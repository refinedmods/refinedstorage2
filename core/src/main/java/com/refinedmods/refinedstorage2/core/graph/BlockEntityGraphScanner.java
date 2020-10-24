package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.adapter.WorldAdapter;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class BlockEntityGraphScanner<T extends BlockEntity> implements GraphScanner<T> {
    private final Class<T> blockEntityClass;

    public BlockEntityGraphScanner(Class<T> blockEntityClass) {
        this.blockEntityClass = blockEntityClass;
    }

    @Override
    public GraphScannerResult<T> scanAt(WorldAdapter worldAdapter, BlockPos pos, Set<GraphEntry<T>> previousEntries) {
        Set<GraphEntry<T>> allEntries = new HashSet<>();
        Set<GraphEntry<T>> removedEntries = new HashSet<>(previousEntries);
        Set<GraphEntry<T>> newEntries = new HashSet<>();

        Queue<GraphScannerRequest> requests = new ArrayDeque<>();
        requests.add(new GraphScannerRequest(worldAdapter, pos));

        GraphScannerRequest request;
        while ((request = requests.poll()) != null) {
            handleRequest(allEntries, removedEntries, newEntries, request, requests);
        }

        return new GraphScannerResult<>(newEntries, removedEntries, allEntries);
    }

    private void handleRequest(Set<GraphEntry<T>> allEntries, Set<GraphEntry<T>> previousEntries, Set<GraphEntry<T>> newEntries, GraphScannerRequest request, Queue<GraphScannerRequest> requests) {
        request.getWorldAdapter().getBlockEntity(request.getPos()).ifPresent(blockEntity -> {
            if (blockEntity.getClass().isAssignableFrom(blockEntityClass)) {
                GraphEntry<T> entry = new GraphEntry<>(request.getWorldAdapter().getIdentifier(), request.getPos(), (T) blockEntity);

                if (allEntries.add(entry)) {
                    for (Direction direction : Direction.values()) {
                        requests.add(new GraphScannerRequest(request.getWorldAdapter(), request.getPos().offset(direction)));
                    }

                    boolean entryExistedPreviously = previousEntries.remove(entry);
                    if (!entryExistedPreviously) {
                        newEntries.add(entry);
                    }
                }
            }
        });
    }
}

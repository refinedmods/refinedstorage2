package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.adapter.WorldAdapter;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.Set;

public interface GraphScanner<T> {
    GraphScannerResult<T> scanAt(WorldAdapter worldAdapter, BlockPos pos, Set<GraphEntry<T>> previousEntries);

    default GraphScannerResult<T> scanAt(WorldAdapter worldAdapter, BlockPos pos) {
        return scanAt(worldAdapter, pos, Collections.emptySet());
    }
}

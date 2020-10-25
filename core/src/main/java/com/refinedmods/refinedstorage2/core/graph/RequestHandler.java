package com.refinedmods.refinedstorage2.core.graph;

public interface RequestHandler<T, R> {
    void handle(R request, GraphScannerContext<T, R> context);
}

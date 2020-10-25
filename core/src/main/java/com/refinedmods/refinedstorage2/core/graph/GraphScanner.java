package com.refinedmods.refinedstorage2.core.graph;

import java.util.Collections;
import java.util.Set;

public class GraphScanner<T, R> {
    public GraphScannerResult<T> scanAt(R initialRequest, Set<T> previousEntries, RequestHandler<T, R> requestHandler) {
        GraphScannerContext<T, R> context = new GraphScannerContext<>(previousEntries);

        context.addRequest(initialRequest);

        R request;
        while ((request = context.getRequests().poll()) != null) {
            requestHandler.handle(request, context);
        }

        return context.toResult();
    }

    public GraphScannerResult<T> scanAt(R initialRequest, RequestHandler<T, R> requestHandler) {
        return scanAt(initialRequest, Collections.emptySet(), requestHandler);
    }
}

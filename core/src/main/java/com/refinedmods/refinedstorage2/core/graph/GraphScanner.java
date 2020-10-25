package com.refinedmods.refinedstorage2.core.graph;

import java.util.Collections;
import java.util.Set;

public class GraphScanner<T, R> {
    private final RequestHandler<T, R> requestHandler;

    public GraphScanner(RequestHandler<T, R> requestHandler) {
        this.requestHandler = requestHandler;
    }

    public GraphScannerResult<T> scanAt(R initialRequest, Set<T> previousEntries) {
        GraphScannerContext<T, R> context = new GraphScannerContext<>(previousEntries);

        context.addRequest(initialRequest);

        R request;
        while ((request = context.getRequests().poll()) != null) {
            requestHandler.handle(request, context);
        }

        return context.toResult();
    }

    public GraphScannerResult<T> scanAt(R initialRequest) {
        return scanAt(initialRequest, Collections.emptySet());
    }
}

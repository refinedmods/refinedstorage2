package com.refinedmods.refinedstorage2.core.network.node.graph;

import com.refinedmods.refinedstorage2.core.graph.GraphScannerContext;
import com.refinedmods.refinedstorage2.core.graph.RequestHandler;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.util.Direction;

public class NetworkNodeRequestHandler implements RequestHandler<NetworkNode, NetworkNodeRequest> {
    @Override
    public void handle(NetworkNodeRequest request, GraphScannerContext<NetworkNode, NetworkNodeRequest> context) {
        request.getNetworkNodeRepository().getNode(request.getPos()).ifPresent(node -> {
            if (context.addEntry(node)) {
                for (Direction direction : Direction.values()) {
                    context.addRequest(new NetworkNodeRequest(request.getNetworkNodeRepository(), request.getPos().offset(direction)));
                }
            }
        });
    }
}

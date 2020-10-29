package com.refinedmods.refinedstorage2.core.network.node.graph;

import com.refinedmods.refinedstorage2.core.graph.GraphScannerContext;
import com.refinedmods.refinedstorage2.core.graph.RequestHandler;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import net.minecraft.util.math.Direction;

public class NetworkNodeRequestHandler implements RequestHandler<NetworkNode, NetworkNodeRequest> {
    @Override
    public void handle(NetworkNodeRequest request, GraphScannerContext<NetworkNode, NetworkNodeRequest> context) {
        request.getNetworkNodeAdapter().getNode(request.getPos()).ifPresent(node -> {
            if (context.addEntry(node)) {
                for (Direction direction : Direction.values()) {
                    context.addRequest(new NetworkNodeRequest(request.getNetworkNodeAdapter(), request.getPos().offset(direction)));
                }
            }
        });
    }
}

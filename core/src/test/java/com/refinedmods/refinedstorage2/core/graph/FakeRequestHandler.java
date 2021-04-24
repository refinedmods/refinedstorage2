package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeRequestHandler implements RequestHandler<Position, FakeRequest> {
    private final String requiredType;

    public FakeRequestHandler(String requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    public void handle(FakeRequest request, GraphScannerContext<Position, FakeRequest> context) {
        String type = ((FakeRs2World) request.getWorldAdapter()).getType(request.getPos());

        if (type == null) {
            return;
        }

        if (requiredType.equals(type) && context.addEntry(request.getPos())) {
            for (Direction direction : Direction.values()) {
                context.addRequest(new FakeRequest(request.getWorldAdapter(), request.getPos().offset(direction)));
            }
        }
    }
}

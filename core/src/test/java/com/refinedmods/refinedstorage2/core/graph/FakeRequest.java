package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeRequest {
    private final Rs2World world;
    private final Position pos;

    public FakeRequest(Rs2World world, Position pos) {
        this.world = world;
        this.pos = pos;
    }

    public Rs2World getWorldAdapter() {
        return world;
    }

    public Position getPos() {
        return pos;
    }
}

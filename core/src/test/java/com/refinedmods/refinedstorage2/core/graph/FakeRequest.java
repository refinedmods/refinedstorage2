package com.refinedmods.refinedstorage2.core.graph;

import com.refinedmods.refinedstorage2.core.World;
import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeRequest {
    private final World world;
    private final Position pos;

    public FakeRequest(World world, Position pos) {
        this.world = world;
        this.pos = pos;
    }

    public World getWorldAdapter() {
        return world;
    }

    public Position getPos() {
        return pos;
    }
}

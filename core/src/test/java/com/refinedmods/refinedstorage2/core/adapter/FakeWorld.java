package com.refinedmods.refinedstorage2.core.adapter;

import java.util.HashMap;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.World;
import com.refinedmods.refinedstorage2.core.util.Position;

public class FakeWorld implements World {
    private final Map<Position, String> types = new HashMap<>();

    public Position setType(Position position, String type) {
        types.put(position, type);
        return position;
    }

    public void removeType(Position position) {
        types.remove(position);
    }

    public String getType(Position position) {
        return types.get(position);
    }

    @Override
    public boolean isPowered(Position position) {
        return false;
    }
}

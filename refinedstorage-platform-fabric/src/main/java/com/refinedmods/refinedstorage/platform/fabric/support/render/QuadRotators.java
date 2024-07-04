package com.refinedmods.refinedstorage.platform.fabric.support.render;

import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirection;

import java.util.EnumMap;
import java.util.Map;

public class QuadRotators {
    private final Map<BiDirection, QuadRotator> rotators = new EnumMap<>(BiDirection.class);

    public QuadRotators() {
        for (final BiDirection direction : BiDirection.values()) {
            rotators.put(direction, new QuadRotator(direction));
        }
    }

    public QuadRotator forDirection(final BiDirection direction) {
        return rotators.get(direction);
    }
}

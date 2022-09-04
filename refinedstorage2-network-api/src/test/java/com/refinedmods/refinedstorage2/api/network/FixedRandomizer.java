package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;

import java.util.List;

public class FixedRandomizer implements Randomizer {
    private int index;

    public void setIndex(final int index) {
        this.index = index;
    }

    @Override
    public <T> T choose(final List<T> list) {
        return list.get(index);
    }
}

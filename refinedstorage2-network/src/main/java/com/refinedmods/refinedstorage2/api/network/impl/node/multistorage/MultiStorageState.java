package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import java.util.function.IntFunction;

public class MultiStorageState {
    private final MultiStorageStorageState[] states;

    private MultiStorageState(final MultiStorageStorageState[] states) {
        this.states = states;
    }

    public MultiStorageStorageState getState(final int id) {
        return states[id];
    }

    public MultiStorageStorageState[] getStates() {
        return states;
    }

    public static MultiStorageState of(final int count, final IntFunction<MultiStorageStorageState> provider) {
        final MultiStorageStorageState[] states = new MultiStorageStorageState[count];
        for (int i = 0; i < count; ++i) {
            states[i] = provider.apply(i);
        }
        return new MultiStorageState(states);
    }
}

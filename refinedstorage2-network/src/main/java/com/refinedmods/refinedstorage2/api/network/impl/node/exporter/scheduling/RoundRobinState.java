package com.refinedmods.refinedstorage2.api.network.impl.node.exporter.scheduling;

public class RoundRobinState {
    private int index;

    private final Runnable callback;

    public RoundRobinState(final Runnable callback, final int index) {
        this.index = index;
        this.callback = callback;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        final boolean didChange = this.index != index;
        this.index = index;
        if (didChange) {
            callback.run();
        }
    }
}

package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.storage.Source;

public final class FakeSource implements Source {
    public static final FakeSource INSTANCE = new FakeSource();

    private FakeSource() {
    }

    @Override
    public String getName() {
        return "Fake";
    }
}

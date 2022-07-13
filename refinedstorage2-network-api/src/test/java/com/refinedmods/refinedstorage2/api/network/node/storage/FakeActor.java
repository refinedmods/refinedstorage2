package com.refinedmods.refinedstorage2.api.network.node.storage;

import com.refinedmods.refinedstorage2.api.storage.Actor;

public final class FakeActor implements Actor {
    public static final FakeActor INSTANCE = new FakeActor();

    private FakeActor() {
    }

    @Override
    public String getName() {
        return "Fake";
    }
}

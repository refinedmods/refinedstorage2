package com.refinedmods.refinedstorage.network.test.fake;

import com.refinedmods.refinedstorage.api.storage.Actor;

public final class FakeActor implements Actor {
    public static final FakeActor INSTANCE = new FakeActor();

    private FakeActor() {
    }

    @Override
    public String getName() {
        return "Fake";
    }
}

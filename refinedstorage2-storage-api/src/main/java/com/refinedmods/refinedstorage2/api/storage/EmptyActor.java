package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * An unidentified {@link Actor}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.3")
public final class EmptyActor implements Actor {
    public static final EmptyActor INSTANCE = new EmptyActor();

    private EmptyActor() {
    }

    @Override
    public String getName() {
        return "Empty";
    }
}

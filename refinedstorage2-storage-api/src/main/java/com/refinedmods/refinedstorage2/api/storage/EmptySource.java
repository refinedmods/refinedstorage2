package com.refinedmods.refinedstorage2.api.storage;

import org.apiguardian.api.API;

/**
 * An unidentified {@link Source}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.3")
public final class EmptySource implements Source {
    public static final EmptySource INSTANCE = new EmptySource();

    private EmptySource() {
    }

    @Override
    public String getName() {
        return "Empty";
    }
}

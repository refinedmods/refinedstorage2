package com.refinedmods.refinedstorage.api.network.node;

import org.apiguardian.api.API;

/**
 * A key for a piece of state in {@link MergedNetworkNodeDetails}.
 * Elements are compared by identity, so they should be shared as constants.
 *
 * @param <T> the type of the state
 */
@API(status = API.Status.STABLE, since = "3.3.0")
@FunctionalInterface
public interface MergedNetworkNodeDetailsElement<T> {
    /**
     * @return the initial state of this element, before any node has merged into it
     */
    T create();
}

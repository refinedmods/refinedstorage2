package com.refinedmods.refinedstorage.api.network.node;

import org.apiguardian.api.API;

/**
 * A listener for changes of a {@link NetworkNodeDetailsProvider}.
 */
@API(status = API.Status.STABLE, since = "3.3.0")
@FunctionalInterface
public interface NetworkNodeListener {
    /**
     * Called when something has changed in the node that is being listened to.
     * The type of the event depends on the node that is being listened to.
     *
     * @param event the event
     */
    void notify(Object event);
}

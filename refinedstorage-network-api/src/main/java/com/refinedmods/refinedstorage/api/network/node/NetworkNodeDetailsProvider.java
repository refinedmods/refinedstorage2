package com.refinedmods.refinedstorage.api.network.node;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.3.0")
public interface NetworkNodeDetailsProvider {
    /**
     * @return the type of this node
     */
    NetworkNodeType getType();

    /**
     * Creates a snapshot of the details of this node.
     *
     * @return the details
     */
    NetworkNodeDetails createDetails();

    /**
     * @return the energy usage of this node
     */
    long getEnergyUsage();

    /**
     * Starts listening to changes of this node.
     * The listener is notified with events that are specific to the node that is being listened to.
     *
     * @param listener the listener
     */
    void addListener(NetworkNodeListener listener);

    /**
     * Stops listening to changes of this node.
     * Does nothing if the listener isn't listening to this node.
     *
     * @param listener the listener
     */
    void removeListener(NetworkNodeListener listener);
}

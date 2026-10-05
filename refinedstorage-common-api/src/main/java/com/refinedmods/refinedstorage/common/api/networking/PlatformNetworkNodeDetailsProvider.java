package com.refinedmods.refinedstorage.common.api.networking;

import com.refinedmods.refinedstorage.api.network.node.MergedNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import org.apiguardian.api.API;

/**
 * Implemented by block entities to add platform specific details to the details of their network node.
 */
@API(status = API.Status.STABLE, since = "3.3.0")
@FunctionalInterface
public interface PlatformNetworkNodeDetailsProvider {
    /**
     * @param details the details of the network node
     * @return the details, possibly wrapped with additional platform specific details
     */
    NetworkNodeDetails wrap(NetworkNodeDetails details);

    /**
     * Merges platform specific details into details that are shared with other nodes.
     * The details of the network node itself have already been merged.
     *
     * @param details the merged details
     */
    default void mergeDetails(final MergedNetworkNodeDetails details) {
        // no op
    }
}

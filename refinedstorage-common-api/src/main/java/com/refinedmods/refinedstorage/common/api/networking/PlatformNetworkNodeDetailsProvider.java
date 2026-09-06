package com.refinedmods.refinedstorage.common.api.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.3.0")
@FunctionalInterface
public interface PlatformNetworkNodeDetailsProvider {
    NetworkNodeDetails wrap(NetworkNodeDetails details);
}

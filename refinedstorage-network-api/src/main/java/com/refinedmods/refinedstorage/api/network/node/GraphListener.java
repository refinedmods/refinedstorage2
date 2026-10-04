package com.refinedmods.refinedstorage.api.network.node;

import com.refinedmods.refinedstorage.api.network.node.container.NetworkNodeContainer;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.3.0")
public interface GraphListener {
    void onContainerAdded(NetworkNodeContainer container);

    void onContainerRemoved(NetworkNodeContainer container);
}

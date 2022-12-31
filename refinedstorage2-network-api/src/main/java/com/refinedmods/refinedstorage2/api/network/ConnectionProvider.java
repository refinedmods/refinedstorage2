package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.List;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface ConnectionProvider {
    Connections findConnections(NetworkNodeContainer pivot, Set<NetworkNodeContainer> existingConnections);

    List<NetworkNodeContainer> sort(Set<NetworkNodeContainer> containers);
}

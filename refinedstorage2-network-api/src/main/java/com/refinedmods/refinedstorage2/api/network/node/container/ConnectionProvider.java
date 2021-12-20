package com.refinedmods.refinedstorage2.api.network.node.container;

import java.util.List;
import java.util.Set;

public interface ConnectionProvider {
    Connections findConnections(NetworkNodeContainer pivot, Set<NetworkNodeContainer> existingConnections);

    List<NetworkNodeContainer> sort(Set<NetworkNodeContainer> containers);
}

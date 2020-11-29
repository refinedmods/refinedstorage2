package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;

import java.util.Set;
import java.util.UUID;

public interface Network {
    UUID getId();

    Set<NetworkNodeReference> getNodeReferences();

    void onNodesChanged();
}

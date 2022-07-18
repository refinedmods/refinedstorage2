package com.refinedmods.refinedstorage2.platform.api.network.node;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import net.minecraft.core.Direction;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public interface PlatformNetworkNodeContainer extends NetworkNodeContainer {
    boolean canAcceptOutgoingConnection(Direction direction);

    boolean canAcceptIncomingConnection(Direction direction);
}

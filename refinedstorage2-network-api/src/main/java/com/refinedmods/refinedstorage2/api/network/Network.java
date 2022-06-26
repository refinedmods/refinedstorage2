package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.component.ComponentAccessor;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public interface Network extends ComponentAccessor<NetworkComponent> {
    void addContainer(NetworkNodeContainer container);

    void removeContainer(NetworkNodeContainer container);

    void remove();

    void split(Set<Network> networks);

    void merge(Network network);
}

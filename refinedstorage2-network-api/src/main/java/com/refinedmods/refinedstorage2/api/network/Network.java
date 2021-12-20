package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;

public interface Network {
    <T extends NetworkComponent> T getComponent(Class<T> componentClass);

    void addContainer(NetworkNodeContainer container);

    void removeContainer(NetworkNodeContainer container);

    void remove();

    void split(Set<Network> networks);

    void merge(Network network);
}

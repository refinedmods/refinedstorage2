package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

import java.util.Set;

public interface Network {
    <T extends NetworkComponent> T getComponent(Class<T> componentClass);

    void addHost(NetworkNodeHost<?> host);

    void removeHost(NetworkNodeHost<?> host);

    void remove();

    void split(Set<Network> networks);

    void merge(Network network);
}

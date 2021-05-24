package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

public interface Network {
    <T extends NetworkComponent> T getComponent(Class<T> componentClass);

    void addHost(NetworkNodeHost<?> host);

    void removeHost(NetworkNodeHost<?> host);
}

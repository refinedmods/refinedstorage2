package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

public interface NetworkComponent {
    void onHostAdded(NetworkNodeHost<?> host);

    void onHostRemoved(NetworkNodeHost<?> host);
}

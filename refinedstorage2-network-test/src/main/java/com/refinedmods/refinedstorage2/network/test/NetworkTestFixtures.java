package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.component.EnergyNetworkComponentImpl;
import com.refinedmods.refinedstorage2.api.network.impl.component.GraphNetworkComponentImpl;
import com.refinedmods.refinedstorage2.api.network.impl.component.StorageNetworkComponentImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListImpl;

public final class NetworkTestFixtures {
    public static final ComponentMapFactory<NetworkComponent, Network> NETWORK_COMPONENT_MAP_FACTORY =
        new ComponentMapFactory<>();

    static {
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            EnergyNetworkComponent.class,
            network -> new EnergyNetworkComponentImpl()
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            GraphNetworkComponent.class,
            GraphNetworkComponentImpl::new
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            StorageNetworkComponent.class,
            network -> new StorageNetworkComponentImpl(new ResourceListImpl())
        );
    }

    private NetworkTestFixtures() {
    }
}

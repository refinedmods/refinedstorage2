package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistryImpl;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.component.EnergyNetworkComponentImpl;
import com.refinedmods.refinedstorage2.api.network.impl.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.component.StorageNetworkComponentImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

public final class NetworkTestFixtures {
    public static final StorageChannelType<String> STORAGE_CHANNEL_TYPE = StorageChannelImpl::new;
    public static final OrderedRegistry<String, StorageChannelType<?>> STORAGE_CHANNEL_TYPE_REGISTRY =
        new OrderedRegistryImpl<>("default", STORAGE_CHANNEL_TYPE);
    public static final ComponentMapFactory<NetworkComponent, Network> NETWORK_COMPONENT_MAP_FACTORY =
        new ComponentMapFactory<>();

    static {
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            EnergyNetworkComponent.class,
            network -> new EnergyNetworkComponentImpl()
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            GraphNetworkComponent.class,
            GraphNetworkComponent::new
        );
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(
            StorageNetworkComponent.class,
            network -> new StorageNetworkComponentImpl(STORAGE_CHANNEL_TYPE_REGISTRY)
        );
    }

    private NetworkTestFixtures() {
    }
}

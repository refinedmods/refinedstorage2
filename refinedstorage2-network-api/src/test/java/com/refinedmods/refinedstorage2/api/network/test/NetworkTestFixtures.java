package com.refinedmods.refinedstorage2.api.network.test;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistryImpl;

public final class NetworkTestFixtures {
    public static final StorageChannelType<String> STORAGE_CHANNEL_TYPE = StorageChannelImpl::new;
    public static final StorageChannelTypeRegistry STORAGE_CHANNEL_TYPE_REGISTRY = new StorageChannelTypeRegistryImpl();
    public static final ComponentMapFactory<NetworkComponent, Network> NETWORK_COMPONENT_MAP_FACTORY = new ComponentMapFactory<>();

    static {
        STORAGE_CHANNEL_TYPE_REGISTRY.addType(STORAGE_CHANNEL_TYPE);
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(GraphNetworkComponent.class, GraphNetworkComponent::new);
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(StorageNetworkComponent.class, network -> new StorageNetworkComponent(STORAGE_CHANNEL_TYPE_REGISTRY));
    }

    private NetworkTestFixtures() {
    }
}

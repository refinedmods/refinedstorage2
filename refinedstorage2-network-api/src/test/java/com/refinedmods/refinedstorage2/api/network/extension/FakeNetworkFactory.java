package com.refinedmods.refinedstorage2.api.network.extension;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkImpl;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistryImpl;

public class FakeNetworkFactory {
    public final ComponentMapFactory<NetworkComponent, Network> componentMapFactory = new ComponentMapFactory<>();

    public FakeNetworkFactory() {
        StorageChannelTypeRegistry storageChannelTypeRegistry = new StorageChannelTypeRegistryImpl();
        storageChannelTypeRegistry.addType(StorageChannelTypes.FAKE);

        componentMapFactory.addFactory(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        componentMapFactory.addFactory(GraphNetworkComponent.class, GraphNetworkComponent::new);
        componentMapFactory.addFactory(StorageNetworkComponent.class, network -> new StorageNetworkComponent(storageChannelTypeRegistry));
    }

    public Network create() {
        return new NetworkImpl(componentMapFactory);
    }
}

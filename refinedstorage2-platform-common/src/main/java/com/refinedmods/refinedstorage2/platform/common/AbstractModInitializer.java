package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacadeProxy;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageTypeRegistry;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractModInitializer {
    protected void initializePlatform(Platform platform) {
        ((PlatformProxy) Platform.INSTANCE).setPlatform(platform);
    }

    protected void initializePlatformApiFacade() {
        ((Rs2PlatformApiFacadeProxy) Rs2PlatformApiFacade.INSTANCE).setFacade(new Rs2PlatformApiFacadeImpl());
    }

    protected void registerDiskTypes() {
        StorageTypeRegistry.INSTANCE.addType(createIdentifier("item_disk"), ItemStorageType.INSTANCE);
        StorageTypeRegistry.INSTANCE.addType(createIdentifier("fluid_disk"), FluidStorageType.INSTANCE);
    }

    protected void registerStorageChannelTypes() {
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.ITEM);
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.FLUID);
    }

    protected void registerNetworkComponents() {
        Rs2PlatformApiFacade.INSTANCE.getNetworkComponentMapFactory().addFactory(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        Rs2PlatformApiFacade.INSTANCE.getNetworkComponentMapFactory().addFactory(GraphNetworkComponent.class, GraphNetworkComponent::new);
        Rs2PlatformApiFacade.INSTANCE.getNetworkComponentMapFactory().addFactory(StorageNetworkComponent.class, network -> new StorageNetworkComponent(StorageChannelTypeRegistry.INSTANCE));
    }

    protected void registerResourceTypes() {
        Rs2PlatformApiFacade.INSTANCE.getResourceTypeRegistry().register(FluidResourceType.INSTANCE);
    }
}

package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.PlatformApiProxy;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractModInitializer {
    protected void initializePlatform(Platform platform) {
        ((PlatformProxy) Platform.INSTANCE).setPlatform(platform);
    }

    protected void initializePlatformApiFacade() {
        ((PlatformApiProxy) PlatformApi.INSTANCE).setDelegate(new PlatformApiImpl());
    }

    protected void registerDiskTypes() {
        PlatformApi.INSTANCE.getStorageTypeRegistry().addType(createIdentifier("item_disk"), ItemStorageType.INSTANCE);
        PlatformApi.INSTANCE.getStorageTypeRegistry().addType(createIdentifier("fluid_disk"), FluidStorageType.INSTANCE);
    }

    protected void registerStorageChannelTypes() {
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.ITEM);
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.FLUID);
    }

    protected void registerNetworkComponents() {
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(GraphNetworkComponent.class, GraphNetworkComponent::new);
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(StorageNetworkComponent.class, network -> new StorageNetworkComponent(StorageChannelTypeRegistry.INSTANCE));
    }

    protected void registerResourceTypes() {
        PlatformApi.INSTANCE.getResourceTypeRegistry().register(FluidResourceType.INSTANCE);
    }
}

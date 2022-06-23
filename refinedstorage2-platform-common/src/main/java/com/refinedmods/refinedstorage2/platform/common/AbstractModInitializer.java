package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.PlatformApiProxy;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractModInitializer {
    protected void initializePlatform(Platform platform) {
        ((PlatformProxy) Platform.INSTANCE).setPlatform(platform);
    }

    protected void initializePlatformApi() {
        ((PlatformApiProxy) PlatformApi.INSTANCE).setDelegate(new PlatformApiImpl());
    }

    protected void registerDiskTypes() {
        PlatformApi.INSTANCE.getStorageTypeRegistry().addType(createIdentifier("item_disk"), ItemStorageType.INSTANCE);
        PlatformApi.INSTANCE.getStorageTypeRegistry().addType(createIdentifier("fluid_disk"), FluidStorageType.INSTANCE);
    }

    protected void registerStorageChannelTypes() {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().addType(StorageChannelTypes.ITEM);
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().addType(StorageChannelTypes.FLUID);
    }

    protected void registerNetworkComponents() {
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(GraphNetworkComponent.class, GraphNetworkComponent::new);
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(StorageNetworkComponent.class, network -> new StorageNetworkComponent(PlatformApi.INSTANCE.getStorageChannelTypeRegistry()));
    }

    protected void registerAdditionalResourceTypes() {
        PlatformApi.INSTANCE.getResourceTypeRegistry().register(createIdentifier("fluid_resource_type"), FluidResourceType.INSTANCE);
    }
}

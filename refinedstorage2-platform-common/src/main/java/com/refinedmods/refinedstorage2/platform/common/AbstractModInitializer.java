package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.component.EnergyNetworkComponentImpl;
import com.refinedmods.refinedstorage2.api.network.impl.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.component.StorageNetworkComponentImpl;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.PlatformApiProxy;
import com.refinedmods.refinedstorage2.platform.common.block.entity.destructor.BlockBreakDestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.block.entity.destructor.FluidBreakDestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.block.entity.destructor.ItemPickupDestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid.FluidFilteredResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractModInitializer {
    private static final String FLUID_REGISTRY_KEY = "fluid";

    protected void initializePlatform(final Platform platform) {
        ((PlatformProxy) Platform.INSTANCE).setPlatform(platform);
    }

    protected void initializePlatformApi() {
        ((PlatformApiProxy) PlatformApi.INSTANCE).setDelegate(new PlatformApiImpl());
    }

    protected void registerAdditionalStorageTypes() {
        PlatformApi.INSTANCE.getStorageTypeRegistry().register(
            createIdentifier(FLUID_REGISTRY_KEY),
            FluidStorageType.INSTANCE
        );
    }

    protected void registerAdditionalStorageChannelTypes() {
        PlatformApi.INSTANCE.getStorageChannelTypeRegistry().register(
            createIdentifier(FLUID_REGISTRY_KEY),
            StorageChannelTypes.FLUID
        );
    }

    protected void registerAdditionalFilteredResourceFactories() {
        PlatformApi.INSTANCE.addFilteredResourceFactory(new FluidFilteredResourceFactory());
    }

    protected void registerDestructorStrategyFactories() {
        PlatformApi.INSTANCE.addDestructorStrategyFactory(new BlockBreakDestructorStrategyFactory());
        PlatformApi.INSTANCE.addDestructorStrategyFactory(new FluidBreakDestructorStrategyFactory());
        PlatformApi.INSTANCE.addDestructorStrategyFactory(new ItemPickupDestructorStrategyFactory());
    }

    protected void registerNetworkComponents() {
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            EnergyNetworkComponent.class,
            network -> new EnergyNetworkComponentImpl()
        );
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            GraphNetworkComponent.class,
            GraphNetworkComponent::new
        );
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            StorageNetworkComponent.class,
            network -> new StorageNetworkComponentImpl(
                PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getAll()
            )
        );
    }

    protected void addApplicableUpgrades() {
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.IMPORTER,
            Items.INSTANCE::getSpeedUpgrade,
            4
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.IMPORTER,
            Items.INSTANCE::getStackUpgrade,
            1
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.EXPORTER,
            Items.INSTANCE::getSpeedUpgrade,
            4
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.EXPORTER,
            Items.INSTANCE::getStackUpgrade,
            1
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.DESTRUCTOR,
            Items.INSTANCE::getSpeedUpgrade,
            4
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.DESTRUCTOR,
            Items.INSTANCE::getFortune1Upgrade,
            1
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.DESTRUCTOR,
            Items.INSTANCE::getFortune2Upgrade,
            1
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.DESTRUCTOR,
            Items.INSTANCE::getFortune3Upgrade,
            1
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.DESTRUCTOR,
            Items.INSTANCE::getSilkTouchUpgrade,
            1
        );
    }
}

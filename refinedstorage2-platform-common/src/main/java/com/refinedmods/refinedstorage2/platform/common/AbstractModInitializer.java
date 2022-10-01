package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.util.Randomizer;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.exporter.RandomExporterSchedulingMode;
import com.refinedmods.refinedstorage2.api.network.node.exporter.RoundRobinExporterSchedulingMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.PlatformApiProxy;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.PlatformExporterSchedulingModeImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid.FluidResourceType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

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

    protected void registerNetworkComponents() {
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            EnergyNetworkComponent.class,
            network -> new EnergyNetworkComponent()
        );
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            GraphNetworkComponent.class,
            GraphNetworkComponent::new
        );
        PlatformApi.INSTANCE.getNetworkComponentMapFactory().addFactory(
            StorageNetworkComponent.class,
            network -> new StorageNetworkComponent(PlatformApi.INSTANCE.getStorageChannelTypeRegistry())
        );
    }

    protected void registerAdditionalResourceTypes() {
        PlatformApi.INSTANCE.getResourceTypeRegistry().register(
            createIdentifier(FLUID_REGISTRY_KEY),
            FluidResourceType.INSTANCE
        );
    }

    protected void registerAdditionalExporterSchedulingModes() {
        PlatformApi.INSTANCE.getExporterSchedulingModeRegistry().register(
            createIdentifier("round_robin"),
            new PlatformExporterSchedulingModeImpl(
                new RoundRobinExporterSchedulingMode(),
                Component.translatable("gui.refinedstorage2.exporter.scheduling_mode.round_robin")
            )
        );
        final Random random = new Random();
        PlatformApi.INSTANCE.getExporterSchedulingModeRegistry().register(
            createIdentifier("random"),
            new PlatformExporterSchedulingModeImpl(
                new RandomExporterSchedulingMode(new Randomizer() {
                    @Override
                    public <T> void shuffle(final List<T> list) {
                        Collections.shuffle(list, random);
                    }
                }),
                Component.translatable("gui.refinedstorage2.exporter.scheduling_mode.random")
            )
        );
    }

    protected void addApplicableUpgrades(final Supplier<Item> speedUpgrade,
                                         final Supplier<Item> stackUpgrade) {
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.IMPORTER,
            speedUpgrade,
            4
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.IMPORTER,
            stackUpgrade,
            1
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.EXPORTER,
            speedUpgrade,
            4
        );
        PlatformApi.INSTANCE.getUpgradeRegistry().addApplicableUpgrade(
            UpgradeDestinations.EXPORTER,
            stackUpgrade,
            1
        );
    }
}

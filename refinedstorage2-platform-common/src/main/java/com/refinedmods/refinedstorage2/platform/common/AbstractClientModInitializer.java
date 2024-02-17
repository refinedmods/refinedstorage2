package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.constructordestructor.ConstructorScreen;
import com.refinedmods.refinedstorage2.platform.common.constructordestructor.DestructorScreen;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.common.detector.DetectorScreen;
import com.refinedmods.refinedstorage2.platform.common.exporter.ExporterScreen;
import com.refinedmods.refinedstorage2.platform.common.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.grid.WirelessGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage2.platform.common.grid.screen.GridScreen;
import com.refinedmods.refinedstorage2.platform.common.grid.screen.hint.FluidGridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfaceScreen;
import com.refinedmods.refinedstorage2.platform.common.importer.ImporterScreen;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterScreen;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.common.storage.externalstorage.ExternalStorageScreen;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.PortableGridScreen;
import com.refinedmods.refinedstorage2.platform.common.storage.storageblock.FluidStorageBlockScreen;
import com.refinedmods.refinedstorage2.platform.common.storage.storageblock.ItemStorageBlockScreen;
import com.refinedmods.refinedstorage2.platform.common.storagemonitor.StorageMonitorScreen;
import com.refinedmods.refinedstorage2.platform.common.support.resource.FluidResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResourceRendering;
import com.refinedmods.refinedstorage2.platform.common.upgrade.RegulatorUpgradeScreen;
import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterScreen;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractClientModInitializer {
    protected static void registerScreens(final ScreenRegistration registration) {
        registration.register(Menus.INSTANCE.getDiskDrive(), DiskDriveScreen::new);
        registration.register(Menus.INSTANCE.getGrid(), GridScreen<GridContainerMenu>::new);
        registration.register(Menus.INSTANCE.getCraftingGrid(), CraftingGridScreen::new);
        registration.register(Menus.INSTANCE.getWirelessGrid(), GridScreen<WirelessGridContainerMenu>::new);
        registration.register(Menus.INSTANCE.getController(), ControllerScreen::new);
        registration.register(Menus.INSTANCE.getItemStorage(), ItemStorageBlockScreen::new);
        registration.register(Menus.INSTANCE.getFluidStorage(), FluidStorageBlockScreen::new);
        registration.register(Menus.INSTANCE.getImporter(), ImporterScreen::new);
        registration.register(Menus.INSTANCE.getExporter(), ExporterScreen::new);
        registration.register(Menus.INSTANCE.getInterface(), InterfaceScreen::new);
        registration.register(Menus.INSTANCE.getExternalStorage(), ExternalStorageScreen::new);
        registration.register(Menus.INSTANCE.getDetector(), DetectorScreen::new);
        registration.register(Menus.INSTANCE.getDestructor(), DestructorScreen::new);
        registration.register(Menus.INSTANCE.getConstructor(), ConstructorScreen::new);
        registration.register(Menus.INSTANCE.getRegulatorUpgrade(), RegulatorUpgradeScreen::new);
        registration.register(Menus.INSTANCE.getWirelessTransmitter(), WirelessTransmitterScreen::new);
        registration.register(Menus.INSTANCE.getStorageMonitor(), StorageMonitorScreen::new);
        registration.register(Menus.INSTANCE.getNetworkTransmitter(), NetworkTransmitterScreen::new);
        registration.register(Menus.INSTANCE.getPortableGridBlock(), PortableGridScreen::new);
        registration.register(Menus.INSTANCE.getPortableGridItem(), PortableGridScreen::new);
    }

    protected static void registerAlternativeGridHints() {
        PlatformApi.INSTANCE.addAlternativeGridInsertionHint(new FluidGridInsertionHint());
    }

    protected static void registerResourceRendering() {
        PlatformApi.INSTANCE.registerResourceRendering(ItemResource.class, new ItemResourceRendering());
        PlatformApi.INSTANCE.registerResourceRendering(FluidResource.class, new FluidResourceRendering());
    }

    protected static void handleInputEvents() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final KeyMapping openWirelessGrid = KeyMappings.INSTANCE.getOpenWirelessGrid();
        while (openWirelessGrid != null && openWirelessGrid.consumeClick()) {
            PlatformApi.INSTANCE.useNetworkBoundItem(
                player,
                Items.INSTANCE.getWirelessGrid(),
                Items.INSTANCE.getCreativeWirelessGrid()
            );
        }
    }

    protected static void registerDiskModels() {
        final ResourceLocation diskModel = createIdentifier("block/disk/disk");
        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            PlatformApi.INSTANCE.getStorageContainerItemHelper().registerDiskModel(
                Items.INSTANCE.getItemStorageDisk(variant),
                diskModel
            );
        }

        final ResourceLocation fluidDiskModel = createIdentifier("block/disk/fluid_disk");
        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            PlatformApi.INSTANCE.getStorageContainerItemHelper().registerDiskModel(
                Items.INSTANCE.getFluidStorageDisk(variant),
                fluidDiskModel
            );
        }
    }

    @FunctionalInterface
    public interface ScreenRegistration {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type,
                                                                                          ScreenConstructor<M, U>
                                                                                              factory);
    }

    @FunctionalInterface
    public interface ScreenConstructor<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T menu, Inventory inventory, Component title);
    }
}

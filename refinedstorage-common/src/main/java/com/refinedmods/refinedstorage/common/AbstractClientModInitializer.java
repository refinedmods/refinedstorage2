package com.refinedmods.refinedstorage.common;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApiProxy;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterScreen;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerScreen;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorScreen;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridScreen;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewContainerMenu;
import com.refinedmods.refinedstorage.common.autocrafting.preview.AutocraftingPreviewScreen;
import com.refinedmods.refinedstorage.common.constructordestructor.ConstructorScreen;
import com.refinedmods.refinedstorage.common.constructordestructor.DestructorScreen;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.KeyMappings;
import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.controller.ControllerScreen;
import com.refinedmods.refinedstorage.common.detector.DetectorScreen;
import com.refinedmods.refinedstorage.common.exporter.ExporterScreen;
import com.refinedmods.refinedstorage.common.grid.GridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.WirelessGridContainerMenu;
import com.refinedmods.refinedstorage.common.grid.screen.CraftingGridScreen;
import com.refinedmods.refinedstorage.common.grid.screen.GridScreen;
import com.refinedmods.refinedstorage.common.grid.screen.hint.FluidGridInsertionHint;
import com.refinedmods.refinedstorage.common.iface.InterfaceScreen;
import com.refinedmods.refinedstorage.common.importer.ImporterScreen;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterScreen;
import com.refinedmods.refinedstorage.common.networking.RelayScreen;
import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterScreen;
import com.refinedmods.refinedstorage.common.security.FallbackSecurityCardScreen;
import com.refinedmods.refinedstorage.common.security.SecurityCardScreen;
import com.refinedmods.refinedstorage.common.security.SecurityManagerScreen;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveScreen;
import com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceScreen;
import com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageScreen;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridScreen;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorScreen;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.FluidResourceRendering;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResourceRendering;
import com.refinedmods.refinedstorage.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.ResourceClientTooltipComponent;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeScreen;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinationClientTooltipComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public abstract class AbstractClientModInitializer {
    public static void initializeClientPlatformApi() {
        ((RefinedStorageClientApiProxy) RefinedStorageClientApi.INSTANCE).setDelegate(
            new RefinedStorageClientApiImpl()
        );
    }

    protected static void registerScreens(final ScreenRegistration registration) {
        registration.register(Menus.INSTANCE.getDiskDrive(), DiskDriveScreen::new);
        registration.register(Menus.INSTANCE.getGrid(), GridScreen<GridContainerMenu>::new);
        registration.register(Menus.INSTANCE.getCraftingGrid(), CraftingGridScreen::new);
        registration.register(Menus.INSTANCE.getPatternGrid(), PatternGridScreen::new);
        registration.register(Menus.INSTANCE.getWirelessGrid(), GridScreen<WirelessGridContainerMenu>::new);
        registration.register(Menus.INSTANCE.getController(), ControllerScreen::new);
        registration.register(Menus.INSTANCE.getItemStorage(),
            new ScreenConstructor<AbstractContainerMenu, AbstractContainerScreen<AbstractContainerMenu>>() {
                @Override
                public AbstractContainerScreen<AbstractContainerMenu> create(final AbstractContainerMenu menu,
                                                                             final Inventory inventory,
                                                                             final Component title) {
                    return RefinedStorageClientApi.INSTANCE.createStorageBlockScreen(menu, inventory, title,
                        ItemResource.class);
                }
            });
        registration.register(Menus.INSTANCE.getFluidStorage(),
            new ScreenConstructor<AbstractContainerMenu, AbstractContainerScreen<AbstractContainerMenu>>() {
                @Override
                public AbstractContainerScreen<AbstractContainerMenu> create(final AbstractContainerMenu menu,
                                                                             final Inventory inventory,
                                                                             final Component title) {
                    return RefinedStorageClientApi.INSTANCE.createStorageBlockScreen(menu, inventory, title,
                        FluidResource.class);
                }
            });
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
        registration.register(Menus.INSTANCE.getSecurityCard(), SecurityCardScreen::new);
        registration.register(Menus.INSTANCE.getFallbackSecurityCard(), FallbackSecurityCardScreen::new);
        registration.register(Menus.INSTANCE.getSecurityManager(), SecurityManagerScreen::new);
        registration.register(Menus.INSTANCE.getRelay(), RelayScreen::new);
        registration.register(Menus.INSTANCE.getDiskInterface(), DiskInterfaceScreen::new);
        registration.register(Menus.INSTANCE.getAutocrafter(), AutocrafterScreen::new);
        registration.register(Menus.INSTANCE.getAutocraftingStorageMonitor(),
            new ScreenConstructor<AutocraftingPreviewContainerMenu, AutocraftingPreviewScreen>() {
                @Override
                public AutocraftingPreviewScreen create(final AutocraftingPreviewContainerMenu menu,
                                                        final Inventory inventory,
                                                        final Component title) {
                    return new AutocraftingPreviewScreen(menu, inventory);
                }
            });
        registration.register(Menus.INSTANCE.getAutocrafterManager(), AutocrafterManagerScreen::new);
        registration.register(Menus.INSTANCE.getAutocraftingMonitor(), AutocraftingMonitorScreen::new);
        registration.register(Menus.INSTANCE.getWirelessAutocraftingMonitor(), AutocraftingMonitorScreen::new);
    }

    protected static void registerAlternativeGridHints() {
        RefinedStorageClientApi.INSTANCE.addAlternativeGridInsertionHint(new FluidGridInsertionHint());
    }

    protected static void registerResourceRendering() {
        RefinedStorageClientApi.INSTANCE.registerResourceRendering(ItemResource.class, ItemResourceRendering.INSTANCE);
        RefinedStorageClientApi.INSTANCE.registerResourceRendering(FluidResource.class, new FluidResourceRendering(
            Platform.INSTANCE.getBucketAmount()
        ));
    }

    protected static void handleInputEvents() {
        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        final KeyMapping openWirelessGrid = KeyMappings.INSTANCE.getOpenWirelessGrid();
        while (openWirelessGrid != null && openWirelessGrid.consumeClick()) {
            RefinedStorageApi.INSTANCE.useSlotReferencedItem(
                player,
                Items.INSTANCE.getWirelessGrid(),
                Items.INSTANCE.getCreativeWirelessGrid()
            );
        }
        final KeyMapping openPortableGrid = KeyMappings.INSTANCE.getOpenPortableGrid();
        while (openPortableGrid != null && openPortableGrid.consumeClick()) {
            RefinedStorageApi.INSTANCE.useSlotReferencedItem(
                player,
                Items.INSTANCE.getPortableGrid(),
                Items.INSTANCE.getCreativePortableGrid()
            );
        }
        final KeyMapping openWirelessAutocraftingMonitor = KeyMappings.INSTANCE.getOpenWirelessAutocraftingMonitor();
        while (openWirelessAutocraftingMonitor != null && openWirelessAutocraftingMonitor.consumeClick()) {
            RefinedStorageApi.INSTANCE.useSlotReferencedItem(
                player,
                Items.INSTANCE.getWirelessAutocraftingMonitor(),
                Items.INSTANCE.getCreativeWirelessAutocraftingMonitor()
            );
        }
    }

    protected static void registerDiskModels() {
        final Identifier diskModel = createIdentifier("block/disk/disk");
        for (final ItemStorageVariant variant : ItemStorageVariant.values()) {
            final Item item = Items.INSTANCE.getItemStorageDisk(variant);
            RefinedStorageClientApi.INSTANCE.registerDiskModel(item, diskModel);
        }
        final Identifier fluidDiskModel = createIdentifier("block/disk/fluid_disk");
        for (final FluidStorageVariant variant : FluidStorageVariant.values()) {
            final Item item = Items.INSTANCE.getFluidStorageDisk(variant);
            RefinedStorageClientApi.INSTANCE.registerDiskModel(item, fluidDiskModel);
        }
    }

    protected static CompositeClientTooltipComponent createRegulatorUpgradeClientTooltipComponent(
        final Set<UpgradeMapping> destinations,
        @Nullable final ResourceAmount configuredResource,
        final Component help
    ) {
        final List<ClientTooltipComponent> components = new ArrayList<>();
        if (configuredResource != null) {
            components.add(new ResourceClientTooltipComponent(configuredResource));
        } else {
            components.add(new UpgradeDestinationClientTooltipComponent(destinations));
        }
        components.add(HelpClientTooltipComponent.create(help));
        return new CompositeClientTooltipComponent(components);
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

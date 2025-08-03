package com.refinedmods.refinedstorage.neoforge;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternTooltipCache;
import com.refinedmods.refinedstorage.common.configurationcard.ConfigurationCardItemPropertyFunction;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.KeyMappings;
import com.refinedmods.refinedstorage.common.controller.ControllerItemPropertyFunction;
import com.refinedmods.refinedstorage.common.networking.NetworkCardItemPropertyFunction;
import com.refinedmods.refinedstorage.common.security.SecurityCardItemPropertyFunction;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkItemPropertyFunction;
import com.refinedmods.refinedstorage.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage.neoforge.autocrafting.PatternGeometryLoader;
import com.refinedmods.refinedstorage.neoforge.networking.CableGeometryLoader;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveGeometryLoader;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.DiskInterfaceBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.DiskInterfaceGeometryLoader;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.PortableGridBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.PortableGridGeometryLoader;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.content.ContentIds.CABLE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.DISK_INTERFACE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.PATTERN;
import static com.refinedmods.refinedstorage.common.content.ContentIds.PORTABLE_GRID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class ClientModInitializer extends AbstractClientModInitializer {
    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onMouseInput);
        e.enqueueWork(ClientModInitializer::registerModelPredicates);
        e.enqueueWork(ClientModInitializer::registerItemProperties);
        registerBlockEntityRenderer();
        registerResourceRendering();
        registerAlternativeGridHints();
        registerDiskModels();
    }

    @SubscribeEvent
    public static void onKeyInput(final InputEvent.Key e) {
        handleInputEvents();
    }

    @SubscribeEvent
    public static void onMouseInput(final InputEvent.MouseButton.Pre e) {
        handleInputEvents();
    }

    private static void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemProperties.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerItemPropertyFunction()
        ));
    }

    @SubscribeEvent
    public static void onRegisterCustomModels(final ModelEvent.RegisterGeometryLoaders e) {
        registerDiskModels();
        e.register(PATTERN, new PatternGeometryLoader());
        e.register(DISK_DRIVE, new DiskDriveGeometryLoader());
        e.register(PORTABLE_GRID, new PortableGridGeometryLoader());
        e.register(DISK_INTERFACE, new DiskInterfaceGeometryLoader());
        e.register(CABLE, new CableGeometryLoader());
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(final RegisterMenuScreensEvent e) {
        registerScreens(new ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
                final MenuType<? extends M> type,
                final ScreenConstructor<M, U> factory
            ) {
                e.register(type, factory::create);
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent e) {
        final KeyMapping focusSearchBarKeyBinding = new KeyMapping(
            ContentNames.FOCUS_SEARCH_BAR_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(focusSearchBarKeyBinding);
        KeyMappings.INSTANCE.setFocusSearchBar(focusSearchBarKeyBinding);

        final KeyMapping clearCraftingGridMatrixToNetwork = new KeyMapping(
            ContentNames.CLEAR_CRAFTING_MATRIX_TO_NETWORK_TRANSLATION_KEY,
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(clearCraftingGridMatrixToNetwork);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToNetwork(clearCraftingGridMatrixToNetwork);

        final KeyMapping clearCraftingGridMatrixToInventory = new KeyMapping(
            ContentNames.CLEAR_CRAFTING_MATRIX_TO_INVENTORY_TRANSLATION_KEY,
            InputConstants.UNKNOWN.getValue(),
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(clearCraftingGridMatrixToInventory);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToInventory(clearCraftingGridMatrixToInventory);

        final KeyMapping openWirelessGrid = new KeyMapping(
            ContentNames.OPEN_WIRELESS_GRID_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(openWirelessGrid);
        KeyMappings.INSTANCE.setOpenWirelessGrid(openWirelessGrid);

        final KeyMapping openPortableGrid = new KeyMapping(
            ContentNames.OPEN_PORTABLE_GRID_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(openPortableGrid);
        KeyMappings.INSTANCE.setOpenPortableGrid(openPortableGrid);

        final KeyMapping openWirelessAutocraftingMonitor = new KeyMapping(
            ContentNames.OPEN_WIRELESS_AUTOCRAFTING_MONITOR_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(openWirelessAutocraftingMonitor);
        KeyMappings.INSTANCE.setOpenWirelessAutocraftingMonitor(openWirelessAutocraftingMonitor);
    }

    private static void registerBlockEntityRenderer() {
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskDrive(),
            ctx -> new DiskDriveBlockEntityRendererImpl<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getStorageMonitor(),
            ctx -> new StorageMonitorBlockEntityRenderer()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getPortableGrid(),
            ctx -> new PortableGridBlockEntityRendererImpl<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getCreativePortableGrid(),
            ctx -> new PortableGridBlockEntityRendererImpl<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskInterface(),
            ctx -> new DiskInterfaceBlockEntityRendererImpl<>()
        );
    }

    @SubscribeEvent
    public static void onRegisterTooltipFactories(final RegisterClientTooltipComponentFactoriesEvent e) {
        e.register(
            AbstractUpgradeItem.UpgradeDestinationTooltipComponent.class,
            component -> new CompositeClientTooltipComponent(List.of(
                new UpgradeDestinationClientTooltipComponent(component.destinations()),
                HelpClientTooltipComponent.create(component.helpText())
            ))
        );
        e.register(
            HelpTooltipComponent.class,
            component -> HelpClientTooltipComponent.create(component.text())
        );
        e.register(
            RegulatorUpgradeItem.RegulatorTooltipComponent.class,
            component -> createRegulatorUpgradeClientTooltipComponent(
                component.destinations(),
                component.configuredResource(),
                component.helpText()
            )
        );
        e.register(PatternItem.CraftingPatternTooltipComponent.class, PatternTooltipCache::getComponent);
        e.register(PatternItem.ProcessingPatternTooltipComponent.class, PatternTooltipCache::getComponent);
        e.register(PatternItem.StonecutterPatternTooltipComponent.class, PatternTooltipCache::getComponent);
        e.register(PatternItem.SmithingTablePatternTooltipComponent.class, PatternTooltipCache::getComponent);
    }

    private static void registerItemProperties() {
        ItemProperties.register(
            Items.INSTANCE.getWirelessGrid(),
            NetworkItemPropertyFunction.NAME,
            new NetworkItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getCreativeWirelessGrid(),
            NetworkItemPropertyFunction.NAME,
            new NetworkItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getConfigurationCard(),
            ConfigurationCardItemPropertyFunction.NAME,
            new ConfigurationCardItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getNetworkCard(),
            NetworkCardItemPropertyFunction.NAME,
            new NetworkCardItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getSecurityCard(),
            SecurityCardItemPropertyFunction.NAME,
            new SecurityCardItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getWirelessAutocraftingMonitor(),
            NetworkItemPropertyFunction.NAME,
            new NetworkItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getCreativeWirelessAutocraftingMonitor(),
            NetworkItemPropertyFunction.NAME,
            new NetworkItemPropertyFunction()
        );
    }
}

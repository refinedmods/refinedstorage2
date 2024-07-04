package com.refinedmods.refinedstorage.platform.neoforge;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage.platform.common.configurationcard.ConfigurationCardItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage.platform.common.controller.ControllerItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.networking.NetworkCardItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage.platform.common.support.network.bounditem.NetworkBoundItemItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.ResourceClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.neoforge.storage.diskdrive.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.platform.neoforge.storage.diskdrive.DiskDriveGeometryLoader;
import com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface.DiskInterfaceBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface.DiskInterfaceGeometryLoader;
import com.refinedmods.refinedstorage.platform.neoforge.storage.portablegrid.PortableGridBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.platform.neoforge.storage.portablegrid.PortableGridGeometryLoader;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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

import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.PORTABLE_GRID;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslationKey;

public final class ClientModInitializer extends AbstractClientModInitializer {
    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onKeyInput);
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

    private static void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemProperties.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerItemPropertyFunction()
        ));
    }

    @SubscribeEvent
    public static void onRegisterModelGeometry(final ModelEvent.RegisterGeometryLoaders e) {
        registerDiskModels();
        e.register(DISK_DRIVE, new DiskDriveGeometryLoader());
        e.register(PORTABLE_GRID, new PortableGridGeometryLoader());
        Blocks.INSTANCE.getDiskInterface().forEach(
            (color, id, supplier) -> e.register(id, new DiskInterfaceGeometryLoader(color))
        );
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
            createTranslationKey("key", "focus_search_bar"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(focusSearchBarKeyBinding);
        KeyMappings.INSTANCE.setFocusSearchBar(focusSearchBarKeyBinding);

        final KeyMapping clearCraftingGridMatrixToNetwork = new KeyMapping(
            createTranslationKey("key", "clear_crafting_grid_matrix_to_network"),
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(clearCraftingGridMatrixToNetwork);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToNetwork(clearCraftingGridMatrixToNetwork);

        final KeyMapping clearCraftingGridMatrixToInventory = new KeyMapping(
            createTranslationKey("key", "clear_crafting_grid_matrix_to_inventory"),
            InputConstants.UNKNOWN.getValue(),
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(clearCraftingGridMatrixToInventory);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToInventory(clearCraftingGridMatrixToInventory);

        final KeyMapping openWirelessGrid = new KeyMapping(
            createTranslationKey("key", "open_wireless_grid"),
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            ContentNames.MOD_TRANSLATION_KEY
        );
        e.register(openWirelessGrid);
        KeyMappings.INSTANCE.setOpenWirelessGrid(openWirelessGrid);
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
            component -> new UpgradeDestinationClientTooltipComponent(component.destinations())
        );
        e.register(
            HelpTooltipComponent.class,
            component -> HelpClientTooltipComponent.create(component.text())
        );
        e.register(
            RegulatorUpgradeItem.RegulatorTooltipComponent.class,
            component -> {
                final ClientTooltipComponent help = HelpClientTooltipComponent.create(component.helpText());
                return component.configuredResource() == null
                    ? help
                    : createRegulatorUpgradeClientTooltipComponent(component.configuredResource(), help);
            }
        );
    }

    private static CompositeClientTooltipComponent createRegulatorUpgradeClientTooltipComponent(
        final ResourceAmount configuredResource,
        final ClientTooltipComponent help
    ) {
        return new CompositeClientTooltipComponent(List.of(
            new ResourceClientTooltipComponent(configuredResource),
            help
        ));
    }

    private static void registerItemProperties() {
        ItemProperties.register(
            Items.INSTANCE.getWirelessGrid(),
            NetworkBoundItemItemPropertyFunction.NAME,
            new NetworkBoundItemItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getCreativeWirelessGrid(),
            NetworkBoundItemItemPropertyFunction.NAME,
            new NetworkBoundItemItemPropertyFunction()
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
    }
}

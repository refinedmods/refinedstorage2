package com.refinedmods.refinedstorage.neoforge;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternTooltipCache;
import com.refinedmods.refinedstorage.common.autocrafting.PatternTypeItemModelProperty;
import com.refinedmods.refinedstorage.common.configurationcard.ActiveConfigurationCardItemModelProperty;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.KeyMappings;
import com.refinedmods.refinedstorage.common.controller.ControllerEnergyLevelItemModelProperty;
import com.refinedmods.refinedstorage.common.networking.ActiveNetworkCardItemModelProperty;
import com.refinedmods.refinedstorage.common.security.ActiveSecurityCardItemModelProperty;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage.common.support.RecipeMapRecipeProvider;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkBoundItemModelProperty;
import com.refinedmods.refinedstorage.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage.neoforge.debug.NetworkDebugRenderer;
import com.refinedmods.refinedstorage.neoforge.networking.ActiveInactiveCablePartUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.networking.CablePartUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.networking.CableUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveItemModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.DiskDriveUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.ForgeDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.DiskInterfaceItemModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.DiskInterfaceUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.ForgeDiskInterfaceBlockEntityRenderer;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.ForgePortableGridBlockEntityRenderer;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.PortableGridItemModel;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.PortableGridUnbakedBlockStateModel;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.content.ContentIds.CABLE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.DISK_INTERFACE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.PORTABLE_GRID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class ClientModInitializer extends AbstractClientModInitializer {
    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onKeyInput);
        NeoForge.EVENT_BUS.addListener(ClientModInitializer::onMouseInput);
        if (Platform.INSTANCE.getConfig().isDebug()) {
            NeoForge.EVENT_BUS.addListener(NetworkDebugRenderer::renderDebugOverlay);
        }
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

    @SubscribeEvent
    public static void onRegisterItemModels(final RegisterItemModelsEvent e) {
        e.register(DISK_DRIVE, DiskDriveItemModel.Unbaked.CODEC);
        e.register(DISK_INTERFACE, DiskInterfaceItemModel.Unbaked.CODEC);
        e.register(PORTABLE_GRID, PortableGridItemModel.Unbaked.CODEC);
    }

    @SubscribeEvent
    public static void onRegisterBlockStateModels(final RegisterBlockStateModels e) {
        e.registerModel(CABLE, CableUnbakedBlockStateModel.MODEL_CODEC);
        e.registerModel(createIdentifier("active_inactive_cable_part"),
            ActiveInactiveCablePartUnbakedBlockStateModel.MODEL_CODEC);
        e.registerModel(createIdentifier("cable_part"), CablePartUnbakedBlockStateModel.MODEL_CODEC);
        e.registerModel(DISK_DRIVE, DiskDriveUnbakedBlockStateModel.MODEL_CODEC);
        e.registerModel(DISK_INTERFACE, DiskInterfaceUnbakedBlockStateModel.MODEL_CODEC);
        e.registerModel(PORTABLE_GRID, PortableGridUnbakedBlockStateModel.MODEL_CODEC);
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
        final KeyMapping.Category category = new KeyMapping.Category(createIdentifier("keymappings"));
        e.registerCategory(category);

        final KeyMapping focusSearchBarKeyBinding = new KeyMapping(
            ContentNames.FOCUS_SEARCH_BAR_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            category
        );
        e.register(focusSearchBarKeyBinding);
        KeyMappings.INSTANCE.setFocusSearchBar(focusSearchBarKeyBinding);

        final KeyMapping clearCraftingGridMatrixToNetwork = new KeyMapping(
            ContentNames.CLEAR_CRAFTING_MATRIX_TO_NETWORK_TRANSLATION_KEY,
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            category
        );
        e.register(clearCraftingGridMatrixToNetwork);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToNetwork(clearCraftingGridMatrixToNetwork);

        final KeyMapping clearCraftingGridMatrixToInventory = new KeyMapping(
            ContentNames.CLEAR_CRAFTING_MATRIX_TO_INVENTORY_TRANSLATION_KEY,
            InputConstants.UNKNOWN.getValue(),
            category
        );
        e.register(clearCraftingGridMatrixToInventory);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToInventory(clearCraftingGridMatrixToInventory);

        final KeyMapping openWirelessGrid = new KeyMapping(
            ContentNames.OPEN_WIRELESS_GRID_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            category
        );
        e.register(openWirelessGrid);
        KeyMappings.INSTANCE.setOpenWirelessGrid(openWirelessGrid);

        final KeyMapping openPortableGrid = new KeyMapping(
            ContentNames.OPEN_PORTABLE_GRID_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            category
        );
        e.register(openPortableGrid);
        KeyMappings.INSTANCE.setOpenPortableGrid(openPortableGrid);

        final KeyMapping openWirelessAutocraftingMonitor = new KeyMapping(
            ContentNames.OPEN_WIRELESS_AUTOCRAFTING_MONITOR_TRANSLATION_KEY,
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            category
        );
        e.register(openWirelessAutocraftingMonitor);
        KeyMappings.INSTANCE.setOpenWirelessAutocraftingMonitor(openWirelessAutocraftingMonitor);
    }

    private static void registerBlockEntityRenderer() {
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskDrive(),
            ctx -> new ForgeDiskDriveBlockEntityRenderer<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskInterface(),
            ctx -> new ForgeDiskInterfaceBlockEntityRenderer<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getStorageMonitor(),
            ctx -> new StorageMonitorBlockEntityRenderer()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getPortableGrid(),
            ctx -> new ForgePortableGridBlockEntityRenderer<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getCreativePortableGrid(),
            ctx -> new ForgePortableGridBlockEntityRenderer<>()
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

    @SubscribeEvent
    public static void registerRangeItemProperties(final RegisterRangeSelectItemModelPropertyEvent e) {
        e.register(ControllerEnergyLevelItemModelProperty.NAME, ControllerEnergyLevelItemModelProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerConditionalItemProperties(final RegisterConditionalItemModelPropertyEvent e) {
        e.register(
            ActiveNetworkCardItemModelProperty.NAME,
            ActiveNetworkCardItemModelProperty.MAP_CODEC
        );
        e.register(
            ActiveConfigurationCardItemModelProperty.NAME,
            ActiveConfigurationCardItemModelProperty.MAP_CODEC
        );
        e.register(
            ActiveSecurityCardItemModelProperty.NAME,
            ActiveSecurityCardItemModelProperty.MAP_CODEC
        );
        e.register(
            NetworkBoundItemModelProperty.NAME,
            NetworkBoundItemModelProperty.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerSelectItemProperties(final RegisterSelectItemModelPropertyEvent e) {
        e.register(
            PatternTypeItemModelProperty.NAME,
            PatternTypeItemModelProperty.PROPERTY_TYPE
        );
    }

    @SubscribeEvent
    public static void onRecipesReceived(final RecipesReceivedEvent e) {
        Platform.INSTANCE.setClientRecipeProvider(new RecipeMapRecipeProvider(e.getRecipeMap()));
    }
}

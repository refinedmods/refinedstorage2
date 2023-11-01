package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage2.platform.common.configurationcard.ConfigurationCardItemPropertyFunction;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkCardItemPropertyFunction;
import com.refinedmods.refinedstorage2.platform.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.common.support.networkbounditem.NetworkBoundItemItemPropertyFunction;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.ResourceClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.forge.recipemod.rei.RefinedStorageREIClientPlugin;
import com.refinedmods.refinedstorage2.platform.forge.recipemod.rei.ReiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.forge.recipemod.rei.ReiProxy;
import com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive.DiskDriveGeometryLoader;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationKey;

public final class ClientModInitializer extends AbstractClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModInitializer.class);
    private static final String KEY_BINDINGS_TRANSLATION_KEY = createTranslationKey("category", "key_bindings");

    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        MinecraftForge.EVENT_BUS.addListener(ClientModInitializer::onKeyInput);
        e.enqueueWork(ClientModInitializer::registerModelPredicates);
        e.enqueueWork(() -> registerScreens(new ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
                final MenuType<? extends M> type,
                final ScreenConstructor<M, U> factory
            ) {
                MenuScreens.register(type, factory::create);
            }
        }));
        e.enqueueWork(ClientModInitializer::registerItemProperties);
        registerBlockEntityRenderer();
        registerGridSynchronizers();
        registerResourceRendering();
        registerAlternativeGridHints();
    }

    @SubscribeEvent
    public static void onKeyInput(final InputEvent.Key e) {
        handleInputEvents();
    }

    private static void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemProperties.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerModelPredicateProvider()
        ));
    }

    @SubscribeEvent
    public static void onRegisterModelGeometry(final ModelEvent.RegisterGeometryLoaders e) {
        e.register(DISK_DRIVE.getPath(), new DiskDriveGeometryLoader());
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent e) {
        final KeyMapping focusSearchBarKeyBinding = new KeyMapping(
            createTranslationKey("key", "focus_search_bar"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            KEY_BINDINGS_TRANSLATION_KEY
        );
        e.register(focusSearchBarKeyBinding);
        KeyMappings.INSTANCE.setFocusSearchBar(focusSearchBarKeyBinding);

        final KeyMapping clearCraftingGridMatrixToNetwork = new KeyMapping(
            createTranslationKey("key", "clear_crafting_grid_matrix_to_network"),
            KeyConflictContext.GUI,
            KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEY_BINDINGS_TRANSLATION_KEY
        );
        e.register(clearCraftingGridMatrixToNetwork);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToNetwork(clearCraftingGridMatrixToNetwork);

        final KeyMapping clearCraftingGridMatrixToInventory = new KeyMapping(
            createTranslationKey("key", "clear_crafting_grid_matrix_to_inventory"),
            InputConstants.UNKNOWN.getValue(),
            KEY_BINDINGS_TRANSLATION_KEY
        );
        e.register(clearCraftingGridMatrixToInventory);
        KeyMappings.INSTANCE.setClearCraftingGridMatrixToInventory(clearCraftingGridMatrixToInventory);

        final KeyMapping openWirelessGrid = new KeyMapping(
            createTranslationKey("key", "open_wireless_grid"),
            KeyConflictContext.IN_GAME,
            InputConstants.UNKNOWN,
            KEY_BINDINGS_TRANSLATION_KEY
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
    }

    private static void registerGridSynchronizers() {
        final ModList list = ModList.get();
        // Give priority to REI, as REI requires a JEI compat mod on Forge.
        // This means that both JEI + REI support would be activated. We only want REI in that case.
        if (list.isLoaded("roughlyenoughitems")) {
            registerReiGridSynchronizers();
        }
    }

    private static void registerReiGridSynchronizers() {
        LOGGER.info("Enabling REI grid synchronizers");
        // This is so the ingredient converters are only registered once
        // see https://github.com/refinedmods/refinedstorage2/pull/302#discussion_r1070015672
        RefinedStorageREIClientPlugin.registerIngredientConverters();
        final ReiProxy reiProxy = new ReiProxy();
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("rei"),
            new ReiGridSynchronizer(reiProxy, false)
        );
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("rei_two_way"),
            new ReiGridSynchronizer(reiProxy, true)
        );
    }

    @SubscribeEvent
    @SuppressWarnings("unchecked")
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
                return component.filteredResource() == null
                    ? help
                    : createRegulatorUpgradeClientTooltipComponent(component.filteredResource(), help);
            }
        );
    }

    private static <T> CompositeClientTooltipComponent createRegulatorUpgradeClientTooltipComponent(
        final ResourceAmountTemplate<T> filteredResource,
        final ClientTooltipComponent help
    ) {
        return new CompositeClientTooltipComponent(List.of(
            new ResourceClientTooltipComponent<>(filteredResource),
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
    }
}

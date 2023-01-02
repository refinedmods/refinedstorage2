package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.integration.jei.JeiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.integration.jei.JeiProxy;
import com.refinedmods.refinedstorage2.platform.common.render.model.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.common.screen.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ExporterScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ExternalStorageScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.FluidStorageBlockScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ImporterScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.InterfaceScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ItemStorageBlockScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.FluidGridScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.ItemGridScreen;
import com.refinedmods.refinedstorage2.platform.forge.integration.rei.ReiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.forge.integration.rei.ReiProxy;
import com.refinedmods.refinedstorage2.platform.forge.render.entity.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage2.platform.forge.render.model.DiskDriveGeometryLoader;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationKey;

public final class ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModInitializer.class);

    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent e) {
        e.enqueueWork(ClientModInitializer::registerModelPredicates);
        e.enqueueWork(ClientModInitializer::registerScreens);
        registerBlockEntityRenderer();
        registerGridSynchronizers();
    }

    private static void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemProperties.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerModelPredicateProvider()
        ));
    }

    private static void registerScreens() {
        MenuScreens.register(Menus.INSTANCE.getController(), ControllerScreen::new);
        MenuScreens.register(Menus.INSTANCE.getDiskDrive(), DiskDriveScreen::new);
        MenuScreens.register(Menus.INSTANCE.getGrid(), ItemGridScreen::new);
        MenuScreens.register(Menus.INSTANCE.getFluidGrid(), FluidGridScreen::new);
        MenuScreens.register(Menus.INSTANCE.getItemStorage(), ItemStorageBlockScreen::new);
        MenuScreens.register(Menus.INSTANCE.getFluidStorage(), FluidStorageBlockScreen::new);
        MenuScreens.register(Menus.INSTANCE.getImporter(), ImporterScreen::new);
        MenuScreens.register(Menus.INSTANCE.getExporter(), ExporterScreen::new);
        MenuScreens.register(Menus.INSTANCE.getInterface(), InterfaceScreen::new);
        MenuScreens.register(Menus.INSTANCE.getExternalStorage(), ExternalStorageScreen::new);
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
            createTranslationKey("category", "key_bindings")
        );
        e.register(focusSearchBarKeyBinding);
        KeyMappings.INSTANCE.setFocusSearchBar(focusSearchBarKeyBinding);
    }

    private static void registerBlockEntityRenderer() {
        BlockEntityRenderers.register(BlockEntities.INSTANCE.getDiskDrive(),
            ctx -> new DiskDriveBlockEntityRendererImpl<>());
    }

    private static void registerGridSynchronizers() {
        final ModList list = ModList.get();
        // Give priority to REI, as REI requires a JEI compat mod on Forge.
        // This means that both JEI + REI support would be activated. We only want REI in that case.
        if (list.isLoaded("roughlyenoughitems")) {
            registerReiGridSynchronizers();
        } else if (list.isLoaded("jei")) {
            registerJeiGridSynchronizers();
        }
    }

    private static void registerJeiGridSynchronizers() {
        LOGGER.info("Activating JEI grid synchronizers");
        final JeiProxy jeiProxy = new JeiProxy();
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("jei"),
            new JeiGridSynchronizer(jeiProxy, false)
        );
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("jei_two_way"),
            new JeiGridSynchronizer(jeiProxy, true)
        );
    }

    private static void registerReiGridSynchronizers() {
        LOGGER.info("Activating REI grid synchronizers");
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
}

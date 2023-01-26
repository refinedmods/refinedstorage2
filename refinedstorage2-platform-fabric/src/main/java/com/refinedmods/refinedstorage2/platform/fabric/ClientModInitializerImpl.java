package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.render.model.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.common.screen.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ExporterScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ExternalStorageScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.FluidStorageBlockScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ImporterScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.InterfaceScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.ItemStorageBlockScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.GridScreen;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.fabric.integration.jei.JeiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.fabric.integration.jei.JeiProxy;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiProxy;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.ItemPropertiesAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ControllerEnergyInfoPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ResourceFilterSlotUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage2.platform.fabric.render.entity.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.DiskDriveUnbakedModel;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.EmissiveModelRegistry;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationKey;

public class ClientModInitializerImpl implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModInitializerImpl.class);

    @Override
    public void onInitializeClient() {
        setRenderLayers();
        registerEmissiveModels();
        registerPackets();
        registerBlockEntityRenderers();
        registerCustomModels();
        registerScreens();
        registerKeyBindings();
        registerModelPredicates();
        registerGridSynchronizers();
    }

    private void setRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.INSTANCE.getCable(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.INSTANCE.getImporter(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.INSTANCE.getExporter(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.INSTANCE.getExternalStorage(), RenderType.cutout());
        Blocks.INSTANCE.getGrid().values().forEach(block ->
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Blocks.INSTANCE.getController().values().forEach(block ->
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Blocks.INSTANCE.getCreativeController().values().forEach(block ->
            BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
    }

    private void registerEmissiveModels() {
        for (final DyeColor color : DyeColor.values()) {
            registerEmissiveControllerModels(color);
            registerEmissiveGridModels(color);
        }
    }

    private void registerEmissiveControllerModels(final DyeColor color) {
        final ResourceLocation spriteLocation = createIdentifier("block/controller/cutouts/" + color.getName());
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/controller/" + color.getName()),
            spriteLocation
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(
            ColorMap.generateId(color, IdentifierUtil.MOD_ID, "controller"),
            spriteLocation
        );
        EmissiveModelRegistry.INSTANCE.register(
            ColorMap.generateId(color, IdentifierUtil.MOD_ID, "creative_controller"),
            spriteLocation
        );
    }

    private void registerEmissiveGridModels(final DyeColor color) {
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/grid/" + color.getName()),
            createIdentifier("block/grid/cutouts/" + color.getName())
        );
        EmissiveModelRegistry.INSTANCE.register(
            ColorMap.generateId(color, IdentifierUtil.MOD_ID, "grid"),
            createIdentifier("block/grid/cutouts/" + color.getName())
        );
    }

    private void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_RESPONSE, new StorageInfoResponsePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_UPDATE, new GridUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ACTIVE, new GridActivePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.CONTROLLER_ENERGY_INFO, new ControllerEnergyInfoPacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.RESOURCE_FILTER_SLOT_UPDATE,
            new ResourceFilterSlotUpdatePacket());
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskDrive(),
            ctx -> new DiskDriveBlockEntityRendererImpl<>()
        );
    }

    private void registerCustomModels() {
        final ResourceLocation diskDriveIdentifier = createIdentifier("block/disk_drive");
        final ResourceLocation diskDriveIdentifierItem = createIdentifier("item/disk_drive");

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (identifier, ctx) -> {
            if (identifier.equals(diskDriveIdentifier) || identifier.equals(diskDriveIdentifierItem)) {
                return new DiskDriveUnbakedModel();
            }
            return null;
        });
    }

    private void registerScreens() {
        MenuScreens.register(Menus.INSTANCE.getDiskDrive(), DiskDriveScreen::new);
        MenuScreens.register(Menus.INSTANCE.getGrid(), GridScreen::new);
        MenuScreens.register(Menus.INSTANCE.getController(), ControllerScreen::new);
        MenuScreens.register(Menus.INSTANCE.getItemStorage(), ItemStorageBlockScreen::new);
        MenuScreens.register(Menus.INSTANCE.getFluidStorage(), FluidStorageBlockScreen::new);
        MenuScreens.register(Menus.INSTANCE.getImporter(), ImporterScreen::new);
        MenuScreens.register(Menus.INSTANCE.getExporter(), ExporterScreen::new);
        MenuScreens.register(Menus.INSTANCE.getInterface(), InterfaceScreen::new);
        MenuScreens.register(Menus.INSTANCE.getExternalStorage(), ExternalStorageScreen::new);
    }

    private void registerKeyBindings() {
        KeyMappings.INSTANCE.setFocusSearchBar(KeyBindingHelper.registerKeyBinding(new KeyMapping(
            createTranslationKey("key", "focus_search_bar"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            createTranslationKey("category", "key_bindings")
        )));
    }

    private void registerModelPredicates() {
        Items.INSTANCE.getRegularControllers().forEach(controllerBlockItem -> ItemPropertiesAccessor.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerModelPredicateProvider()
        ));
    }

    private void registerGridSynchronizers() {
        final FabricLoader loader = FabricLoader.getInstance();
        if (loader.isModLoaded("jei")) {
            registerJeiGridSynchronizers();
        }
        if (loader.isModLoaded("roughlyenoughitems")) {
            registerReiGridSynchronizers();
        }
    }

    private void registerJeiGridSynchronizers() {
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

    private void registerReiGridSynchronizers() {
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

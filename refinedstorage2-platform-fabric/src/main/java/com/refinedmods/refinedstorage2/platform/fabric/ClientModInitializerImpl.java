package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.render.model.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.common.screen.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.StorageScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.FluidGridScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.GridScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.ItemGridScreen;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiIntegration;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiProxy;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.ItemPropertiesAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ControllerEnergyPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridFluidUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridItemUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ResourceFilterSlotUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage2.platform.fabric.render.entity.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.DiskDriveUnbakedModel;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationKey;

public class ClientModInitializerImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setRenderLayers();
        registerPackets();
        registerBlockEntityRenderers();
        registerCustomModels();
        registerScreens();
        registerKeyBindings();
        registerModelPredicates();
        registerGridSynchronizer();
    }

    private void setRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.INSTANCE.getCable(), RenderType.cutout());
        Blocks.INSTANCE.getGrid().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Blocks.INSTANCE.getFluidGrid().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Blocks.INSTANCE.getController().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Blocks.INSTANCE.getCreativeController().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
    }

    private void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_RESPONSE, new StorageInfoResponsePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ITEM_UPDATE, new GridItemUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_FLUID_UPDATE, new GridFluidUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ACTIVE, new GridActivePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.CONTROLLER_ENERGY, new ControllerEnergyPacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.RESOURCE_FILTER_SLOT_UPDATE, new ResourceFilterSlotUpdatePacket());
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRendererRegistry.register(BlockEntities.INSTANCE.getDiskDrive(), ctx -> new DiskDriveBlockEntityRendererImpl<>());
    }

    private void registerCustomModels() {
        ResourceLocation diskDriveIdentifier = createIdentifier("block/disk_drive");
        ResourceLocation diskDriveIdentifierItem = createIdentifier("item/disk_drive");

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (identifier, modelProviderContext) -> {
            if (identifier.equals(diskDriveIdentifier)) {
                return new DiskDriveUnbakedModel();
            } else if (identifier.equals(diskDriveIdentifierItem)) {
                return new DiskDriveUnbakedModel();
            }
            return null;
        });
    }

    private void registerScreens() {
        ScreenRegistry.register(Menus.INSTANCE.getDiskDrive(), DiskDriveScreen::new);
        ScreenRegistry.register(Menus.INSTANCE.getGrid(), ItemGridScreen::new);
        ScreenRegistry.register(Menus.INSTANCE.getFluidGrid(), FluidGridScreen::new);
        ScreenRegistry.register(Menus.INSTANCE.getController(), ControllerScreen::new);
        ScreenRegistry.register(Menus.INSTANCE.getItemStorage(), StorageScreen::new);
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
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemPropertiesAccessor.register(
                controllerBlockItem,
                createIdentifier("stored_in_controller"),
                new ControllerModelPredicateProvider()
        ));
    }

    private void registerGridSynchronizer() {
        if (ReiIntegration.isLoaded()) {
            GridScreen.setSynchronizer(new ReiGridSynchronizer(new ReiProxy()));
        }
    }
}

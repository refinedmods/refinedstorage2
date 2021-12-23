package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.fabric.mixin.ItemPropertiesAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ControllerEnergyPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridFluidUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridItemUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ResourceFilterSlotUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage2.platform.fabric.render.entity.DiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.DiskDriveUnbakedModel;
import com.refinedmods.refinedstorage2.platform.fabric.screen.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.FluidGridScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.ItemGridScreen;

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

public class Rs2ClientMod implements ClientModInitializer {
    private static KeyMapping focusSearchBar;

    public static KeyMapping getFocusSearchBarKeyBinding() {
        return focusSearchBar;
    }

    @Override
    public void onInitializeClient() {
        setRenderLayers();
        registerPackets();
        registerBlockEntityRenderers();
        registerCustomModels();
        registerScreens();
        registerKeyBindings();
        registerModelPredicates();
    }

    private void registerModelPredicates() {
        Rs2Mod.ITEMS.getControllers().forEach(controllerBlockItem -> ItemPropertiesAccessor.register(
                controllerBlockItem,
                Rs2Mod.createIdentifier("stored_in_controller"),
                new ControllerModelPredicateProvider()
        ));
    }

    private void setRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(Rs2Mod.BLOCKS.getCable(), RenderType.cutout());

        if (Rs2Mod.FEATURES.contains(FeatureFlag.RELAY)) {
            BlockRenderLayerMap.INSTANCE.putBlock(Rs2Mod.BLOCKS.getRelay(), RenderType.cutout());
        }

        Rs2Mod.BLOCKS.getGrid().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Rs2Mod.BLOCKS.getFluidGrid().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Rs2Mod.BLOCKS.getController().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
        Rs2Mod.BLOCKS.getCreativeController().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout()));
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRendererRegistry.register(Rs2Mod.BLOCK_ENTITIES.getDiskDrive(), ctx -> new DiskDriveBlockEntityRenderer());
    }

    private void registerCustomModels() {
        ResourceLocation diskDriveIdentifier = Rs2Mod.createIdentifier("block/disk_drive");

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (identifier, modelProviderContext) -> {
            if (identifier.equals(diskDriveIdentifier)) {
                return new DiskDriveUnbakedModel();
            }
            return null;
        });
    }

    private void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_RESPONSE, new StorageInfoResponsePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ITEM_UPDATE, new GridItemUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_FLUID_UPDATE, new GridFluidUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ACTIVE, new GridActivePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.CONTROLLER_ENERGY, new ControllerEnergyPacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.RESOURCE_FILTER_SLOT_UPDATE, new ResourceFilterSlotUpdatePacket());
    }

    private void registerKeyBindings() {
        focusSearchBar = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                String.format("key.%s.focus_search_bar", Rs2Mod.ID),
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                String.format("category.%s.key_bindings", Rs2Mod.ID)
        ));
    }

    private void registerScreens() {
        ScreenRegistry.register(Rs2Mod.MENUS.getDiskDrive(), DiskDriveScreen::new);
        ScreenRegistry.register(Rs2Mod.MENUS.getGrid(), ItemGridScreen::new);
        ScreenRegistry.register(Rs2Mod.MENUS.getFluidGrid(), FluidGridScreen::new);
        ScreenRegistry.register(Rs2Mod.MENUS.getController(), ControllerScreen::new);
    }
}

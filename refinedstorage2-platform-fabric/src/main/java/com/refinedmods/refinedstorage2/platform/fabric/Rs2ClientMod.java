package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.fabric.mixin.ModelPredicateProviderRegistryAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ControllerEnergyPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridFluidUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridItemUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.StorageDiskInfoResponsePacket;
import com.refinedmods.refinedstorage2.platform.fabric.render.entity.DiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.fabric.render.model.DiskDriveUnbakedModel;
import com.refinedmods.refinedstorage2.platform.fabric.screen.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.FluidGridScreen;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.ItemGridScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Rs2ClientMod implements ClientModInitializer {
    private static KeyBinding focusSearchBar;

    public static KeyBinding getFocusSearchBarKeyBinding() {
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
        Rs2Mod.ITEMS.getControllers().forEach(controllerBlockItem -> ModelPredicateProviderRegistryAccessor.register(
                controllerBlockItem,
                Rs2Mod.createIdentifier("stored_in_controller"),
                new ControllerModelPredicateProvider()
        ));
    }

    private void setRenderLayers() {
        BlockRenderLayerMap.INSTANCE.putBlock(Rs2Mod.BLOCKS.getCable(), RenderLayer.getCutout());

        if (Rs2Mod.FEATURES.contains(FeatureFlag.RELAY)) {
            BlockRenderLayerMap.INSTANCE.putBlock(Rs2Mod.BLOCKS.getRelay(), RenderLayer.getCutout());
        }

        Rs2Mod.BLOCKS.getGrid().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout()));
        Rs2Mod.BLOCKS.getFluidGrid().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout()));
        Rs2Mod.BLOCKS.getController().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout()));
        Rs2Mod.BLOCKS.getCreativeController().values().forEach(block -> BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout()));
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRendererRegistry.INSTANCE.register(Rs2Mod.BLOCK_ENTITIES.getDiskDrive(), ctx -> new DiskDriveBlockEntityRenderer());
    }

    private void registerCustomModels() {
        Identifier diskDriveIdentifier = Rs2Mod.createIdentifier("block/disk_drive");

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (identifier, modelProviderContext) -> {
            if (identifier.equals(diskDriveIdentifier)) {
                return new DiskDriveUnbakedModel();
            }
            return null;
        });
    }

    private void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_DISK_INFO_RESPONSE, new StorageDiskInfoResponsePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ITEM_UPDATE, new GridItemUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_FLUID_UPDATE, new GridFluidUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ACTIVE, new GridActivePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.CONTROLLER_ENERGY, new ControllerEnergyPacket());
    }

    private void registerKeyBindings() {
        focusSearchBar = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                String.format("key.%s.focus_search_bar", Rs2Mod.ID),
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_TAB,
                String.format("category.%s.key_bindings", Rs2Mod.ID)
        ));
    }

    private void registerScreens() {
        ScreenRegistry.register(Rs2Mod.SCREEN_HANDLERS.getDiskDrive(), DiskDriveScreen::new);
        ScreenRegistry.register(Rs2Mod.SCREEN_HANDLERS.getGrid(), ItemGridScreen::new);
        ScreenRegistry.register(Rs2Mod.SCREEN_HANDLERS.getFluidGrid(), FluidGridScreen::new);
        ScreenRegistry.register(Rs2Mod.SCREEN_HANDLERS.getController(), ControllerScreen::new);
    }
}

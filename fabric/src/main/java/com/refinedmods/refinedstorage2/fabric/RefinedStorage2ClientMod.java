package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.fabric.packet.s2c.StorageDiskInfoResponsePacket;
import com.refinedmods.refinedstorage2.fabric.render.entity.DiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage2.fabric.render.model.DiskDriveUnbakedModel;
import com.refinedmods.refinedstorage2.fabric.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.fabric.screen.GridScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RefinedStorage2ClientMod implements ClientModInitializer {
    private static KeyBinding FOCUS_SEARCH_BAR;

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(RefinedStorage2Mod.BLOCKS.getCable(), RenderLayer.getCutout());

        ClientSidePacketRegistry.INSTANCE.register(StorageDiskInfoResponsePacket.ID, new StorageDiskInfoResponsePacket());

        BlockEntityRendererRegistry.INSTANCE.register(RefinedStorage2Mod.BLOCK_ENTITIES.getDiskDrive(), DiskDriveBlockEntityRenderer::new);

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (identifier, modelProviderContext) -> {
            if (identifier.equals(new Identifier(RefinedStorage2Mod.ID, "block/disk_drive"))) {
                return new DiskDriveUnbakedModel();
            }
            return null;
        });

        ScreenRegistry.register(RefinedStorage2Mod.SCREEN_HANDLERS.getDiskDrive(), DiskDriveScreen::new);
        ScreenRegistry.register(RefinedStorage2Mod.SCREEN_HANDLERS.getGrid(), GridScreen::new);

        FOCUS_SEARCH_BAR = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.refinedstorage2.focus_search_bar",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            "category.refinedstorage2.key_bindings"
        ));
    }

    public static KeyBinding getFocusSearchBarKeyBinding() {
        return FOCUS_SEARCH_BAR;
    }
}

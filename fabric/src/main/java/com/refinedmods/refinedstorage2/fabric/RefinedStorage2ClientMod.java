package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.fabric.packet.s2c.StorageDiskInfoResponsePacket;
import com.refinedmods.refinedstorage2.fabric.render.DiskDriveBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class RefinedStorage2ClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(RefinedStorage2Mod.BLOCKS.getCable(), RenderLayer.getCutout());

        ClientSidePacketRegistry.INSTANCE.register(StorageDiskInfoResponsePacket.ID, new StorageDiskInfoResponsePacket());

        BlockEntityRendererRegistry.INSTANCE.register(RefinedStorage2Mod.BLOCK_ENTITIES.getDiskDrive(), DiskDriveBlockEntityRenderer::new);

        ClientSpriteRegistryCallback.event(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).register(((spriteAtlasTexture, registry) -> registry.register(new Identifier(RefinedStorage2Mod.ID, "block/disk"))));
    }
}

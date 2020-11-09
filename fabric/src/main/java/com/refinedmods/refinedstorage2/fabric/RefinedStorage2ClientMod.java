package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.fabric.packet.s2c.StorageDiskInfoResponsePacket;
import com.refinedmods.refinedstorage2.fabric.render.entity.DiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage2.fabric.render.model.DiskDriveUnbakedModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class RefinedStorage2ClientMod implements ClientModInitializer {
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
    }
}

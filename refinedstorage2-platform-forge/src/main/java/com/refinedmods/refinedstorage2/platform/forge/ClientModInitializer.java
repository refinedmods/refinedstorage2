package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class ClientModInitializer {
    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(ClientModInitializer::setRenderLayers);
    }

    private static void setRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(Blocks.INSTANCE.getCable(), RenderType.cutout());
    }
}

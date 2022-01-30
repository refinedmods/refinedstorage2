package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.render.model.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.common.screen.ControllerScreen;
import com.refinedmods.refinedstorage2.platform.common.screen.DiskDriveScreen;
import com.refinedmods.refinedstorage2.platform.forge.render.model.DiskDriveModelLoader;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public final class ClientModInitializer {
    private ClientModInitializer() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        e.enqueueWork(ClientModInitializer::setRenderLayers);
        e.enqueueWork(ClientModInitializer::registerModelPredicates);
        e.enqueueWork(ClientModInitializer::registerScreens);
    }

    private static void registerScreens() {
        MenuScreens.register(Menus.INSTANCE.getController(), ControllerScreen::new);
        MenuScreens.register(Menus.INSTANCE.getDiskDrive(), DiskDriveScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent e) {
        ModelLoaderRegistry.registerLoader(createIdentifier("disk_drive"), new DiskDriveModelLoader());
    }

    private static void setRenderLayers() {
        ItemBlockRenderTypes.setRenderLayer(Blocks.INSTANCE.getCable(), RenderType.cutout());
        Blocks.INSTANCE.getGrid().values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
        Blocks.INSTANCE.getFluidGrid().values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
        Blocks.INSTANCE.getController().values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
        Blocks.INSTANCE.getCreativeController().values().forEach(block -> ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout()));
    }

    private static void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemProperties.register(controllerBlockItem, createIdentifier("stored_in_controller"), new ControllerModelPredicateProvider()));
    }
}

package com.refinedmods.refinedstorage.fabric;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternItem;
import com.refinedmods.refinedstorage.common.autocrafting.PatternTooltipCache;
import com.refinedmods.refinedstorage.common.autocrafting.PatternTypeItemModelProperty;
import com.refinedmods.refinedstorage.common.configurationcard.ActiveConfigurationCardItemModelProperty;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.KeyMappings;
import com.refinedmods.refinedstorage.common.controller.ControllerEnergyLevelItemModelProperty;
import com.refinedmods.refinedstorage.common.networking.ActiveNetworkCardItemModelProperty;
import com.refinedmods.refinedstorage.common.security.ActiveSecurityCardItemModelProperty;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage.common.support.network.item.NetworkBoundItemModelProperty;
import com.refinedmods.refinedstorage.common.support.packet.PacketHandler;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocrafterLockedUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocrafterManagerActivePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocrafterNameUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingMonitorActivePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingMonitorTaskAddedPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingMonitorTaskRemovedPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingMonitorTaskStatusChangedPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingPreviewCancelResponsePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingPreviewMaxAmountResponsePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingPreviewResponsePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingResponsePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingTaskCompletedPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.AutocraftingTreePreviewResponsePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.EnergyInfoPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.ExportingIndicatorUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.GridClearPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.GridUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.MessagePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.NetworkTransmitterStatusPacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.PatternGridAllowedAlternativesUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.ResourceSlotUpdatePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage.common.support.packet.s2c.WirelessTransmitterDataPacket;
import com.refinedmods.refinedstorage.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage.fabric.networking.ActiveInactiveCablePartUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.fabric.networking.CablePartUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.fabric.networking.CableUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.fabric.storage.diskdrive.DiskDriveItemModel;
import com.refinedmods.refinedstorage.fabric.storage.diskdrive.DiskDriveUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.fabric.storage.diskdrive.FabricDiskDriveBlockEntityRenderer;
import com.refinedmods.refinedstorage.fabric.storage.diskinterface.DiskInterfaceItemModel;
import com.refinedmods.refinedstorage.fabric.storage.diskinterface.DiskInterfaceUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.fabric.storage.diskinterface.FabricDiskInterfaceBlockEntityRenderer;
import com.refinedmods.refinedstorage.fabric.storage.portablegrid.FabricPortableGridBlockEntityRenderer;
import com.refinedmods.refinedstorage.fabric.storage.portablegrid.PortableGridItemModel;
import com.refinedmods.refinedstorage.fabric.storage.portablegrid.PortableGridUnbakedBlockStateModel;
import com.refinedmods.refinedstorage.fabric.support.SynchronizedRecipesRecipeProvider;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        initializeClientPlatformApi();
        registerPacketHandlers();
        registerRecipeSync();
        registerBlockEntityRenderers();
        registerCustomModels();
        registerCustomTooltips();
        registerScreens(new ScreenRegistration() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
                final MenuType<? extends M> type,
                final ScreenConstructor<M, U> factory
            ) {
                MenuScreens.register(type, factory::create);
            }
        });
        // TODO: fix fabric startup warnings
        // TODO: scrolling in grid is going wrong direction always down! :)
        registerKeyMappings();
        registerResourceRendering();
        registerAlternativeGridHints();
        registerItemProperties();
    }

    private void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
            StorageInfoResponsePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> StorageInfoResponsePacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            GridUpdatePacket.PACKET_TYPE,
            wrapHandler(GridUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            GridClearPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> GridClearPacket.handle(ctx))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            GridActivePacket.PACKET_TYPE,
            wrapHandler(GridActivePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocrafterManagerActivePacket.PACKET_TYPE,
            wrapHandler(AutocrafterManagerActivePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            EnergyInfoPacket.PACKET_TYPE,
            wrapHandler(EnergyInfoPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            WirelessTransmitterDataPacket.PACKET_TYPE,
            wrapHandler(WirelessTransmitterDataPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            ResourceSlotUpdatePacket.PACKET_TYPE,
            wrapHandler(ResourceSlotUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            NetworkTransmitterStatusPacket.PACKET_TYPE,
            wrapHandler(NetworkTransmitterStatusPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            MessagePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> MessagePacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            PatternGridAllowedAlternativesUpdatePacket.PACKET_TYPE,
            wrapHandler(PatternGridAllowedAlternativesUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocrafterNameUpdatePacket.PACKET_TYPE,
            wrapHandler(AutocrafterNameUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocrafterLockedUpdatePacket.PACKET_TYPE,
            wrapHandler(AutocrafterLockedUpdatePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingPreviewResponsePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingPreviewResponsePacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingTreePreviewResponsePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingTreePreviewResponsePacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingPreviewCancelResponsePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingPreviewCancelResponsePacket.handle())
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingPreviewMaxAmountResponsePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingPreviewMaxAmountResponsePacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingResponsePacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingResponsePacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingMonitorTaskAddedPacket.PACKET_TYPE,
            wrapHandler(AutocraftingMonitorTaskAddedPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingMonitorTaskRemovedPacket.PACKET_TYPE,
            wrapHandler(AutocraftingMonitorTaskRemovedPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingMonitorTaskStatusChangedPacket.PACKET_TYPE,
            wrapHandler(AutocraftingMonitorTaskStatusChangedPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingMonitorActivePacket.PACKET_TYPE,
            wrapHandler(AutocraftingMonitorActivePacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AutocraftingTaskCompletedPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingTaskCompletedPacket.handle(packet))
        );
        ClientPlayNetworking.registerGlobalReceiver(
            ExportingIndicatorUpdatePacket.PACKET_TYPE,
            wrapHandler(ExportingIndicatorUpdatePacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> ClientPlayNetworking.PlayPayloadHandler<T> wrapHandler(
        final PacketHandler<T> handler
    ) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    private void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskDrive(),
            ctx -> new FabricDiskDriveBlockEntityRenderer<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getStorageMonitor(),
            ctx -> new StorageMonitorBlockEntityRenderer()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getPortableGrid(),
            ctx -> new FabricPortableGridBlockEntityRenderer<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getCreativePortableGrid(),
            ctx -> new FabricPortableGridBlockEntityRenderer<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskInterface(),
            ctx -> new FabricDiskInterfaceBlockEntityRenderer<>()
        );
    }

    private void registerCustomModels() {
        registerDiskModels();
        CustomUnbakedBlockStateModel.register(ContentIds.CABLE, CableUnbakedBlockStateModel.MODEL_CODEC);
        CustomUnbakedBlockStateModel.register(createIdentifier("active_inactive_cable_part"),
            ActiveInactiveCablePartUnbakedBlockStateModel.MODEL_CODEC);
        CustomUnbakedBlockStateModel.register(createIdentifier("cable_part"),
            CablePartUnbakedBlockStateModel.MODEL_CODEC);
        CustomUnbakedBlockStateModel.register(ContentIds.DISK_DRIVE, DiskDriveUnbakedBlockStateModel.MODEL_CODEC);
        CustomUnbakedBlockStateModel.register(ContentIds.DISK_INTERFACE,
            DiskInterfaceUnbakedBlockStateModel.MODEL_CODEC);
        CustomUnbakedBlockStateModel.register(ContentIds.PORTABLE_GRID,
            PortableGridUnbakedBlockStateModel.MODEL_CODEC);
        ItemModels.ID_MAPPER.put(ContentIds.DISK_DRIVE, DiskDriveItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(ContentIds.DISK_INTERFACE, DiskInterfaceItemModel.Unbaked.CODEC);
        ItemModels.ID_MAPPER.put(ContentIds.PORTABLE_GRID, PortableGridItemModel.Unbaked.CODEC);
    }

    private void registerCustomTooltips() {
        ClientTooltipComponentCallback.EVENT.register(d -> {
            if (d instanceof AbstractUpgradeItem.UpgradeDestinationTooltipComponent(var destinations, var helpText)) {
                return new CompositeClientTooltipComponent(List.of(
                    new UpgradeDestinationClientTooltipComponent(destinations),
                    HelpClientTooltipComponent.create(helpText)
                ));
            }
            return null;
        });
        ClientTooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof HelpTooltipComponent(Component text)) {
                return HelpClientTooltipComponent.create(text);
            }
            return null;
        });
        ClientTooltipComponentCallback.EVENT.register(d -> {
            if (d instanceof RegulatorUpgradeItem.RegulatorTooltipComponent(var destinations, var helpText, var r)) {
                return createRegulatorUpgradeClientTooltipComponent(
                    destinations,
                    r,
                    helpText
                );
            }
            return null;
        });
        ClientTooltipComponentCallback.EVENT.register(data -> switch (data) {
            case PatternItem.CraftingPatternTooltipComponent component -> PatternTooltipCache.getComponent(component);
            case PatternItem.ProcessingPatternTooltipComponent component -> PatternTooltipCache.getComponent(component);
            case PatternItem.StonecutterPatternTooltipComponent component ->
                PatternTooltipCache.getComponent(component);
            case PatternItem.SmithingTablePatternTooltipComponent component ->
                PatternTooltipCache.getComponent(component);
            default -> null;
        });
    }

    private void registerKeyMappings() {
        final KeyMapping.Category category = KeyMapping.Category.register(createIdentifier("keymappings"));

        KeyMappings.INSTANCE.setFocusSearchBar(KeyMappingHelper.registerKeyMapping(new KeyMapping(
            ContentNames.FOCUS_SEARCH_BAR_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            category
        )));
        KeyMappings.INSTANCE.setOpenWirelessGrid(KeyMappingHelper.registerKeyMapping(new KeyMapping(
            ContentNames.OPEN_WIRELESS_GRID_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            category
        )));
        KeyMappings.INSTANCE.setOpenPortableGrid(KeyMappingHelper.registerKeyMapping(new KeyMapping(
            ContentNames.OPEN_PORTABLE_GRID_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            category
        )));
        KeyMappings.INSTANCE.setOpenWirelessAutocraftingMonitor(KeyMappingHelper.registerKeyMapping(new KeyMapping(
            ContentNames.OPEN_WIRELESS_AUTOCRAFTING_MONITOR_TRANSLATION_KEY,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            category
        )));
        ClientTickEvents.END_CLIENT_TICK.register(client -> handleInputEvents());
    }

    private void registerItemProperties() {
        RangeSelectItemModelProperties.ID_MAPPER.put(
            ControllerEnergyLevelItemModelProperty.NAME,
            ControllerEnergyLevelItemModelProperty.MAP_CODEC
        );
        ConditionalItemModelProperties.ID_MAPPER.put(
            ActiveNetworkCardItemModelProperty.NAME,
            ActiveNetworkCardItemModelProperty.MAP_CODEC
        );
        ConditionalItemModelProperties.ID_MAPPER.put(
            ActiveConfigurationCardItemModelProperty.NAME,
            ActiveConfigurationCardItemModelProperty.MAP_CODEC
        );
        ConditionalItemModelProperties.ID_MAPPER.put(
            ActiveSecurityCardItemModelProperty.NAME,
            ActiveSecurityCardItemModelProperty.MAP_CODEC
        );
        ConditionalItemModelProperties.ID_MAPPER.put(
            NetworkBoundItemModelProperty.NAME,
            NetworkBoundItemModelProperty.MAP_CODEC
        );
        SelectItemModelProperties.ID_MAPPER.put(
            PatternTypeItemModelProperty.NAME,
            PatternTypeItemModelProperty.PROPERTY_TYPE
        );
    }

    private void registerRecipeSync() {
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) ->
            Platform.INSTANCE.setClientRecipeProvider(new SynchronizedRecipesRecipeProvider(recipes)));
    }
}

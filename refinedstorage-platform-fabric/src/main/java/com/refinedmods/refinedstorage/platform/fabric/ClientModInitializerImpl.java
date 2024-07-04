package com.refinedmods.refinedstorage.platform.fabric;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage.platform.common.configurationcard.ConfigurationCardItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage.platform.common.controller.ControllerItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.networking.NetworkCardItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage.platform.common.support.network.bounditem.NetworkBoundItemItemPropertyFunction;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketHandler;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.EnergyInfoPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.GridClearPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.GridUpdatePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.NetworkTransmitterStatusPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.NoPermissionPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.ResourceSlotUpdatePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.WirelessTransmitterRangePacket;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.support.tooltip.ResourceClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage.platform.fabric.mixin.ItemPropertiesAccessor;
import com.refinedmods.refinedstorage.platform.fabric.storage.diskdrive.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.platform.fabric.storage.diskdrive.DiskDriveUnbakedModel;
import com.refinedmods.refinedstorage.platform.fabric.storage.diskinterface.DiskInterfaceBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.platform.fabric.storage.diskinterface.DiskInterfaceUnbakedModel;
import com.refinedmods.refinedstorage.platform.fabric.storage.portablegrid.PortableGridBlockEntityRendererImpl;
import com.refinedmods.refinedstorage.platform.fabric.storage.portablegrid.PortableGridUnbakedModel;
import com.refinedmods.refinedstorage.platform.fabric.support.render.EmissiveModelRegistry;
import com.refinedmods.refinedstorage.platform.fabric.support.render.QuadRotators;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslationKey;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    private static final String BLOCK_PREFIX = "block";
    private static final String ITEM_PREFIX = "item";

    @Override
    public void onInitializeClient() {
        setRenderLayers();
        registerEmissiveModels();
        registerPacketHandlers();
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
        registerKeyBindings();
        registerModelPredicates();
        registerResourceRendering();
        registerAlternativeGridHints();
        registerItemProperties();
    }

    private void setRenderLayers() {
        setCutout(Blocks.INSTANCE.getImporter());
        setCutout(Blocks.INSTANCE.getExporter());
        setCutout(Blocks.INSTANCE.getExternalStorage());
        setCutout(Blocks.INSTANCE.getCable());
        setCutout(Blocks.INSTANCE.getGrid());
        setCutout(Blocks.INSTANCE.getCraftingGrid());
        setCutout(Blocks.INSTANCE.getController());
        setCutout(Blocks.INSTANCE.getCreativeController());
        setCutout(Blocks.INSTANCE.getDetector());
        setCutout(Blocks.INSTANCE.getConstructor());
        setCutout(Blocks.INSTANCE.getDestructor());
        setCutout(Blocks.INSTANCE.getWirelessTransmitter());
        setCutout(Blocks.INSTANCE.getNetworkReceiver());
        setCutout(Blocks.INSTANCE.getNetworkTransmitter());
        setCutout(Blocks.INSTANCE.getPortableGrid());
        setCutout(Blocks.INSTANCE.getCreativePortableGrid());
        setCutout(Blocks.INSTANCE.getSecurityManager());
        setCutout(Blocks.INSTANCE.getRelay());
        setCutout(Blocks.INSTANCE.getDiskInterface());
    }

    private void setCutout(final BlockColorMap<?, ?> blockMap) {
        blockMap.values().forEach(this::setCutout);
    }

    private void setCutout(final Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
    }

    private void registerEmissiveModels() {
        registerColoredEmissiveModels(Blocks.INSTANCE.getController(), "controller");
        registerColoredEmissiveModels(Blocks.INSTANCE.getCreativeController(), "controller");
        registerColoredEmissiveModels(Blocks.INSTANCE.getGrid(), "grid");
        registerColoredEmissiveModels(Blocks.INSTANCE.getCraftingGrid(), "crafting_grid");
        registerColoredEmissiveModels(Blocks.INSTANCE.getDetector(), "detector");
        registerConstructorDestructorEmissiveModels(Blocks.INSTANCE.getConstructor(), "constructor");
        registerConstructorDestructorEmissiveModels(Blocks.INSTANCE.getDestructor(), "destructor");
        registerColoredEmissiveModels(Blocks.INSTANCE.getWirelessTransmitter(), "wireless_transmitter");
        registerColoredEmissiveModels(Blocks.INSTANCE.getNetworkReceiver(), "network_receiver");
        Blocks.INSTANCE.getNetworkTransmitter().forEach(
            (color, id, block) -> registerEmissiveNetworkTransmitterModels(color, id)
        );
        Blocks.INSTANCE.getSecurityManager().forEach(
            (color, id, block) -> registerEmissiveSecurityManagerModels(color, id)
        );
        Blocks.INSTANCE.getRelay().forEach(
            (color, id, block) -> registerEmissiveRelayModels(color, id)
        );
    }

    private void registerColoredEmissiveModels(final BlockColorMap<?, ?> blockMap,
                                               final String blockDirectory) {
        blockMap.forEach((color, id, block) -> {
            final ResourceLocation blockModelLocation = createIdentifier(
                BLOCK_PREFIX + "/" + blockDirectory + "/" + color.getName()
            );
            final ResourceLocation spriteLocation = createIdentifier(
                BLOCK_PREFIX + "/" + blockDirectory + "/cutouts/" + color.getName()
            );
            EmissiveModelRegistry.INSTANCE.register(blockModelLocation, spriteLocation);
            EmissiveModelRegistry.INSTANCE.register(id.withPath(ITEM_PREFIX + "/" + id.getPath()), spriteLocation);
        });
    }

    private void registerConstructorDestructorEmissiveModels(final BlockColorMap<?, ?> blockMap,
                                                             final String blockDirectory) {
        blockMap.forEach((color, id, block) -> {
            final ResourceLocation blockModelLocation = createIdentifier(
                BLOCK_PREFIX + "/" + blockDirectory + "/active"
            );
            final ResourceLocation spriteLocation = createIdentifier(
                BLOCK_PREFIX + "/" + blockDirectory + "/cutouts/active"
            );
            EmissiveModelRegistry.INSTANCE.register(blockModelLocation, spriteLocation);
            EmissiveModelRegistry.INSTANCE.register(createIdentifier(ITEM_PREFIX + "/" + id.getPath()), spriteLocation);
        });
    }

    private void registerEmissiveNetworkTransmitterModels(final DyeColor color, final ResourceLocation id) {
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(BLOCK_PREFIX + "/network_transmitter/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/network_transmitter/cutouts/" + color.getName())
        );
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(BLOCK_PREFIX + "/network_transmitter/error"),
            createIdentifier(BLOCK_PREFIX + "/network_transmitter/cutouts/error")
        );
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(ITEM_PREFIX + "/" + id.getPath()),
            createIdentifier(BLOCK_PREFIX + "/network_transmitter/cutouts/" + color.getName())
        );
    }

    private void registerEmissiveSecurityManagerModels(final DyeColor color, final ResourceLocation id) {
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(BLOCK_PREFIX + "/security_manager/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/back/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/front/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/left/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/right/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/top/" + color.getName())
        );
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(ITEM_PREFIX + "/" + id.getPath()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/back/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/front/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/left/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/right/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/security_manager/cutouts/top/" + color.getName())
        );
    }

    private void registerEmissiveRelayModels(final DyeColor color, final ResourceLocation id) {
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(BLOCK_PREFIX + "/relay/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/relay/cutouts/in/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/relay/cutouts/out/" + color.getName())
        );
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier(ITEM_PREFIX + "/" + id.getPath()),
            createIdentifier(BLOCK_PREFIX + "/relay/cutouts/in/" + color.getName()),
            createIdentifier(BLOCK_PREFIX + "/relay/cutouts/out/" + color.getName())
        );
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
            EnergyInfoPacket.PACKET_TYPE,
            wrapHandler(EnergyInfoPacket::handle)
        );
        ClientPlayNetworking.registerGlobalReceiver(
            WirelessTransmitterRangePacket.PACKET_TYPE,
            wrapHandler(WirelessTransmitterRangePacket::handle)
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
            NoPermissionPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> NoPermissionPacket.handle(packet))
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
            ctx -> new DiskDriveBlockEntityRendererImpl<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getStorageMonitor(),
            ctx -> new StorageMonitorBlockEntityRenderer()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getPortableGrid(),
            ctx -> new PortableGridBlockEntityRendererImpl<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getCreativePortableGrid(),
            ctx -> new PortableGridBlockEntityRendererImpl<>()
        );
        BlockEntityRenderers.register(
            BlockEntities.INSTANCE.getDiskInterface(),
            ctx -> new DiskInterfaceBlockEntityRendererImpl<>()
        );
    }

    private void registerCustomModels() {
        registerDiskModels();
        final QuadRotators quadRotators = new QuadRotators();
        ModelLoadingPlugin.register(pluginContext -> {
            registerCustomDiskDriveModels(pluginContext, quadRotators);
            registerCustomDiskInterfaceModels(pluginContext, quadRotators);
            registerCustomPortableGridModels(pluginContext, quadRotators);
        });
    }

    private void registerCustomDiskInterfaceModels(final ModelLoadingPlugin.Context pluginContext,
                                                   final QuadRotators quadRotators) {
        pluginContext.resolveModel().register(context -> {
            if (context.id().getNamespace().equals(IdentifierUtil.MOD_ID)
                && context.id().getPath().startsWith(ITEM_PREFIX + "/")
                && context.id().getPath().endsWith("disk_interface")) {
                final boolean isDefault = !context.id().getPath().endsWith("_disk_interface");
                final DyeColor color = isDefault
                    ? Blocks.INSTANCE.getDiskInterface().getDefault().getColor()
                    : DyeColor.byName(context.id().getPath().replace("_disk_interface", "")
                    .replace(ITEM_PREFIX + "/", ""), Blocks.INSTANCE.getDiskInterface().getDefault().getColor());
                return new DiskInterfaceUnbakedModel(quadRotators, color);
            }
            if (context.id().getNamespace().equals(IdentifierUtil.MOD_ID)
                && context.id().getPath().startsWith(BLOCK_PREFIX + "/disk_interface/")
                && !context.id().getPath().startsWith(BLOCK_PREFIX + "/disk_interface/base_")
                && !context.id().getPath().equals(BLOCK_PREFIX + "/disk_interface/inactive")) {
                final DyeColor color = DyeColor.byName(
                    context.id().getPath().replace(BLOCK_PREFIX + "/disk_interface/", ""),
                    Blocks.INSTANCE.getDiskInterface().getDefault().getColor()
                );
                return new DiskInterfaceUnbakedModel(quadRotators, color);
            }
            return null;
        });
    }

    private void registerCustomPortableGridModels(final ModelLoadingPlugin.Context pluginContext,
                                                  final QuadRotators quadRotators) {
        final ResourceLocation portableGridIdentifier = createIdentifier(BLOCK_PREFIX + "/portable_grid");
        final ResourceLocation portableGridIdentifierItem = createIdentifier(ITEM_PREFIX + "/portable_grid");
        final ResourceLocation creativePortableGridIdentifier = createIdentifier(
            BLOCK_PREFIX + "/creative_portable_grid"
        );
        final ResourceLocation creativePortableGridIdentifierItem = createIdentifier(
            ITEM_PREFIX + "/creative_portable_grid"
        );
        pluginContext.resolveModel().register(context -> {
            if (context.id().equals(portableGridIdentifier)
                || context.id().equals(portableGridIdentifierItem)
                || context.id().equals(creativePortableGridIdentifier)
                || context.id().equals(creativePortableGridIdentifierItem)) {
                return new PortableGridUnbakedModel(quadRotators);
            }
            return null;
        });
    }

    private void registerCustomDiskDriveModels(final ModelLoadingPlugin.Context pluginContext,
                                               final QuadRotators quadRotators) {
        final ResourceLocation diskDriveIdentifier = createIdentifier(BLOCK_PREFIX + "/disk_drive");
        final ResourceLocation diskDriveIdentifierItem = createIdentifier(ITEM_PREFIX + "/disk_drive");
        pluginContext.resolveModel().register(context -> {
            if (context.id().equals(diskDriveIdentifier) || context.id().equals(diskDriveIdentifierItem)) {
                return new DiskDriveUnbakedModel(quadRotators);
            }
            return null;
        });
    }

    private void registerCustomTooltips() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AbstractUpgradeItem.UpgradeDestinationTooltipComponent component) {
                return new UpgradeDestinationClientTooltipComponent(component.destinations());
            }
            if (data instanceof HelpTooltipComponent component) {
                return HelpClientTooltipComponent.create(component.text());
            }
            if (data instanceof RegulatorUpgradeItem.RegulatorTooltipComponent component) {
                final ClientTooltipComponent help = HelpClientTooltipComponent.create(component.helpText());
                return component.configuredResource() == null
                    ? help
                    : createRegulatorUpgradeClientTooltipComponent(component.configuredResource(), help);
            }
            return null;
        });
    }

    private CompositeClientTooltipComponent createRegulatorUpgradeClientTooltipComponent(
        final ResourceAmount configuredResource,
        final ClientTooltipComponent help
    ) {
        return new CompositeClientTooltipComponent(List.of(
            new ResourceClientTooltipComponent(configuredResource),
            help
        ));
    }

    private void registerKeyBindings() {
        KeyMappings.INSTANCE.setFocusSearchBar(KeyBindingHelper.registerKeyBinding(new KeyMapping(
            createTranslationKey("key", "focus_search_bar"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            ContentNames.MOD_TRANSLATION_KEY
        )));
        KeyMappings.INSTANCE.setOpenWirelessGrid(KeyBindingHelper.registerKeyBinding(new KeyMapping(
            createTranslationKey("key", "open_wireless_grid"),
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            ContentNames.MOD_TRANSLATION_KEY
        )));
        ClientTickEvents.END_CLIENT_TICK.register(client -> handleInputEvents());
    }

    private void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemPropertiesAccessor.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerItemPropertyFunction()
        ));
    }

    private void registerItemProperties() {
        ItemProperties.register(
            Items.INSTANCE.getWirelessGrid(),
            NetworkBoundItemItemPropertyFunction.NAME,
            new NetworkBoundItemItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getCreativeWirelessGrid(),
            NetworkBoundItemItemPropertyFunction.NAME,
            new NetworkBoundItemItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getConfigurationCard(),
            ConfigurationCardItemPropertyFunction.NAME,
            new ConfigurationCardItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getNetworkCard(),
            NetworkCardItemPropertyFunction.NAME,
            new NetworkCardItemPropertyFunction()
        );
        ItemProperties.register(
            Items.INSTANCE.getSecurityCard(),
            SecurityCardItemPropertyFunction.NAME,
            new SecurityCardItemPropertyFunction()
        );
    }
}

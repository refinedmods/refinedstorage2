package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.upgrade.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage2.platform.common.configurationcard.ConfigurationCardItemPropertyFunction;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.KeyMappings;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerModelPredicateProvider;
import com.refinedmods.refinedstorage2.platform.common.storagemonitor.StorageMonitorBlockEntityRenderer;
import com.refinedmods.refinedstorage2.platform.common.support.networkbounditem.NetworkBoundItemItemPropertyFunction;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.CompositeClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.HelpClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.ResourceClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeDestinationClientTooltipComponent;
import com.refinedmods.refinedstorage2.platform.fabric.mixin.ItemPropertiesAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ControllerEnergyInfoPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridClearPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.GridUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.ResourceSlotUpdatePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.s2c.WirelessTransmitterRangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.recipemod.rei.RefinedStorageREIClientPlugin;
import com.refinedmods.refinedstorage2.platform.fabric.recipemod.rei.ReiGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.fabric.recipemod.rei.ReiProxy;
import com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive.DiskDriveBlockEntityRendererImpl;
import com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive.DiskDriveUnbakedModel;
import com.refinedmods.refinedstorage2.platform.fabric.support.render.model.EmissiveModelRegistry;

import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslationKey;

public class ClientModInitializerImpl extends AbstractClientModInitializer implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientModInitializerImpl.class);
    private static final String KEY_BINDINGS_TRANSLATION_KEY = createTranslationKey("category", "key_bindings");

    @Override
    public void onInitializeClient() {
        setRenderLayers();
        registerEmissiveModels();
        registerPackets();
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
        registerGridSynchronizers();
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
    }

    private void setCutout(final BlockColorMap<?> blockMap) {
        blockMap.values().forEach(this::setCutout);
    }

    private void setCutout(final Block block) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.cutout());
    }

    private void registerEmissiveModels() {
        Blocks.INSTANCE.getController().forEach((color, id, block) -> {
            registerEmissiveControllerModels(color);
            registerEmissiveControllerItemModels(color, id);
        });
        Blocks.INSTANCE.getCreativeController().forEach(
            (color, id, block) -> registerEmissiveControllerItemModels(color, id)
        );
        Blocks.INSTANCE.getGrid().forEach(
            (color, id, block) -> registerEmissiveGridModels(color, id)
        );
        Blocks.INSTANCE.getCraftingGrid().forEach(
            (color, id, block) -> registerEmissiveCraftingGridModels(color, id)
        );
        Blocks.INSTANCE.getDetector().forEach(
            (color, id, block) -> registerEmissiveDetectorModels(color, id)
        );
        Blocks.INSTANCE.getConstructor().forEach(
            (color, id, block) -> registerEmissiveConstructorModels(color, id)
        );
        Blocks.INSTANCE.getDestructor().forEach(
            (color, id, block) -> registerEmissiveDestructorModels(color, id)
        );
        Blocks.INSTANCE.getWirelessTransmitter().forEach(
            (color, id, block) -> registerEmissiveWirelessTransmitterModels(color, id)
        );
        Blocks.INSTANCE.getNetworkReceiver().forEach(
            (color, id, block) -> registerEmissiveNetworkReceiverModels(color, id)
        );
    }

    private void registerEmissiveControllerModels(final DyeColor color) {
        final ResourceLocation spriteLocation = createIdentifier("block/controller/cutouts/" + color.getName());
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/controller/" + color.getName()),
            spriteLocation
        );
    }

    private void registerEmissiveControllerItemModels(final DyeColor color, final ResourceLocation id) {
        final ResourceLocation spriteLocation = createIdentifier("block/controller/cutouts/" + color.getName());
        EmissiveModelRegistry.INSTANCE.register(id, spriteLocation);
    }

    private void registerEmissiveGridModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/grid/" + color.getName()),
            createIdentifier("block/grid/cutouts/" + color.getName())
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(id, createIdentifier("block/grid/cutouts/" + color.getName()));
    }

    private void registerEmissiveCraftingGridModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/crafting_grid/" + color.getName()),
            createIdentifier("block/crafting_grid/cutouts/" + color.getName())
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(id, createIdentifier("block/crafting_grid/cutouts/" + color.getName()));
    }

    private void registerEmissiveDetectorModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/detector/" + color.getName()),
            createIdentifier("block/detector/cutouts/" + color.getName())
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(id, createIdentifier("block/detector/cutouts/" + color.getName()));
    }

    private void registerEmissiveConstructorModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/constructor/" + color.getName()),
            createIdentifier("block/constructor/cutouts/active")
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(id, createIdentifier("block/constructor/cutouts/active"));
    }

    private void registerEmissiveDestructorModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/destructor/" + color.getName()),
            createIdentifier("block/destructor/cutouts/active")
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(id, createIdentifier("block/destructor/cutouts/active"));
    }

    private void registerEmissiveWirelessTransmitterModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/wireless_transmitter/" + color.getName()),
            createIdentifier("block/wireless_transmitter/cutouts/" + color.getName())
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(
            id,
            createIdentifier("block/wireless_transmitter/cutouts/" + color.getName())
        );
    }

    private void registerEmissiveNetworkReceiverModels(final DyeColor color, final ResourceLocation id) {
        // Block
        EmissiveModelRegistry.INSTANCE.register(
            createIdentifier("block/network_receiver/" + color.getName()),
            createIdentifier("block/network_receiver/cutouts/" + color.getName())
        );
        // Item
        EmissiveModelRegistry.INSTANCE.register(
            id,
            createIdentifier("block/network_receiver/cutouts/" + color.getName())
        );
    }

    private void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_RESPONSE, new StorageInfoResponsePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_UPDATE, new GridUpdatePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_CLEAR, new GridClearPacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.GRID_ACTIVE, new GridActivePacket());
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.CONTROLLER_ENERGY_INFO, new ControllerEnergyInfoPacket());
        ClientPlayNetworking.registerGlobalReceiver(
            PacketIds.WIRELESS_TRANSMITTER_RANGE,
            new WirelessTransmitterRangePacket()
        );
        ClientPlayNetworking.registerGlobalReceiver(PacketIds.RESOURCE_SLOT_UPDATE, new ResourceSlotUpdatePacket());
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
    }

    private void registerCustomModels() {
        final ResourceLocation diskDriveIdentifier = createIdentifier("block/disk_drive");
        final ResourceLocation diskDriveIdentifierItem = createIdentifier("item/disk_drive");

        ModelLoadingPlugin.register(pluginContext -> pluginContext.resolveModel().register(context -> {
            if (context.id().equals(diskDriveIdentifier) || context.id().equals(diskDriveIdentifierItem)) {
                return new DiskDriveUnbakedModel();
            }
            return null;
        }));
    }

    private void registerCustomTooltips() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof AbstractUpgradeItem.UpgradeDestinationTooltipComponent component) {
                return new UpgradeDestinationClientTooltipComponent(component.destinations());
            }
            if (data instanceof HelpTooltipComponent component) {
                return HelpClientTooltipComponent.create(component.text());
            }
            if (data instanceof RegulatorUpgradeItem.RegulatorTooltipComponent<?> component) {
                final ClientTooltipComponent help = HelpClientTooltipComponent.create(component.helpText());
                return component.filteredResource() == null
                    ? help
                    : createRegulatorUpgradeClientTooltipComponent(component.filteredResource(), help);
            }
            return null;
        });
    }

    private <T> CompositeClientTooltipComponent createRegulatorUpgradeClientTooltipComponent(
        final ResourceAmountTemplate<T> filteredResource,
        final ClientTooltipComponent help
    ) {
        return new CompositeClientTooltipComponent(List.of(
            new ResourceClientTooltipComponent<>(filteredResource),
            help
        ));
    }

    private void registerKeyBindings() {
        KeyMappings.INSTANCE.setFocusSearchBar(KeyBindingHelper.registerKeyBinding(new KeyMapping(
            createTranslationKey("key", "focus_search_bar"),
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            KEY_BINDINGS_TRANSLATION_KEY
        )));
        KeyMappings.INSTANCE.setOpenWirelessGrid(KeyBindingHelper.registerKeyBinding(new KeyMapping(
            createTranslationKey("key", "open_wireless_grid"),
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            KEY_BINDINGS_TRANSLATION_KEY
        )));
        ClientTickEvents.END_CLIENT_TICK.register(client -> handleInputEvents());
    }

    private void registerModelPredicates() {
        Items.INSTANCE.getControllers().forEach(controllerBlockItem -> ItemPropertiesAccessor.register(
            controllerBlockItem.get(),
            createIdentifier("stored_in_controller"),
            new ControllerModelPredicateProvider()
        ));
    }

    private void registerGridSynchronizers() {
        final FabricLoader loader = FabricLoader.getInstance();
        if (loader.isModLoaded("roughlyenoughitems")) {
            registerReiGridSynchronizers();
        }
    }

    private void registerReiGridSynchronizers() {
        LOGGER.info("Enabling REI grid synchronizers");
        // This is so the ingredient converters are only registered once
        // see https://github.com/refinedmods/refinedstorage2/pull/302#discussion_r1070015672
        RefinedStorageREIClientPlugin.registerIngredientConverters();
        final ReiProxy reiProxy = new ReiProxy();
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("rei"),
            new ReiGridSynchronizer(reiProxy, false)
        );
        PlatformApi.INSTANCE.getGridSynchronizerRegistry().register(
            createIdentifier("rei_two_way"),
            new ReiGridSynchronizer(reiProxy, true)
        );
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
    }
}

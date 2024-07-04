package com.refinedmods.refinedstorage.platform.fabric;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.RefinedStoragePlugin;
import com.refinedmods.refinedstorage.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage.platform.common.PlatformProxy;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage.platform.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.platform.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.platform.common.content.Items;
import com.refinedmods.refinedstorage.platform.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.platform.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.platform.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.platform.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage.platform.common.iface.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.common.security.FallbackSecurityCardItem;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardItem;
import com.refinedmods.refinedstorage.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.platform.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.platform.common.storage.portablegrid.PortableGridBlockItem;
import com.refinedmods.refinedstorage.platform.common.storage.portablegrid.PortableGridType;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketHandler;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.CraftingGridClearPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.CraftingGridRecipeTransferPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.ResourceFilterSlotChangePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.ResourceSlotAmountChangePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.ResourceSlotChangePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.SecurityCardBoundPlayerPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.SecurityCardPermissionPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.SecurityCardResetPermissionPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.SingleAmountChangePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.UseNetworkBoundItemPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.EnergyInfoPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.GridClearPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.GridUpdatePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.NetworkTransmitterStatusPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.NoPermissionPacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.ResourceSlotUpdatePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.WirelessTransmitterRangePacket;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.util.ServerEventQueue;
import com.refinedmods.refinedstorage.platform.fabric.exporter.FabricStorageExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.fabric.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.fabric.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.fabric.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.fabric.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.fabric.importer.FabricStorageImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.fabric.security.NetworkNodeBreakSecurityEventListener;
import com.refinedmods.refinedstorage.platform.fabric.storage.diskdrive.FabricDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.platform.fabric.storage.diskinterface.FabricDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.platform.fabric.storage.externalstorage.FabricStoragePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.fabric.storage.portablegrid.FabricPortableGridBlockEntity;
import com.refinedmods.refinedstorage.platform.fabric.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage.platform.fabric.support.resource.ResourceContainerFluidStorageAdapter;
import com.refinedmods.refinedstorage.platform.fabric.support.resource.VariantUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.CREATIVE_PORTABLE_GRID;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.CREATIVE_WIRELESS_GRID;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.FALLBACK_SECURITY_CARD;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.PORTABLE_GRID;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.REGULATOR_UPGRADE;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.SECURITY_CARD;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.WIRELESS_GRID;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public class ModInitializerImpl extends AbstractModInitializer implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModInitializerImpl.class);
    private static final String PLUGIN_ENTRYPOINT_KEY = "refinedstorage_plugin";

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);

        PlatformProxy.loadPlatform(new PlatformImpl());
        initializePlatformApi();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerContent();
        registerPackets();
        registerPacketHandlers();
        registerSounds(new DirectRegistryCallback<>(BuiltInRegistries.SOUND_EVENT));
        registerRecipeSerializers(new DirectRegistryCallback<>(BuiltInRegistries.RECIPE_SERIALIZER));
        registerSidedHandlers();
        registerTickHandler();
        registerWrenchingEvent();
        registerSecurityBlockBreakEvent();

        final List<RefinedStoragePlugin> pluginEntrypoints = FabricLoader.getInstance()
            .getEntrypoints(PLUGIN_ENTRYPOINT_KEY, RefinedStoragePlugin.class);
        LOGGER.debug("Loading {} Refined Storage plugin entrypoints.", pluginEntrypoints.size());
        pluginEntrypoints.forEach(plugin -> plugin.onPlatformApiAvailable(PlatformApi.INSTANCE));

        LOGGER.debug("Refined Storage has loaded.");
    }

    private void registerAdditionalGridInsertionStrategyFactories() {
        PlatformApi.INSTANCE.addGridInsertionStrategyFactory(FluidGridInsertionStrategy::new);
    }

    private void registerGridExtractionStrategyFactories() {
        PlatformApi.INSTANCE.addGridExtractionStrategyFactory(ItemGridExtractionStrategy::new);
        PlatformApi.INSTANCE.addGridExtractionStrategyFactory(FluidGridExtractionStrategy::new);
    }

    private void registerGridScrollingStrategyFactories() {
        PlatformApi.INSTANCE.addGridScrollingStrategyFactory(ItemGridScrollingStrategy::new);
    }

    private void registerImporterTransferStrategyFactories() {
        PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new FabricStorageImporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                VariantUtil::ofItemVariant,
                resource -> resource instanceof ItemResource itemResource
                    ? VariantUtil.toItemVariant(itemResource) : null,
                1
            )
        );
        PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FabricStorageImporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                VariantUtil::ofFluidVariant,
                resource -> resource instanceof FluidResource fluidResource
                    ? VariantUtil.toFluidVariant(fluidResource) : null,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExporterTransferStrategyFactories() {
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new FabricStorageExporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                resource -> resource instanceof ItemResource itemResource
                    ? VariantUtil.toItemVariant(itemResource) : null,
                1
            )
        );
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FabricStorageExporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                resource -> resource instanceof FluidResource fluidResource
                    ? VariantUtil.toFluidVariant(fluidResource) : null,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExternalStorageProviderFactories() {
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(new InterfacePlatformExternalStorageProviderFactory());
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            new FabricStoragePlatformExternalStorageProviderFactory<>(
                ItemStorage.SIDED,
                VariantUtil::ofItemVariant,
                resource -> resource instanceof ItemResource itemResource
                    ? VariantUtil.toItemVariant(itemResource) : null
            ));
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            new FabricStoragePlatformExternalStorageProviderFactory<>(
                FluidStorage.SIDED,
                VariantUtil::ofFluidVariant,
                resource -> resource instanceof FluidResource fluidResource
                    ? VariantUtil.toFluidVariant(fluidResource) : null
            ));
    }

    private void registerContent() {
        registerBlocks(
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK),
            FabricDiskDriveBlockEntity::new,
            (pos, state) -> new FabricPortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
            (pos, state) -> new FabricPortableGridBlockEntity(PortableGridType.CREATIVE, pos, state),
            FabricDiskInterfaceBlockEntity::new
        );
        final DirectRegistryCallback<Item> itemRegistryCallback = new DirectRegistryCallback<>(BuiltInRegistries.ITEM);
        registerItems(itemRegistryCallback);
        registerCustomItems(itemRegistryCallback);
        registerUpgradeMappings();
        registerCreativeModeTab();
        registerBlockEntities(
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK_ENTITY_TYPE),
            new BlockEntityTypeFactory() {
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntitySupplier<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            },
            FabricDiskDriveBlockEntity::new,
            (pos, state) -> new FabricPortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
            (pos, state) -> new FabricPortableGridBlockEntity(PortableGridType.CREATIVE, pos, state),
            FabricDiskInterfaceBlockEntity::new
        );
        registerMenus(new DirectRegistryCallback<>(BuiltInRegistries.MENU), new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return new MenuType<>(supplier::create, FeatureFlags.DEFAULT_FLAGS);
            }
        }, new ExtendedMenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu, D> MenuType<T> create(final MenuSupplier<T, D> supplier,
                                                                           final StreamCodec<RegistryFriendlyByteBuf, D>
                                                                               streamCodec) {
                return new ExtendedScreenHandlerType<>(supplier::create, streamCodec);
            }
        });
        registerLootFunctions(new DirectRegistryCallback<>(BuiltInRegistries.LOOT_FUNCTION_TYPE));
        registerDataComponents(new DirectRegistryCallback<>(BuiltInRegistries.DATA_COMPONENT_TYPE));
    }

    private void registerCustomItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setRegulatorUpgrade(callback.register(REGULATOR_UPGRADE, () -> new RegulatorUpgradeItem(
            PlatformApi.INSTANCE.getUpgradeRegistry()
        ) {
            @Override
            public boolean allowComponentsUpdateAnimation(final Player player,
                                                          final InteractionHand hand,
                                                          final ItemStack oldStack,
                                                          final ItemStack newStack) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setWirelessGrid(callback.register(WIRELESS_GRID, () -> new WirelessGridItem() {
            @Override
            public boolean allowComponentsUpdateAnimation(final Player player,
                                                          final InteractionHand hand,
                                                          final ItemStack oldStack,
                                                          final ItemStack newStack) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setCreativeWirelessGrid(callback.register(
            CREATIVE_WIRELESS_GRID,
            () -> new WirelessGridItem() {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                              final InteractionHand hand,
                                                              final ItemStack oldStack,
                                                              final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setPortableGrid(callback.register(PORTABLE_GRID, () -> new PortableGridBlockItem(
            Blocks.INSTANCE.getPortableGrid(), PortableGridType.NORMAL
        ) {
            @Override
            public boolean allowComponentsUpdateAnimation(final Player player,
                                                          final InteractionHand hand,
                                                          final ItemStack oldStack,
                                                          final ItemStack newStack) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setCreativePortableGrid(callback.register(
            CREATIVE_PORTABLE_GRID,
            () -> new PortableGridBlockItem(Blocks.INSTANCE.getCreativePortableGrid(), PortableGridType.CREATIVE) {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                              final InteractionHand hand,
                                                              final ItemStack oldStack,
                                                              final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setSecurityCard(callback.register(SECURITY_CARD, () -> new SecurityCardItem() {
            @Override
            public boolean allowComponentsUpdateAnimation(final Player player,
                                                          final InteractionHand hand,
                                                          final ItemStack oldStack,
                                                          final ItemStack newStack) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setFallbackSecurityCard(callback.register(
            FALLBACK_SECURITY_CARD,
            () -> new FallbackSecurityCardItem() {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                              final InteractionHand hand,
                                                              final ItemStack oldStack,
                                                              final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
    }

    private void registerCreativeModeTab() {
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            createIdentifier("general"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(ContentNames.MOD)
                .icon(() -> new ItemStack(Blocks.INSTANCE.getCreativeController().getDefault()))
                .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
                .build()
        );
    }

    private void registerPackets() {
        registerServerToClientPackets();
        registerClientToServerPackets();
    }

    private void registerServerToClientPackets() {
        PayloadTypeRegistry.playS2C().register(EnergyInfoPacket.PACKET_TYPE, EnergyInfoPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(
            WirelessTransmitterRangePacket.PACKET_TYPE,
            WirelessTransmitterRangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(GridActivePacket.PACKET_TYPE, GridActivePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(GridClearPacket.PACKET_TYPE, GridClearPacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(GridUpdatePacket.PACKET_TYPE, GridUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(
            NetworkTransmitterStatusPacket.PACKET_TYPE,
            NetworkTransmitterStatusPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(
            NoPermissionPacket.PACKET_TYPE,
            NoPermissionPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(
            ResourceSlotUpdatePacket.PACKET_TYPE,
            ResourceSlotUpdatePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(
            StorageInfoResponsePacket.PACKET_TYPE,
            StorageInfoResponsePacket.STREAM_CODEC
        );
    }

    private void registerClientToServerPackets() {
        PayloadTypeRegistry.playC2S().register(
            CraftingGridClearPacket.PACKET_TYPE,
            CraftingGridClearPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            CraftingGridRecipeTransferPacket.PACKET_TYPE,
            CraftingGridRecipeTransferPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(GridExtractPacket.PACKET_TYPE, GridExtractPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(GridInsertPacket.PACKET_TYPE, GridInsertPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(GridScrollPacket.PACKET_TYPE, GridScrollPacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PropertyChangePacket.PACKET_TYPE, PropertyChangePacket.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(
            ResourceFilterSlotChangePacket.PACKET_TYPE,
            ResourceFilterSlotChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            ResourceSlotAmountChangePacket.PACKET_TYPE,
            ResourceSlotAmountChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            ResourceSlotChangePacket.PACKET_TYPE,
            ResourceSlotChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            SecurityCardBoundPlayerPacket.PACKET_TYPE,
            SecurityCardBoundPlayerPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            SecurityCardPermissionPacket.PACKET_TYPE,
            SecurityCardPermissionPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            SecurityCardResetPermissionPacket.PACKET_TYPE,
            SecurityCardResetPermissionPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            SingleAmountChangePacket.PACKET_TYPE,
            SingleAmountChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            StorageInfoRequestPacket.PACKET_TYPE,
            StorageInfoRequestPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
            UseNetworkBoundItemPacket.PACKET_TYPE,
            UseNetworkBoundItemPacket.STREAM_CODEC
        );
    }

    private void registerPacketHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(
            StorageInfoRequestPacket.PACKET_TYPE,
            wrapHandler(StorageInfoRequestPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            GridInsertPacket.PACKET_TYPE,
            wrapHandler(GridInsertPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            GridExtractPacket.PACKET_TYPE,
            wrapHandler(GridExtractPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            GridScrollPacket.PACKET_TYPE,
            wrapHandler(GridScrollPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            CraftingGridClearPacket.PACKET_TYPE,
            wrapHandler(CraftingGridClearPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            CraftingGridRecipeTransferPacket.PACKET_TYPE,
            wrapHandler(CraftingGridRecipeTransferPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PropertyChangePacket.PACKET_TYPE,
            wrapHandler(PropertyChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            ResourceSlotAmountChangePacket.PACKET_TYPE,
            wrapHandler(ResourceSlotAmountChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            ResourceFilterSlotChangePacket.PACKET_TYPE,
            wrapHandler(ResourceFilterSlotChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            ResourceSlotChangePacket.PACKET_TYPE,
            wrapHandler(ResourceSlotChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            SingleAmountChangePacket.PACKET_TYPE,
            wrapHandler(SingleAmountChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            UseNetworkBoundItemPacket.PACKET_TYPE,
            wrapHandler(UseNetworkBoundItemPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            SecurityCardPermissionPacket.PACKET_TYPE,
            wrapHandler(SecurityCardPermissionPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            SecurityCardResetPermissionPacket.PACKET_TYPE,
            wrapHandler(SecurityCardResetPermissionPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            SecurityCardBoundPlayerPacket.PACKET_TYPE,
            wrapHandler(SecurityCardBoundPlayerPacket::handle)
        );
    }

    private static <T extends CustomPacketPayload> ServerPlayNetworking.PlayPayloadHandler<T> wrapHandler(
        final PacketHandler<T> handler
    ) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    private void registerSidedHandlers() {
        registerItemStorage(
            AbstractDiskDriveBlockEntity.class::isInstance,
            AbstractDiskDriveBlockEntity.class::cast,
            AbstractDiskDriveBlockEntity::getDiskInventory,
            BlockEntities.INSTANCE.getDiskDrive()
        );
        registerItemStorage(
            InterfaceBlockEntity.class::isInstance,
            InterfaceBlockEntity.class::cast,
            InterfaceBlockEntity::getExportedResourcesAsContainer,
            BlockEntities.INSTANCE.getInterface()
        );
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            final InventoryStorage storage = InventoryStorage.of(blockEntity.getDiskInventory(), context);
            final List<Storage<ItemVariant>> parts = new ArrayList<>();
            for (int i = 0; i < AbstractDiskInterfaceBlockEntity.AMOUNT_OF_DISKS; ++i) {
                final var slot = storage.getSlot(i);
                parts.add(i < 3 ? FilteringStorage.insertOnlyOf(slot) : FilteringStorage.extractOnlyOf(slot));
            }
            return new CombinedStorage<>(parts);
        }, BlockEntities.INSTANCE.getDiskInterface());
        FluidStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new ResourceContainerFluidStorageAdapter(blockEntity.getExportedResources()),
            BlockEntities.INSTANCE.getInterface()
        );
        registerEnergyBlockEntityProviders();
        registerEnergyItemProviders();
    }

    private <T extends BlockEntity> void registerItemStorage(final Predicate<BlockEntity> test,
                                                             final Function<BlockEntity, T> caster,
                                                             final Function<T, Container> containerSupplier,
                                                             final BlockEntityType<?> type) {
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (test.test(blockEntity)) {
                final T casted = caster.apply(blockEntity);
                return InventoryStorage.of(containerSupplier.apply(casted), context);
            }
            return null;
        }, type);
    }

    private void registerEnergyBlockEntityProviders() {
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new EnergyStorageAdapter(blockEntity.getEnergyStorage()),
            BlockEntities.INSTANCE.getController()
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new EnergyStorageAdapter(blockEntity.getEnergyStorage()),
            BlockEntities.INSTANCE.getPortableGrid()
        );
    }

    private void registerEnergyItemProviders() {
        EnergyStorage.ITEM.registerForItems(
            (stack, context) -> new EnergyStorageAdapter(Items.INSTANCE.getWirelessGrid().createEnergyStorage(stack)),
            Items.INSTANCE.getWirelessGrid()
        );
        Items.INSTANCE.getControllers().forEach(controller -> EnergyStorage.ITEM.registerForItems(
            (stack, context) -> new EnergyStorageAdapter(controller.get().createEnergyStorage(stack)),
            controller.get()
        ));
        EnergyStorage.ITEM.registerForItems(
            (stack, context) -> new EnergyStorageAdapter(PortableGridBlockItem.createEnergyStorage(stack)),
            Items.INSTANCE.getPortableGrid()
        );
    }

    private void registerTickHandler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> ServerEventQueue.runQueuedActions());
    }

    private void registerWrenchingEvent() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            final BlockState state = level.getBlockState(hitResult.getBlockPos());
            if (!(state.getBlock() instanceof AbstractBaseBlock block)) {
                return InteractionResult.PASS;
            }
            return block.tryUseWrench(state, level, hitResult, player, hand)
                .or(() -> block.tryUpdateColor(state, level, hitResult.getBlockPos(), player, hand))
                .orElse(InteractionResult.PASS);
        });
    }

    private void registerSecurityBlockBreakEvent() {
        PlayerBlockBreakEvents.BEFORE.register(new NetworkNodeBreakSecurityEventListener());
    }
}

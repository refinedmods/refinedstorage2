package com.refinedmods.refinedstorage.fabric;

import com.refinedmods.refinedstorage.common.AbstractModInitializer;
import com.refinedmods.refinedstorage.common.PlatformProxy;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.WirelessAutocraftingMonitorItem;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockEntityProviders;
import com.refinedmods.refinedstorage.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.security.FallbackSecurityCardItem;
import com.refinedmods.refinedstorage.common.security.SecurityCardItem;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlockItem;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridType;
import com.refinedmods.refinedstorage.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage.common.support.packet.PacketHandler;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocrafterNameChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocraftingMonitorCancelAllPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocraftingMonitorCancelPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocraftingPreviewCancelRequestPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocraftingPreviewMaxAmountRequestPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocraftingPreviewRequestPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.AutocraftingRequestPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.CraftingGridClearPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.CraftingGridRecipeTransferPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.FilterSlotChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridAllowedAlternativesChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridClearPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridCraftingRecipeTransferPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridCreatePatternPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridProcessingRecipeTransferPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridSmithingTableRecipeTransferPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PatternGridStonecutterRecipeTransferPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.ResourceFilterSlotChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.ResourceSlotAmountChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.ResourceSlotChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.SecurityCardBoundPlayerPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.SecurityCardPermissionPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.SecurityCardResetPermissionPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.SingleAmountChangePacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage.common.support.packet.c2s.UseSlotReferencedItemPacket;
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
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.common.util.ServerListener;
import com.refinedmods.refinedstorage.fabric.api.RefinedStorageFabricApi;
import com.refinedmods.refinedstorage.fabric.api.RefinedStorageFabricApiProxy;
import com.refinedmods.refinedstorage.fabric.api.RefinedStoragePlugin;
import com.refinedmods.refinedstorage.fabric.autocrafting.StorageExternalPatternSinkStrategyFactoryImpl;
import com.refinedmods.refinedstorage.fabric.constructordestructor.FabricConstructorBlockEntity;
import com.refinedmods.refinedstorage.fabric.constructordestructor.FabricDestructorBlockEntity;
import com.refinedmods.refinedstorage.fabric.exporter.FabricExporterBlockEntity;
import com.refinedmods.refinedstorage.fabric.exporter.FabricStorageExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.fabric.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage.fabric.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage.fabric.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage.fabric.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage.fabric.grid.view.FabricFluidGridResourceRepositoryMapper;
import com.refinedmods.refinedstorage.fabric.grid.view.FabricItemGridResourceRepositoryMapper;
import com.refinedmods.refinedstorage.fabric.importer.FabricImporterBlockEntity;
import com.refinedmods.refinedstorage.fabric.importer.FabricStorageImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.fabric.networking.FabricCableBlockEntity;
import com.refinedmods.refinedstorage.fabric.security.NetworkNodeBreakSecurityEventListener;
import com.refinedmods.refinedstorage.fabric.storage.diskdrive.FabricDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.fabric.storage.diskinterface.FabricDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.fabric.storage.externalstorage.FabricExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.fabric.storage.externalstorage.FabricStorageExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.fabric.storage.portablegrid.FabricPortableGridBlockEntity;
import com.refinedmods.refinedstorage.fabric.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage.fabric.support.resource.ResourceContainerFluidStorageAdapter;
import com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

import static com.refinedmods.refinedstorage.common.content.ContentIds.CREATIVE_PORTABLE_GRID;
import static com.refinedmods.refinedstorage.common.content.ContentIds.CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR;
import static com.refinedmods.refinedstorage.common.content.ContentIds.CREATIVE_WIRELESS_GRID;
import static com.refinedmods.refinedstorage.common.content.ContentIds.FALLBACK_SECURITY_CARD;
import static com.refinedmods.refinedstorage.common.content.ContentIds.PORTABLE_GRID;
import static com.refinedmods.refinedstorage.common.content.ContentIds.REGULATOR_UPGRADE;
import static com.refinedmods.refinedstorage.common.content.ContentIds.SECURITY_CARD;
import static com.refinedmods.refinedstorage.common.content.ContentIds.WIRELESS_AUTOCRAFTING_MONITOR;
import static com.refinedmods.refinedstorage.common.content.ContentIds.WIRELESS_GRID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toFluidVariant;
import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toItemVariant;

public class ModInitializerImpl extends AbstractModInitializer implements ModInitializer {
    private static final BlockEntityProviders BLOCK_ENTITY_PROVIDERS = new BlockEntityProviders(
        FabricDiskDriveBlockEntity::new,
        (pos, state) -> new FabricPortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
        (pos, state) -> new FabricPortableGridBlockEntity(PortableGridType.CREATIVE, pos, state),
        FabricDiskInterfaceBlockEntity::new,
        FabricCableBlockEntity::new,
        FabricExternalStorageBlockEntity::new,
        FabricExporterBlockEntity::new,
        FabricImporterBlockEntity::new,
        FabricConstructorBlockEntity::new,
        FabricDestructorBlockEntity::new
    );
    private static final Logger LOGGER = LoggerFactory.getLogger(ModInitializerImpl.class);
    private static final String PLUGIN_ENTRYPOINT_KEY = "refinedstorage_plugin";

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);
        PlatformProxy.loadPlatform(new PlatformImpl());
        initializePlatformApi();
        ((RefinedStorageFabricApiProxy) RefinedStorageFabricApi.INSTANCE).setDelegate(
            new RefinedStorageFabricApiImpl(RefinedStorageApi.INSTANCE)
        );
        registerGridResourceRepositoryMappers();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerExternalPatternSinkStrategyFactories();
        registerContent();
        registerPackets();
        registerPacketHandlers();
        registerRecipeSync();
        registerSounds(new DirectRegistryCallback<>(BuiltInRegistries.SOUND_EVENT));
        registerRecipeSerializers(new DirectRegistryCallback<>(BuiltInRegistries.RECIPE_SERIALIZER));
        registerCapabilities();
        registerTickHandler();
        registerWrenchingEvent();
        registerSecurityBlockBreakEvent();

        final List<RefinedStoragePlugin> pluginEntrypoints = FabricLoader.getInstance()
            .getEntrypoints(PLUGIN_ENTRYPOINT_KEY, RefinedStoragePlugin.class);
        LOGGER.debug("Loading {} Refined Storage plugin entrypoints.", pluginEntrypoints.size());
        pluginEntrypoints.forEach(plugin -> plugin.onApiAvailable(RefinedStorageApi.INSTANCE));

        LOGGER.debug("Refined Storage has loaded.");
    }

    private void registerGridResourceRepositoryMappers() {
        RefinedStorageApi.INSTANCE.addGridResourceRepositoryMapper(
            ItemResource.class,
            new FabricItemGridResourceRepositoryMapper()
        );
        RefinedStorageApi.INSTANCE.addGridResourceRepositoryMapper(
            FluidResource.class,
            new FabricFluidGridResourceRepositoryMapper()
        );
    }

    private void registerAdditionalGridInsertionStrategyFactories() {
        RefinedStorageApi.INSTANCE.addGridInsertionStrategyFactory(FluidGridInsertionStrategy::new);
    }

    private void registerGridExtractionStrategyFactories() {
        RefinedStorageApi.INSTANCE.addGridExtractionStrategyFactory(ItemGridExtractionStrategy::new);
        RefinedStorageApi.INSTANCE.addGridExtractionStrategyFactory(FluidGridExtractionStrategy::new);
    }

    private void registerGridScrollingStrategyFactories() {
        RefinedStorageApi.INSTANCE.addGridScrollingStrategyFactory(ItemGridScrollingStrategy::new);
    }

    private void registerImporterTransferStrategyFactories() {
        RefinedStorageApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new FabricStorageImporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                VariantUtil::ofItemVariant,
                resource -> resource instanceof ItemResource itemResource
                    ? toItemVariant(itemResource) : null,
                1
            )
        );
        RefinedStorageApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FabricStorageImporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                VariantUtil::ofFluidVariant,
                resource -> resource instanceof FluidResource fluidResource
                    ? toFluidVariant(fluidResource) : null,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExporterTransferStrategyFactories() {
        RefinedStorageApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new FabricStorageExporterTransferStrategyFactory<>(
                ItemResource.class,
                ItemStorage.SIDED,
                resource -> resource instanceof ItemResource itemResource
                    ? toItemVariant(itemResource) : null,
                1
            )
        );
        RefinedStorageApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FabricStorageExporterTransferStrategyFactory<>(
                FluidResource.class,
                FluidStorage.SIDED,
                resource -> resource instanceof FluidResource fluidResource
                    ? toFluidVariant(fluidResource) : null,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExternalStorageProviderFactories() {
        RefinedStorageApi.INSTANCE.addExternalStorageProviderFactory(
            new FabricStorageExternalStorageProviderFactory<>(
                ItemStorage.SIDED,
                VariantUtil::ofItemVariant,
                resource -> resource instanceof ItemResource itemResource
                    ? toItemVariant(itemResource) : null
            )
        );
        RefinedStorageApi.INSTANCE.addExternalStorageProviderFactory(
            new FabricStorageExternalStorageProviderFactory<>(
                FluidStorage.SIDED,
                VariantUtil::ofFluidVariant,
                resource -> resource instanceof FluidResource fluidResource
                    ? toFluidVariant(fluidResource) : null
            )
        );
    }

    private void registerExternalPatternSinkStrategyFactories() {
        RefinedStorageFabricApi.INSTANCE.addStorageExternalPatternSinkStrategyFactory(
            new StorageExternalPatternSinkStrategyFactoryImpl<>(
                ItemStorage.SIDED,
                resource -> resource instanceof ItemResource itemResource
                    ? toItemVariant(itemResource) : null
            )
        );
        RefinedStorageFabricApi.INSTANCE.addStorageExternalPatternSinkStrategyFactory(
            new StorageExternalPatternSinkStrategyFactoryImpl<>(
                FluidStorage.SIDED,
                resource -> resource instanceof FluidResource fluidResource
                    ? toFluidVariant(fluidResource) : null
            )
        );
    }

    private void registerContent() {
        registerBlocks(new DirectRegistryCallback<>(BuiltInRegistries.BLOCK), BLOCK_ENTITY_PROVIDERS);
        final DirectRegistryCallback<Item> itemRegistryCallback = new DirectRegistryCallback<>(BuiltInRegistries.ITEM);
        registerItems(itemRegistryCallback);
        registerCustomItems(itemRegistryCallback);
        registerUpgradeMappings();
        registerCreativeModeTab();
        registerBlockEntities(
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK_ENTITY_TYPE),
            new BlockEntityTypeFactory() {
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntityProvider<T> factory,
                                                                         final Block... allowedBlocks) {
                    return FabricBlockEntityTypeBuilder.create(factory::create, allowedBlocks).build();
                }
            },
            BLOCK_ENTITY_PROVIDERS
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
                return new ExtendedMenuType<>(supplier::create, streamCodec);
            }
        });
        registerLootFunctions(new DirectRegistryCallback<>(BuiltInRegistries.LOOT_FUNCTION_TYPE));
        registerDataComponents(new DirectRegistryCallback<>(BuiltInRegistries.DATA_COMPONENT_TYPE));
    }

    private void registerCustomItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setRegulatorUpgrade(callback.register(REGULATOR_UPGRADE, () -> new RegulatorUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry()
        ) {
            @Override
            public boolean allowComponentsUpdateAnimation(final Player player,
                                                          final InteractionHand hand,
                                                          final ItemStack oldStack,
                                                          final ItemStack newStack) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setWirelessGrid(callback.register(WIRELESS_GRID, () -> new WirelessGridItem(false) {
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
            () -> new WirelessGridItem(true) {
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
        Items.INSTANCE.setWirelessAutocraftingMonitor(callback.register(
            WIRELESS_AUTOCRAFTING_MONITOR,
            () -> new WirelessAutocraftingMonitorItem(false) {
                @Override
                public boolean allowComponentsUpdateAnimation(final Player player,
                                                              final InteractionHand hand,
                                                              final ItemStack oldStack,
                                                              final ItemStack newStack) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setCreativeWirelessAutocraftingMonitor(callback.register(
            CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR,
            () -> new WirelessAutocraftingMonitorItem(true) {
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
            RefinedStorageApi.INSTANCE.getCreativeModeTabId(),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(ContentNames.MOD)
                .icon(() -> new ItemStack(Blocks.INSTANCE.getCreativeController().getDefault()))
                .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
                .build()
        );
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            RefinedStorageApi.INSTANCE.getColoredCreativeModeTabId(),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
                .title(ContentNames.MOD_COLORIZED)
                .icon(() -> new ItemStack(Blocks.INSTANCE.getCreativeController().get(DyeColor.LIME)))
                .displayItems((params, output) -> CreativeModeTabItems.appendColoredVariants(output::accept))
                .build()
        );
    }

    private void registerPackets() {
        registerServerToClientPackets();
        registerClientToServerPackets();
    }

    private void registerServerToClientPackets() {
        PayloadTypeRegistry.clientboundPlay().register(EnergyInfoPacket.PACKET_TYPE, EnergyInfoPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
            WirelessTransmitterDataPacket.PACKET_TYPE,
            WirelessTransmitterDataPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(GridActivePacket.PACKET_TYPE, GridActivePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
            AutocrafterManagerActivePacket.PACKET_TYPE,
            AutocrafterManagerActivePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(GridClearPacket.PACKET_TYPE, GridClearPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GridUpdatePacket.PACKET_TYPE, GridUpdatePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
            NetworkTransmitterStatusPacket.PACKET_TYPE,
            NetworkTransmitterStatusPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            MessagePacket.PACKET_TYPE,
            MessagePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            ResourceSlotUpdatePacket.PACKET_TYPE,
            ResourceSlotUpdatePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            StorageInfoResponsePacket.PACKET_TYPE,
            StorageInfoResponsePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            PatternGridAllowedAlternativesUpdatePacket.PACKET_TYPE,
            PatternGridAllowedAlternativesUpdatePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocrafterNameUpdatePacket.PACKET_TYPE,
            AutocrafterNameUpdatePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocrafterLockedUpdatePacket.PACKET_TYPE,
            AutocrafterLockedUpdatePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingPreviewResponsePacket.PACKET_TYPE,
            AutocraftingPreviewResponsePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingTreePreviewResponsePacket.PACKET_TYPE,
            AutocraftingTreePreviewResponsePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingPreviewCancelResponsePacket.PACKET_TYPE,
            AutocraftingPreviewCancelResponsePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingPreviewMaxAmountResponsePacket.PACKET_TYPE,
            AutocraftingPreviewMaxAmountResponsePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingResponsePacket.PACKET_TYPE,
            AutocraftingResponsePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingMonitorTaskAddedPacket.PACKET_TYPE,
            AutocraftingMonitorTaskAddedPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingMonitorTaskRemovedPacket.PACKET_TYPE,
            AutocraftingMonitorTaskRemovedPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingMonitorTaskStatusChangedPacket.PACKET_TYPE,
            AutocraftingMonitorTaskStatusChangedPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingMonitorActivePacket.PACKET_TYPE,
            AutocraftingMonitorActivePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            AutocraftingTaskCompletedPacket.PACKET_TYPE,
            AutocraftingTaskCompletedPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
            ExportingIndicatorUpdatePacket.PACKET_TYPE,
            ExportingIndicatorUpdatePacket.STREAM_CODEC
        );
    }

    private void registerClientToServerPackets() {
        PayloadTypeRegistry.serverboundPlay().register(
            CraftingGridClearPacket.PACKET_TYPE,
            CraftingGridClearPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridClearPacket.PACKET_TYPE,
            PatternGridClearPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridCreatePatternPacket.PACKET_TYPE,
            PatternGridCreatePatternPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            CraftingGridRecipeTransferPacket.PACKET_TYPE,
            CraftingGridRecipeTransferPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(GridExtractPacket.PACKET_TYPE, GridExtractPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GridInsertPacket.PACKET_TYPE, GridInsertPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GridScrollPacket.PACKET_TYPE, GridScrollPacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay()
            .register(PropertyChangePacket.PACKET_TYPE, PropertyChangePacket.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(
            ResourceFilterSlotChangePacket.PACKET_TYPE,
            ResourceFilterSlotChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            ResourceSlotAmountChangePacket.PACKET_TYPE,
            ResourceSlotAmountChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            ResourceSlotChangePacket.PACKET_TYPE,
            ResourceSlotChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            FilterSlotChangePacket.PACKET_TYPE,
            FilterSlotChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            SecurityCardBoundPlayerPacket.PACKET_TYPE,
            SecurityCardBoundPlayerPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            SecurityCardPermissionPacket.PACKET_TYPE,
            SecurityCardPermissionPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            SecurityCardResetPermissionPacket.PACKET_TYPE,
            SecurityCardResetPermissionPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            SingleAmountChangePacket.PACKET_TYPE,
            SingleAmountChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            StorageInfoRequestPacket.PACKET_TYPE,
            StorageInfoRequestPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            UseSlotReferencedItemPacket.PACKET_TYPE,
            UseSlotReferencedItemPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridAllowedAlternativesChangePacket.PACKET_TYPE,
            PatternGridAllowedAlternativesChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridCraftingRecipeTransferPacket.PACKET_TYPE,
            PatternGridCraftingRecipeTransferPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridProcessingRecipeTransferPacket.PACKET_TYPE,
            PatternGridProcessingRecipeTransferPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridStonecutterRecipeTransferPacket.PACKET_TYPE,
            PatternGridStonecutterRecipeTransferPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            PatternGridSmithingTableRecipeTransferPacket.PACKET_TYPE,
            PatternGridSmithingTableRecipeTransferPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocrafterNameChangePacket.PACKET_TYPE,
            AutocrafterNameChangePacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocraftingPreviewRequestPacket.PACKET_TYPE,
            AutocraftingPreviewRequestPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocraftingPreviewCancelRequestPacket.PACKET_TYPE,
            AutocraftingPreviewCancelRequestPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocraftingPreviewMaxAmountRequestPacket.PACKET_TYPE,
            AutocraftingPreviewMaxAmountRequestPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocraftingRequestPacket.PACKET_TYPE,
            AutocraftingRequestPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocraftingMonitorCancelPacket.PACKET_TYPE,
            AutocraftingMonitorCancelPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
            AutocraftingMonitorCancelAllPacket.PACKET_TYPE,
            AutocraftingMonitorCancelAllPacket.STREAM_CODEC
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
            PatternGridClearPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> PatternGridClearPacket.handle(ctx))
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternGridCreatePatternPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> PatternGridCreatePatternPacket.handle(ctx))
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
            FilterSlotChangePacket.PACKET_TYPE,
            wrapHandler(FilterSlotChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            SingleAmountChangePacket.PACKET_TYPE,
            wrapHandler(SingleAmountChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            UseSlotReferencedItemPacket.PACKET_TYPE,
            wrapHandler(UseSlotReferencedItemPacket::handle)
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
        ServerPlayNetworking.registerGlobalReceiver(
            PatternGridAllowedAlternativesChangePacket.PACKET_TYPE,
            wrapHandler(PatternGridAllowedAlternativesChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternGridCraftingRecipeTransferPacket.PACKET_TYPE,
            wrapHandler(PatternGridCraftingRecipeTransferPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternGridProcessingRecipeTransferPacket.PACKET_TYPE,
            wrapHandler(PatternGridProcessingRecipeTransferPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternGridStonecutterRecipeTransferPacket.PACKET_TYPE,
            wrapHandler(PatternGridStonecutterRecipeTransferPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PatternGridSmithingTableRecipeTransferPacket.PACKET_TYPE,
            wrapHandler(PatternGridSmithingTableRecipeTransferPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocrafterNameChangePacket.PACKET_TYPE,
            wrapHandler(AutocrafterNameChangePacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocraftingPreviewRequestPacket.PACKET_TYPE,
            wrapHandler(AutocraftingPreviewRequestPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocraftingPreviewCancelRequestPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingPreviewCancelRequestPacket.handle(ctx))
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocraftingPreviewMaxAmountRequestPacket.PACKET_TYPE,
            wrapHandler(AutocraftingPreviewMaxAmountRequestPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocraftingRequestPacket.PACKET_TYPE,
            wrapHandler(AutocraftingRequestPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocraftingMonitorCancelPacket.PACKET_TYPE,
            wrapHandler(AutocraftingMonitorCancelPacket::handle)
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AutocraftingMonitorCancelAllPacket.PACKET_TYPE,
            wrapHandler((packet, ctx) -> AutocraftingMonitorCancelAllPacket.handle(ctx))
        );
    }

    private static <T extends CustomPacketPayload> ServerPlayNetworking.PlayPayloadHandler<T> wrapHandler(
        final PacketHandler<T> handler
    ) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    private void registerRecipeSync() {
        for (final var entry : BuiltInRegistries.RECIPE_SERIALIZER.entrySet()) {
            final ResourceKey<RecipeSerializer<?>> resourceKey = entry.getKey();
            if (resourceKey.identifier().getNamespace().equals("minecraft")) {
                final RecipeSerializer<?> serializer = entry.getValue();
                try {
                    RecipeSynchronization.synchronizeRecipeSerializer(serializer);
                } catch (RuntimeException e) {
                    LOGGER.warn("Failed to synchronize recipe serializer {}", resourceKey, e);
                }
            }
        }
    }

    private void registerCapabilities() {
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getCable());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getConstructor());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getController());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getCraftingGrid());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getPatternGrid());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getCreativeController());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getDestructor());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getDetector());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getDiskDrive());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getDiskInterface());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getExporter());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getExternalStorage());
        Arrays.stream(FluidStorageVariant.values()).forEach(type ->
            registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getFluidStorageBlock(type)));
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getGrid());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getImporter());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getInterface());
        Arrays.stream(ItemStorageVariant.values()).forEach(type ->
            registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getItemStorageBlock(type)));
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getNetworkReceiver());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getNetworkTransmitter());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getRelay());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getSecurityManager());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getStorageMonitor());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getWirelessTransmitter());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getAutocrafter());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getAutocrafterManager());
        registerNetworkNodeContainerProvider(BlockEntities.INSTANCE.getAutocraftingMonitor());
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
        registerItemStorage(
            PatternGridBlockEntity.class::isInstance,
            PatternGridBlockEntity.class::cast,
            PatternGridBlockEntity::getPatternInput,
            BlockEntities.INSTANCE.getPatternGrid()
        );
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, context) -> {
            final ContainerStorage storage = ContainerStorage.of(blockEntity.getDiskInventory(), context);
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

    private void registerNetworkNodeContainerProvider(
        final BlockEntityType<? extends AbstractNetworkNodeContainerBlockEntity<?>> type
    ) {
        RefinedStorageFabricApi.INSTANCE.getNetworkNodeContainerProviderLookup().registerForBlockEntity(
            (be, dir) -> be.getContainerProvider(),
            type
        );
    }

    private <T extends BlockEntity> void registerItemStorage(final Predicate<BlockEntity> test,
                                                             final Function<BlockEntity, T> caster,
                                                             final Function<T, Container> containerSupplier,
                                                             final BlockEntityType<?> type) {
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (test.test(blockEntity)) {
                final T casted = caster.apply(blockEntity);
                return ContainerStorage.of(containerSupplier.apply(casted), context);
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
            (stack, context) ->
                new EnergyStorageAdapter(Items.INSTANCE.getWirelessGrid().createEnergyStorage(stack), context),
            Items.INSTANCE.getWirelessGrid()
        );
        Items.INSTANCE.getControllers().forEach(controller -> EnergyStorage.ITEM.registerForItems(
            (stack, context) ->
                new EnergyStorageAdapter(controller.get().createEnergyStorage(stack), context),
            controller.get()
        ));
        EnergyStorage.ITEM.registerForItems(
            (stack, context)
                -> new EnergyStorageAdapter(PortableGridBlockItem.createEnergyStorage(stack), context),
            Items.INSTANCE.getPortableGrid()
        );
        EnergyStorage.ITEM.registerForItems(
            (stack, context) ->
                new EnergyStorageAdapter(Items.INSTANCE.getWirelessAutocraftingMonitor().createEnergyStorage(stack),
                    context),
            Items.INSTANCE.getWirelessAutocraftingMonitor()
        );
    }

    private void registerTickHandler() {
        ServerTickEvents.START_SERVER_TICK.register(ServerListener::tick);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> ServerListener.starting());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerListener.stopped());
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

package com.refinedmods.refinedstorage.neoforge;

import com.refinedmods.refinedstorage.common.AbstractClientModInitializer;
import com.refinedmods.refinedstorage.common.AbstractModInitializer;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.PlatformProxy;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.WirelessAutocraftingMonitorItem;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockEntityProviders;
import com.refinedmods.refinedstorage.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage.common.content.ExtendedMenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage.common.content.RegistryCallback;
import com.refinedmods.refinedstorage.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage.common.security.FallbackSecurityCardItem;
import com.refinedmods.refinedstorage.common.security.SecurityCardItem;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
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
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage.common.util.ServerListener;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApiProxy;
import com.refinedmods.refinedstorage.neoforge.autocrafting.FluidHandlerExternalPatternProviderSinkFactory;
import com.refinedmods.refinedstorage.neoforge.autocrafting.ItemHandlerExternalPatternProviderSinkFactory;
import com.refinedmods.refinedstorage.neoforge.constructordestructor.ForgeConstructorBlockEntity;
import com.refinedmods.refinedstorage.neoforge.constructordestructor.ForgeDestructorBlockEntity;
import com.refinedmods.refinedstorage.neoforge.debug.DebugStickItem;
import com.refinedmods.refinedstorage.neoforge.exporter.FluidHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.neoforge.exporter.ForgeExporterBlockEntity;
import com.refinedmods.refinedstorage.neoforge.exporter.ItemHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.neoforge.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage.neoforge.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage.neoforge.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage.neoforge.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage.neoforge.grid.view.ForgeFluidResourceRepositoryMapper;
import com.refinedmods.refinedstorage.neoforge.grid.view.ForgeItemResourceRepositoryMapper;
import com.refinedmods.refinedstorage.neoforge.importer.FluidHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.neoforge.importer.ForgeImporterBlockEntity;
import com.refinedmods.refinedstorage.neoforge.importer.ItemHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.neoforge.networking.ForgeCableBlockEntity;
import com.refinedmods.refinedstorage.neoforge.storage.diskdrive.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.neoforge.storage.diskinterface.ForgeDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.neoforge.storage.externalstorage.FluidHandlerExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.neoforge.storage.externalstorage.ForgeExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.neoforge.storage.externalstorage.ItemHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.neoforge.storage.portablegrid.ForgePortableGridBlockEntity;
import com.refinedmods.refinedstorage.neoforge.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage.neoforge.support.inventory.InsertExtractItemHandler;
import com.refinedmods.refinedstorage.neoforge.support.resource.ResourceContainerFluidHandlerAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

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
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

@Mod(IdentifierUtil.MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private static final BlockEntityProviders BLOCK_ENTITY_PROVIDERS = new BlockEntityProviders(
        ForgeDiskDriveBlockEntity::new,
        (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
        (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.CREATIVE, pos, state),
        ForgeDiskInterfaceBlockEntity::new,
        ForgeCableBlockEntity::new,
        ForgeExternalStorageBlockEntity::new,
        ForgeExporterBlockEntity::new,
        ForgeImporterBlockEntity::new,
        ForgeConstructorBlockEntity::new,
        ForgeDestructorBlockEntity::new
    );

    private final DeferredRegister<Block> blockRegistry =
        DeferredRegister.create(BuiltInRegistries.BLOCK, IdentifierUtil.MOD_ID);
    private final DeferredRegister<Item> itemRegistry =
        DeferredRegister.create(BuiltInRegistries.ITEM, IdentifierUtil.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntityTypeRegistry =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, IdentifierUtil.MOD_ID);
    private final DeferredRegister<MenuType<?>> menuTypeRegistry =
        DeferredRegister.create(BuiltInRegistries.MENU, IdentifierUtil.MOD_ID);
    private final DeferredRegister<SoundEvent> soundEventRegistry =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, IdentifierUtil.MOD_ID);
    private final DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry =
        DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, IdentifierUtil.MOD_ID);
    private final DeferredRegister<DataComponentType<?>> dataComponentTypeRegistry =
        DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, IdentifierUtil.MOD_ID);
    private final String modVersion;

    public ModInitializer(final IEventBus eventBus, final ModContainer modContainer) {
        this.modVersion = modContainer.getModInfo().getVersion().toString();

        PlatformProxy.loadPlatform(new PlatformImpl(modContainer));
        initializePlatformApi();
        ((RefinedStorageNeoForgeApiProxy) RefinedStorageNeoForgeApi.INSTANCE).setDelegate(
            new RefinedStorageNeoForgeApiImpl()
        );
        registerGridResourceRepositoryMappers();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerPatternProviderSinkFactories();
        registerContent(eventBus);
        registerSounds(eventBus);
        registerRecipeSerializers(eventBus);
        registerTickHandler();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            AbstractClientModInitializer.initializeClientPlatformApi();
            eventBus.addListener(ClientModInitializer::onClientSetup);
            eventBus.addListener(ClientModInitializer::onRegisterCustomModels);
            eventBus.addListener(ClientModInitializer::onRegisterMenuScreens);
            eventBus.addListener(ClientModInitializer::onRegisterKeyMappings);
            eventBus.addListener(ClientModInitializer::onRegisterTooltipFactories);
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onRegister);
        eventBus.addListener(this::registerPackets);
        eventBus.addListener(this::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(this::registerWrenchingEvent);
        NeoForge.EVENT_BUS.addListener(this::registerSecurityBlockBreakEvent);
    }

    private void registerGridResourceRepositoryMappers() {
        RefinedStorageApi.INSTANCE.addGridResourceRepositoryMapper(
            ItemResource.class,
            new ForgeItemResourceRepositoryMapper()
        );
        RefinedStorageApi.INSTANCE.addGridResourceRepositoryMapper(
            FluidResource.class,
            new ForgeFluidResourceRepositoryMapper()
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
            new ItemHandlerImporterTransferStrategyFactory()
        );
        RefinedStorageApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FluidHandlerImporterTransferStrategyFactory()
        );
    }

    private void registerExporterTransferStrategyFactories() {
        RefinedStorageApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new ItemHandlerExporterTransferStrategyFactory()
        );
        RefinedStorageApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FluidHandlerExporterTransferStrategyFactory()
        );
    }

    private void registerExternalStorageProviderFactories() {
        RefinedStorageApi.INSTANCE.addExternalStorageProviderFactory(
            new ItemHandlerPlatformExternalStorageProviderFactory());
        RefinedStorageApi.INSTANCE.addExternalStorageProviderFactory(
            new FluidHandlerExternalStorageProviderFactory()
        );
    }

    private void registerPatternProviderSinkFactories() {
        RefinedStorageApi.INSTANCE.addPatternProviderExternalPatternSinkFactory(
            new ItemHandlerExternalPatternProviderSinkFactory()
        );
        RefinedStorageApi.INSTANCE.addPatternProviderExternalPatternSinkFactory(
            new FluidHandlerExternalPatternProviderSinkFactory()
        );
    }

    private void registerContent(final IEventBus eventBus) {
        registerBlocks(eventBus);
        registerItems(eventBus);
        registerBlockEntities(eventBus);
        registerMenus(eventBus);
        registerDataComponents(eventBus);
    }

    private void registerBlocks(final IEventBus eventBus) {
        registerBlocks(new ForgeRegistryCallback<>(blockRegistry), BLOCK_ENTITY_PROVIDERS);
        blockRegistry.register(eventBus);
    }

    private void registerItems(final IEventBus eventBus) {
        final RegistryCallback<Item> callback = new ForgeRegistryCallback<>(itemRegistry);
        registerItems(callback);
        Items.INSTANCE.setDebugStick(callback.register(ContentIds.DEBUG_STICK, DebugStickItem::new));
        registerCustomItems(callback);
        itemRegistry.register(eventBus);
    }

    private void registerCustomItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setRegulatorUpgrade(callback.register(REGULATOR_UPGRADE, () -> new RegulatorUpgradeItem(
            RefinedStorageApi.INSTANCE.getUpgradeRegistry()
        ) {
            @Override
            public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                       final ItemStack newStack,
                                                       final boolean slotChanged) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setWirelessGrid(callback.register(WIRELESS_GRID, () -> new WirelessGridItem(false) {
            @Override
            public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                       final ItemStack newStack,
                                                       final boolean slotChanged) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setCreativeWirelessGrid(callback.register(
            CREATIVE_WIRELESS_GRID,
            () -> new WirelessGridItem(true) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setPortableGrid(callback.register(PORTABLE_GRID, () -> new PortableGridBlockItem(
            Blocks.INSTANCE.getPortableGrid(), PortableGridType.NORMAL
        ) {
            @Override
            public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                       final ItemStack newStack,
                                                       final boolean slotChanged) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setCreativePortableGrid(callback.register(
            CREATIVE_PORTABLE_GRID,
            () -> new PortableGridBlockItem(Blocks.INSTANCE.getCreativePortableGrid(), PortableGridType.CREATIVE) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setSecurityCard(callback.register(SECURITY_CARD, () -> new SecurityCardItem() {
            @Override
            public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                       final ItemStack newStack,
                                                       final boolean slotChanged) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setFallbackSecurityCard(callback.register(
            FALLBACK_SECURITY_CARD,
            () -> new FallbackSecurityCardItem() {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setWirelessAutocraftingMonitor(callback.register(
            WIRELESS_AUTOCRAFTING_MONITOR,
            () -> new WirelessAutocraftingMonitorItem(false) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
        Items.INSTANCE.setCreativeWirelessAutocraftingMonitor(callback.register(
            CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR,
            () -> new WirelessAutocraftingMonitorItem(true) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
                }
            }
        ));
    }

    private void registerBlockEntities(final IEventBus eventBus) {
        registerBlockEntities(
            new ForgeRegistryCallback<>(blockEntityTypeRegistry),
            new BlockEntityTypeFactory() {
                @SuppressWarnings("DataFlowIssue") // data type can be null
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntityProvider<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            },
            BLOCK_ENTITY_PROVIDERS
        );
        blockEntityTypeRegistry.register(eventBus);
    }

    private void registerMenus(final IEventBus eventBus) {
        registerMenus(new ForgeRegistryCallback<>(menuTypeRegistry), new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return new MenuType<>(supplier::create, FeatureFlags.DEFAULT_FLAGS);
            }
        }, new ExtendedMenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu, D> MenuType<T> create(final MenuSupplier<T, D> supplier,
                                                                           final StreamCodec<RegistryFriendlyByteBuf, D>
                                                                               streamCodec) {
                return IMenuTypeExtension.create((syncId, inventory, buf) -> {
                    final D data = streamCodec.decode(buf);
                    return supplier.create(syncId, inventory, data);
                });
            }
        });
        menuTypeRegistry.register(eventBus);
    }

    private void registerDataComponents(final IEventBus eventBus) {
        final RegistryCallback<DataComponentType<?>> callback = new ForgeRegistryCallback<>(dataComponentTypeRegistry);
        registerDataComponents(callback);
        dataComponentTypeRegistry.register(eventBus);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getCable());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getConstructor());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getController());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getCraftingGrid());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getPatternGrid());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getCreativeController());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getDestructor());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getDetector());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getDiskDrive());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getDiskInterface());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getExporter());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getExternalStorage());
        Arrays.stream(FluidStorageVariant.values()).forEach(type ->
            registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getFluidStorageBlock(type)));
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getGrid());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getImporter());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getInterface());
        Arrays.stream(ItemStorageVariant.values()).forEach(type ->
            registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getItemStorageBlock(type)));
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getNetworkReceiver());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getNetworkTransmitter());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getRelay());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getSecurityManager());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getStorageMonitor());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getWirelessTransmitter());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getAutocrafter());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getAutocrafterManager());
        registerNetworkNodeContainerProvider(event, BlockEntities.INSTANCE.getAutocraftingMonitor());
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.INSTANCE.getDiskDrive(),
            (be, side) -> new InvWrapper(be.getDiskInventory())
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.INSTANCE.getInterface(),
            (be, side) -> new InvWrapper(be.getExportedResourcesAsContainer())
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.INSTANCE.getPatternGrid(),
            (be, side) -> new InvWrapper(be.getPatternInput())
        );
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            BlockEntities.INSTANCE.getInterface(),
            (be, side) -> new ResourceContainerFluidHandlerAdapter(be.getExportedResources())
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntities.INSTANCE.getController(),
            (be, side) -> new EnergyStorageAdapter(be.getEnergyStorage())
        );
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            BlockEntities.INSTANCE.getPortableGrid(),
            (be, side) -> new EnergyStorageAdapter(be.getEnergyStorage())
        );
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new EnergyStorageAdapter(Items.INSTANCE.getWirelessGrid().createEnergyStorage(stack)),
            Items.INSTANCE.getWirelessGrid()
        );
        Items.INSTANCE.getControllers().forEach(controllerItem -> event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new EnergyStorageAdapter(controllerItem.get().createEnergyStorage(stack)),
            controllerItem.get()
        ));
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new EnergyStorageAdapter(PortableGridBlockItem.createEnergyStorage(stack)),
            Items.INSTANCE.getPortableGrid()
        );
        event.registerItem(
            Capabilities.EnergyStorage.ITEM,
            (stack, ctx) -> new EnergyStorageAdapter(
                Items.INSTANCE.getWirelessAutocraftingMonitor().createEnergyStorage(stack)
            ),
            Items.INSTANCE.getWirelessAutocraftingMonitor()
        );
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            BlockEntities.INSTANCE.getDiskInterface(),
            (be, side) -> {
                final InvWrapper wrapper = new InvWrapper(be.getDiskInventory());
                return new InsertExtractItemHandler(
                    new RangedWrapper(
                        wrapper,
                        0,
                        AbstractDiskInterfaceBlockEntity.AMOUNT_OF_DISKS / 2
                    ),
                    new RangedWrapper(
                        wrapper,
                        AbstractDiskInterfaceBlockEntity.AMOUNT_OF_DISKS / 2,
                        AbstractDiskInterfaceBlockEntity.AMOUNT_OF_DISKS
                    )
                );
            }
        );
    }

    private void registerNetworkNodeContainerProvider(
        final RegisterCapabilitiesEvent event,
        final BlockEntityType<? extends AbstractNetworkNodeContainerBlockEntity<?>> type
    ) {
        event.registerBlockEntity(
            RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),
            type,
            (be, side) -> be.getContainerProvider()
        );
    }

    private void registerSounds(final IEventBus eventBus) {
        registerSounds(new ForgeRegistryCallback<>(soundEventRegistry));
        soundEventRegistry.register(eventBus);
    }

    private void registerRecipeSerializers(final IEventBus eventBus) {
        registerRecipeSerializers(new ForgeRegistryCallback<>(recipeSerializerRegistry));
        recipeSerializerRegistry.register(eventBus);
    }

    private void registerTickHandler() {
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onServerStopped);
    }

    @SubscribeEvent
    public void onCommonSetup(final FMLCommonSetupEvent e) {
        registerUpgradeMappings();
    }

    @SubscribeEvent
    public void onRegister(final RegisterEvent e) {
        e.register(
            Registries.LOOT_FUNCTION_TYPE,
            helper -> registerLootFunctions(new DirectRegistryCallback<>(BuiltInRegistries.LOOT_FUNCTION_TYPE))
        );
        e.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(
            RefinedStorageApi.INSTANCE.getCreativeModeTabId(),
            CreativeModeTab.builder()
                .title(ContentNames.MOD)
                .icon(() -> new ItemStack(Blocks.INSTANCE.getCreativeController().getDefault()))
                .displayItems((params, output) -> {
                    CreativeModeTabItems.append(output::accept);
                    if (Platform.INSTANCE.getConfig().isDebug()) {
                        output.accept(Items.INSTANCE.getDebugStick());
                    }
                })
                .build()
        ));
        e.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(
            RefinedStorageApi.INSTANCE.getColoredCreativeModeTabId(),
            CreativeModeTab.builder()
                .title(ContentNames.MOD_COLORIZED)
                .icon(() -> new ItemStack(Blocks.INSTANCE.getCreativeController().get(DyeColor.LIME)))
                .displayItems((params, output) -> CreativeModeTabItems.appendColoredVariants(output::accept))
                .build()
        ));
    }

    @SubscribeEvent
    public void registerWrenchingEvent(final PlayerInteractEvent.RightClickBlock e) {
        final Level level = e.getLevel();
        final BlockState state = level.getBlockState(e.getHitVec().getBlockPos());
        if (!(state.getBlock() instanceof AbstractBaseBlock block)) {
            return;
        }
        block.tryUseWrench(state, level, e.getHitVec(), e.getEntity(), e.getHand()).or(() -> block.tryUpdateColor(
            state,
            level,
            e.getHitVec().getBlockPos(),
            e.getEntity(),
            e.getHand()
        )).ifPresent(result -> {
            e.setCanceled(true);
            e.setCancellationResult(result);
        });
    }

    @SubscribeEvent
    public void registerSecurityBlockBreakEvent(final BlockEvent.BreakEvent e) {
        if (!(e.getLevel() instanceof Level level)) {
            return;
        }
        final NetworkNodeContainerProvider provider = Platform.INSTANCE.getContainerProvider(level, e.getPos(), null);
        final Player player = e.getPlayer();
        if (provider != null && player instanceof ServerPlayer serverPlayer && !provider.canBuild(serverPlayer)) {
            RefinedStorageApi.INSTANCE.sendNoPermissionMessage(
                serverPlayer,
                createTranslation("misc", "no_permission.build.break", e.getState().getBlock().getName())
            );
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(modVersion);
        registerServerToClientPackets(registrar);
        registerClientToServerPackets(registrar);
    }

    private static void registerServerToClientPackets(final PayloadRegistrar registrar) {
        registrar.playToClient(
            EnergyInfoPacket.PACKET_TYPE,
            EnergyInfoPacket.STREAM_CODEC,
            wrapHandler(EnergyInfoPacket::handle)
        );
        registrar.playToClient(
            GridActivePacket.PACKET_TYPE,
            GridActivePacket.STREAM_CODEC,
            wrapHandler(GridActivePacket::handle)
        );
        registrar.playToClient(
            AutocrafterManagerActivePacket.PACKET_TYPE,
            AutocrafterManagerActivePacket.STREAM_CODEC,
            wrapHandler(AutocrafterManagerActivePacket::handle)
        );
        registrar.playToClient(
            GridClearPacket.PACKET_TYPE,
            GridClearPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> GridClearPacket.handle(ctx))
        );
        registrar.playToClient(
            GridUpdatePacket.PACKET_TYPE,
            GridUpdatePacket.STREAM_CODEC,
            wrapHandler(GridUpdatePacket::handle)
        );
        registrar.playToClient(
            NetworkTransmitterStatusPacket.PACKET_TYPE,
            NetworkTransmitterStatusPacket.STREAM_CODEC,
            wrapHandler(NetworkTransmitterStatusPacket::handle)
        );
        registrar.playToClient(
            ResourceSlotUpdatePacket.PACKET_TYPE,
            ResourceSlotUpdatePacket.STREAM_CODEC,
            wrapHandler(ResourceSlotUpdatePacket::handle)
        );
        registrar.playToClient(
            StorageInfoResponsePacket.PACKET_TYPE,
            StorageInfoResponsePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> StorageInfoResponsePacket.handle(packet))
        );
        registrar.playToClient(
            WirelessTransmitterDataPacket.PACKET_TYPE,
            WirelessTransmitterDataPacket.STREAM_CODEC,
            wrapHandler(WirelessTransmitterDataPacket::handle)
        );
        registrar.playToClient(
            MessagePacket.PACKET_TYPE,
            MessagePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> MessagePacket.handle(packet))
        );
        registrar.playToClient(
            PatternGridAllowedAlternativesUpdatePacket.PACKET_TYPE,
            PatternGridAllowedAlternativesUpdatePacket.STREAM_CODEC,
            wrapHandler(PatternGridAllowedAlternativesUpdatePacket::handle)
        );
        registrar.playToClient(
            AutocrafterNameUpdatePacket.PACKET_TYPE,
            AutocrafterNameUpdatePacket.STREAM_CODEC,
            wrapHandler(AutocrafterNameUpdatePacket::handle)
        );
        registrar.playToClient(
            AutocrafterLockedUpdatePacket.PACKET_TYPE,
            AutocrafterLockedUpdatePacket.STREAM_CODEC,
            wrapHandler(AutocrafterLockedUpdatePacket::handle)
        );
        registrar.playToClient(
            AutocraftingPreviewResponsePacket.PACKET_TYPE,
            AutocraftingPreviewResponsePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingPreviewResponsePacket.handle(packet))
        );
        registrar.playToClient(
            AutocraftingTreePreviewResponsePacket.PACKET_TYPE,
            AutocraftingTreePreviewResponsePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingTreePreviewResponsePacket.handle(packet))
        );
        registrar.playToClient(
            AutocraftingPreviewCancelResponsePacket.PACKET_TYPE,
            AutocraftingPreviewCancelResponsePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingPreviewCancelResponsePacket.handle())
        );
        registrar.playToClient(
            AutocraftingPreviewMaxAmountResponsePacket.PACKET_TYPE,
            AutocraftingPreviewMaxAmountResponsePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingPreviewMaxAmountResponsePacket.handle(packet))
        );
        registrar.playToClient(
            AutocraftingResponsePacket.PACKET_TYPE,
            AutocraftingResponsePacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingResponsePacket.handle(packet))
        );
        registrar.playToClient(
            AutocraftingMonitorTaskAddedPacket.PACKET_TYPE,
            AutocraftingMonitorTaskAddedPacket.STREAM_CODEC,
            wrapHandler(AutocraftingMonitorTaskAddedPacket::handle)
        );
        registrar.playToClient(
            AutocraftingMonitorTaskRemovedPacket.PACKET_TYPE,
            AutocraftingMonitorTaskRemovedPacket.STREAM_CODEC,
            wrapHandler(AutocraftingMonitorTaskRemovedPacket::handle)
        );
        registrar.playToClient(
            AutocraftingMonitorTaskStatusChangedPacket.PACKET_TYPE,
            AutocraftingMonitorTaskStatusChangedPacket.STREAM_CODEC,
            wrapHandler(AutocraftingMonitorTaskStatusChangedPacket::handle)
        );
        registrar.playToClient(
            AutocraftingMonitorActivePacket.PACKET_TYPE,
            AutocraftingMonitorActivePacket.STREAM_CODEC,
            wrapHandler(AutocraftingMonitorActivePacket::handle)
        );
        registrar.playToClient(
            AutocraftingTaskCompletedPacket.PACKET_TYPE,
            AutocraftingTaskCompletedPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingTaskCompletedPacket.handle(packet))
        );
        registrar.playToClient(
            ExportingIndicatorUpdatePacket.PACKET_TYPE,
            ExportingIndicatorUpdatePacket.STREAM_CODEC,
            wrapHandler(ExportingIndicatorUpdatePacket::handle)
        );
    }

    private static void registerClientToServerPackets(final PayloadRegistrar registrar) {
        registrar.playToServer(
            CraftingGridClearPacket.PACKET_TYPE,
            CraftingGridClearPacket.STREAM_CODEC,
            wrapHandler(CraftingGridClearPacket::handle)
        );
        registrar.playToServer(
            PatternGridClearPacket.PACKET_TYPE,
            PatternGridClearPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> PatternGridClearPacket.handle(ctx))
        );
        registrar.playToServer(
            PatternGridCreatePatternPacket.PACKET_TYPE,
            PatternGridCreatePatternPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> PatternGridCreatePatternPacket.handle(ctx))
        );
        registrar.playToServer(
            CraftingGridRecipeTransferPacket.PACKET_TYPE,
            CraftingGridRecipeTransferPacket.STREAM_CODEC,
            wrapHandler(CraftingGridRecipeTransferPacket::handle)
        );
        registrar.playToServer(
            GridExtractPacket.PACKET_TYPE,
            GridExtractPacket.STREAM_CODEC,
            wrapHandler(GridExtractPacket::handle)
        );
        registrar.playToServer(
            GridInsertPacket.PACKET_TYPE,
            GridInsertPacket.STREAM_CODEC,
            wrapHandler(GridInsertPacket::handle)
        );
        registrar.playToServer(
            GridScrollPacket.PACKET_TYPE,
            GridScrollPacket.STREAM_CODEC,
            wrapHandler(GridScrollPacket::handle)
        );
        registrar.playToServer(
            PropertyChangePacket.PACKET_TYPE,
            PropertyChangePacket.STREAM_CODEC,
            wrapHandler(PropertyChangePacket::handle)
        );
        registrar.playToServer(
            ResourceFilterSlotChangePacket.PACKET_TYPE,
            ResourceFilterSlotChangePacket.STREAM_CODEC,
            wrapHandler(ResourceFilterSlotChangePacket::handle)
        );
        registrar.playToServer(
            ResourceSlotAmountChangePacket.PACKET_TYPE,
            ResourceSlotAmountChangePacket.STREAM_CODEC,
            wrapHandler(ResourceSlotAmountChangePacket::handle)
        );
        registrar.playToServer(
            ResourceSlotChangePacket.PACKET_TYPE,
            ResourceSlotChangePacket.STREAM_CODEC,
            wrapHandler(ResourceSlotChangePacket::handle)
        );
        registrar.playToServer(
            FilterSlotChangePacket.PACKET_TYPE,
            FilterSlotChangePacket.STREAM_CODEC,
            wrapHandler(FilterSlotChangePacket::handle)
        );
        registrar.playToServer(
            SingleAmountChangePacket.PACKET_TYPE,
            SingleAmountChangePacket.STREAM_CODEC,
            wrapHandler(SingleAmountChangePacket::handle)
        );
        registrar.playToServer(
            StorageInfoRequestPacket.PACKET_TYPE,
            StorageInfoRequestPacket.STREAM_CODEC,
            wrapHandler(StorageInfoRequestPacket::handle)
        );
        registrar.playToServer(
            UseSlotReferencedItemPacket.PACKET_TYPE,
            UseSlotReferencedItemPacket.STREAM_CODEC,
            wrapHandler(UseSlotReferencedItemPacket::handle)
        );
        registrar.playToServer(
            SecurityCardPermissionPacket.PACKET_TYPE,
            SecurityCardPermissionPacket.STREAM_CODEC,
            wrapHandler(SecurityCardPermissionPacket::handle)
        );
        registrar.playToServer(
            SecurityCardResetPermissionPacket.PACKET_TYPE,
            SecurityCardResetPermissionPacket.STREAM_CODEC,
            wrapHandler(SecurityCardResetPermissionPacket::handle)
        );
        registrar.playToServer(
            SecurityCardBoundPlayerPacket.PACKET_TYPE,
            SecurityCardBoundPlayerPacket.STREAM_CODEC,
            wrapHandler(SecurityCardBoundPlayerPacket::handle)
        );
        registrar.playToServer(
            PatternGridAllowedAlternativesChangePacket.PACKET_TYPE,
            PatternGridAllowedAlternativesChangePacket.STREAM_CODEC,
            wrapHandler(PatternGridAllowedAlternativesChangePacket::handle)
        );
        registrar.playToServer(
            PatternGridCraftingRecipeTransferPacket.PACKET_TYPE,
            PatternGridCraftingRecipeTransferPacket.STREAM_CODEC,
            wrapHandler(PatternGridCraftingRecipeTransferPacket::handle)
        );
        registrar.playToServer(
            PatternGridProcessingRecipeTransferPacket.PACKET_TYPE,
            PatternGridProcessingRecipeTransferPacket.STREAM_CODEC,
            wrapHandler(PatternGridProcessingRecipeTransferPacket::handle)
        );
        registrar.playToServer(
            PatternGridStonecutterRecipeTransferPacket.PACKET_TYPE,
            PatternGridStonecutterRecipeTransferPacket.STREAM_CODEC,
            wrapHandler(PatternGridStonecutterRecipeTransferPacket::handle)
        );
        registrar.playToServer(
            PatternGridSmithingTableRecipeTransferPacket.PACKET_TYPE,
            PatternGridSmithingTableRecipeTransferPacket.STREAM_CODEC,
            wrapHandler(PatternGridSmithingTableRecipeTransferPacket::handle)
        );
        registrar.playToServer(
            AutocrafterNameChangePacket.PACKET_TYPE,
            AutocrafterNameChangePacket.STREAM_CODEC,
            wrapHandler(AutocrafterNameChangePacket::handle)
        );
        registrar.playToServer(
            AutocraftingPreviewRequestPacket.PACKET_TYPE,
            AutocraftingPreviewRequestPacket.STREAM_CODEC,
            wrapHandler(AutocraftingPreviewRequestPacket::handle)
        );
        registrar.playToServer(
            AutocraftingPreviewCancelRequestPacket.PACKET_TYPE,
            AutocraftingPreviewCancelRequestPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingPreviewCancelRequestPacket.handle(ctx))
        );
        registrar.playToServer(
            AutocraftingPreviewMaxAmountRequestPacket.PACKET_TYPE,
            AutocraftingPreviewMaxAmountRequestPacket.STREAM_CODEC,
            wrapHandler(AutocraftingPreviewMaxAmountRequestPacket::handle)
        );
        registrar.playToServer(
            AutocraftingRequestPacket.PACKET_TYPE,
            AutocraftingRequestPacket.STREAM_CODEC,
            wrapHandler(AutocraftingRequestPacket::handle)
        );
        registrar.playToServer(
            AutocraftingMonitorCancelPacket.PACKET_TYPE,
            AutocraftingMonitorCancelPacket.STREAM_CODEC,
            wrapHandler(AutocraftingMonitorCancelPacket::handle)
        );
        registrar.playToServer(
            AutocraftingMonitorCancelAllPacket.PACKET_TYPE,
            AutocraftingMonitorCancelAllPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> AutocraftingMonitorCancelAllPacket.handle(ctx))
        );
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> wrapHandler(final PacketHandler<T> handler) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Pre e) {
        ServerListener.tick(e.getServer());
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent e) {
        ServerListener.starting();
    }

    @SubscribeEvent
    public void onServerStopped(final ServerStoppedEvent e) {
        ServerListener.stopped();
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return registry.register(id.getPath(), value);
        }
    }
}

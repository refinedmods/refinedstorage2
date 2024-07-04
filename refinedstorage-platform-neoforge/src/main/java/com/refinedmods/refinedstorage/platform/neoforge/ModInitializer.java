package com.refinedmods.refinedstorage.platform.neoforge;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.network.NetworkNodeContainerBlockEntity;
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
import com.refinedmods.refinedstorage.platform.common.iface.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.common.security.FallbackSecurityCardItem;
import com.refinedmods.refinedstorage.platform.common.security.SecurityCardItem;
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
import com.refinedmods.refinedstorage.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage.platform.common.util.ServerEventQueue;
import com.refinedmods.refinedstorage.platform.neoforge.exporter.FluidHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.neoforge.exporter.ItemHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.neoforge.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.neoforge.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.neoforge.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.neoforge.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.neoforge.importer.FluidHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.neoforge.importer.ItemHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.neoforge.storage.diskdrive.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.platform.neoforge.storage.diskinterface.ForgeDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.platform.neoforge.storage.externalstorage.FluidHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.neoforge.storage.externalstorage.ItemHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.neoforge.storage.portablegrid.ForgePortableGridBlockEntity;
import com.refinedmods.refinedstorage.platform.neoforge.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage.platform.neoforge.support.inventory.InsertExtractItemHandler;
import com.refinedmods.refinedstorage.platform.neoforge.support.resource.ResourceContainerFluidHandlerAdapter;

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
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.CREATIVE_PORTABLE_GRID;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.CREATIVE_WIRELESS_GRID;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.FALLBACK_SECURITY_CARD;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.PORTABLE_GRID;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.REGULATOR_UPGRADE;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.SECURITY_CARD;
import static com.refinedmods.refinedstorage.platform.common.content.ContentIds.WIRELESS_GRID;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

@Mod(IdentifierUtil.MOD_ID)
public class ModInitializer extends AbstractModInitializer {
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

    public ModInitializer(final IEventBus eventBus, final ModContainer modContainer) {
        PlatformProxy.loadPlatform(new PlatformImpl(modContainer));
        initializePlatformApi();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerContent(eventBus);
        registerSounds(eventBus);
        registerRecipeSerializers(eventBus);
        registerTickHandler();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            eventBus.addListener(ClientModInitializer::onClientSetup);
            eventBus.addListener(ClientModInitializer::onRegisterModelGeometry);
            eventBus.addListener(ClientModInitializer::onRegisterMenuScreens);
            eventBus.addListener(ClientModInitializer::onRegisterKeyMappings);
            eventBus.addListener(ClientModInitializer::onRegisterTooltipFactories);
        }

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onRegister);
        eventBus.addListener(this::registerNetworkPackets);
        eventBus.addListener(this::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(this::registerWrenchingEvent);
        NeoForge.EVENT_BUS.addListener(this::registerSecurityBlockBreakEvent);
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
            new ItemHandlerImporterTransferStrategyFactory()
        );
        PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FluidHandlerImporterTransferStrategyFactory()
        );
    }

    private void registerExporterTransferStrategyFactories() {
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new ItemHandlerExporterTransferStrategyFactory()
        );
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new FluidHandlerExporterTransferStrategyFactory()
        );
    }

    private void registerExternalStorageProviderFactories() {
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(new InterfacePlatformExternalStorageProviderFactory());
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(new ItemHandlerPlatformExternalStorageProviderFactory());
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            new FluidHandlerPlatformExternalStorageProviderFactory()
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
        registerBlocks(
            new ForgeRegistryCallback<>(blockRegistry),
            ForgeDiskDriveBlockEntity::new,
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.CREATIVE, pos, state),
            ForgeDiskInterfaceBlockEntity::new
        );
        blockRegistry.register(eventBus);
    }

    private void registerItems(final IEventBus eventBus) {
        final RegistryCallback<Item> callback = new ForgeRegistryCallback<>(itemRegistry);
        registerItems(callback);
        registerCustomItems(callback);
        itemRegistry.register(eventBus);
    }

    private void registerCustomItems(final RegistryCallback<Item> callback) {
        Items.INSTANCE.setRegulatorUpgrade(callback.register(REGULATOR_UPGRADE, () -> new RegulatorUpgradeItem(
            PlatformApi.INSTANCE.getUpgradeRegistry()
        ) {
            @Override
            public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                       final ItemStack newStack,
                                                       final boolean slotChanged) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setWirelessGrid(callback.register(WIRELESS_GRID, () -> new WirelessGridItem() {
            @Override
            public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                       final ItemStack newStack,
                                                       final boolean slotChanged) {
                return AbstractModInitializer.allowComponentsUpdateAnimation(oldStack, newStack);
            }
        }));
        Items.INSTANCE.setCreativeWirelessGrid(callback.register(
            CREATIVE_WIRELESS_GRID,
            () -> new WirelessGridItem() {
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
    }

    private void registerBlockEntities(final IEventBus eventBus) {
        registerBlockEntities(
            new ForgeRegistryCallback<>(blockEntityTypeRegistry),
            new BlockEntityTypeFactory() {
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntitySupplier<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            },
            ForgeDiskDriveBlockEntity::new,
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.CREATIVE, pos, state),
            ForgeDiskInterfaceBlockEntity::new
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
            createIdentifier("general"),
            CreativeModeTab.builder()
                .title(ContentNames.MOD)
                .icon(() -> new ItemStack(Blocks.INSTANCE.getController().getDefault()))
                .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
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
        final BlockEntity blockEntity = e.getLevel().getBlockEntity(e.getPos());
        if (blockEntity instanceof NetworkNodeContainerBlockEntity networkNodeContainerBlockEntity
            && e.getPlayer() instanceof ServerPlayer serverPlayer
            && !networkNodeContainerBlockEntity.canBuild(serverPlayer)) {
            PlatformApi.INSTANCE.sendNoPermissionMessage(
                serverPlayer,
                createTranslation("misc", "no_permission.build.break", e.getState().getBlock().getName())
            );
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void registerNetworkPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);
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
            WirelessTransmitterRangePacket.PACKET_TYPE,
            WirelessTransmitterRangePacket.STREAM_CODEC,
            wrapHandler(WirelessTransmitterRangePacket::handle)
        );
        registrar.playToClient(
            NoPermissionPacket.PACKET_TYPE,
            NoPermissionPacket.STREAM_CODEC,
            wrapHandler((packet, ctx) -> NoPermissionPacket.handle(packet))
        );
    }

    private static void registerClientToServerPackets(final PayloadRegistrar registrar) {
        registrar.playToServer(
            CraftingGridClearPacket.PACKET_TYPE,
            CraftingGridClearPacket.STREAM_CODEC,
            wrapHandler(CraftingGridClearPacket::handle)
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
            UseNetworkBoundItemPacket.PACKET_TYPE,
            UseNetworkBoundItemPacket.STREAM_CODEC,
            wrapHandler(UseNetworkBoundItemPacket::handle)
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
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> wrapHandler(final PacketHandler<T> handler) {
        return (packet, ctx) -> handler.handle(packet, ctx::player);
    }

    @SubscribeEvent
    public void onServerTick(final ServerTickEvent.Pre e) {
        ServerEventQueue.runQueuedActions();
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return registry.register(id.getPath(), value);
        }
    }
}

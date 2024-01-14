package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.PlatformProxy;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.RegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.PortableGridBlockItem;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.PortableGridType;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.ServerEventQueue;
import com.refinedmods.refinedstorage2.platform.forge.exporter.FluidHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.exporter.ItemHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.forge.importer.FluidHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.importer.ItemHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.forge.storage.externalstorage.FluidHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.storage.externalstorage.ItemHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.storage.portablegrid.ForgePortableGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.forge.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage2.platform.forge.support.network.bounditem.CuriosSlotReferenceFactory;
import com.refinedmods.refinedstorage2.platform.forge.support.network.bounditem.CuriosSlotReferenceProvider;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.CraftingGridClearPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.CraftingGridRecipeTransferPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.ResourceFilterSlotChangePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.ResourceSlotAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.ResourceSlotChangePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.SingleAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.c2s.UseNetworkBoundItemPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.EnergyInfoPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.GridActivePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.GridClearPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.GridUpdatePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.NetworkTransmitterStatusPacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.ResourceSlotUpdatePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.StorageInfoResponsePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.packet.s2c.WirelessTransmitterRangePacket;
import com.refinedmods.refinedstorage2.platform.forge.support.resource.ResourceContainerFluidHandlerAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

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

    public ModInitializer(final IEventBus eventBus) {
        PlatformProxy.loadPlatform(new PlatformImpl());
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
        registerSlotReferenceProviders();

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
            new FluidHandlerPlatformExternalStorageProviderFactory());
    }

    private void registerContent(final IEventBus eventBus) {
        registerBlocks(eventBus);
        registerItems(eventBus);
        registerBlockEntities(eventBus);
        registerMenus(eventBus);
    }

    private void registerBlocks(final IEventBus eventBus) {
        registerBlocks(
            new ForgeRegistryCallback<>(blockRegistry),
            ForgeDiskDriveBlockEntity::new,
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.NORMAL, pos, state),
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.CREATIVE, pos, state)
        );
        blockRegistry.register(eventBus);
    }

    private void registerItems(final IEventBus eventBus) {
        registerItems(
            new ForgeRegistryCallback<>(itemRegistry),
            () -> new RegulatorUpgradeItem(PlatformApi.INSTANCE.getUpgradeRegistry()) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            },
            () -> new WirelessGridItem(false) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            },
            () -> new WirelessGridItem(true) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            },
            () -> new PortableGridBlockItem(Blocks.INSTANCE.getPortableGrid(), PortableGridType.NORMAL) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            },
            () -> new PortableGridBlockItem(Blocks.INSTANCE.getCreativePortableGrid(), PortableGridType.CREATIVE) {
                @Override
                public boolean shouldCauseReequipAnimation(final ItemStack oldStack,
                                                           final ItemStack newStack,
                                                           final boolean slotChanged) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            }
        );
        itemRegistry.register(eventBus);
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
            (pos, state) -> new ForgePortableGridBlockEntity(PortableGridType.CREATIVE, pos, state)
        );
        blockEntityTypeRegistry.register(eventBus);
    }

    private void registerMenus(final IEventBus eventBus) {
        registerMenus(new ForgeRegistryCallback<>(menuTypeRegistry), new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return IMenuTypeExtension.create(supplier::create);
            }
        });
        menuTypeRegistry.register(eventBus);
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
            (stack, ctx) -> new EnergyStorageAdapter(Items.INSTANCE.getPortableGrid().createEnergyStorage(stack)),
            Items.INSTANCE.getPortableGrid()
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

    protected void registerSlotReferenceProviders() {
        CuriosSlotReferenceProvider.create().ifPresent(slotReferenceProvider -> {
            PlatformApi.INSTANCE.getSlotReferenceFactoryRegistry().register(
                createIdentifier("curios"),
                CuriosSlotReferenceFactory.INSTANCE
            );
            PlatformApi.INSTANCE.addSlotReferenceProvider(slotReferenceProvider);
        });
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
                .title(createTranslation("itemGroup", "general"))
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
    public void registerNetworkPackets(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(MOD_ID);
        registerServerToClientPackets(registrar);
        registerClientToServerPackets(registrar);
    }

    private static void registerServerToClientPackets(final IPayloadRegistrar registrar) {
        registrar.play(
            PacketIds.ENERGY_INFO,
            EnergyInfoPacket::decode,
            handler -> handler.client(EnergyInfoPacket::handle)
        );
        registrar.play(
            PacketIds.GRID_ACTIVE,
            GridActivePacket::decode,
            handler -> handler.client(GridActivePacket::handle)
        );
        registrar.play(
            PacketIds.GRID_CLEAR,
            buf -> new GridClearPacket(),
            handler -> handler.client((packet, ctx) -> GridClearPacket.handle(ctx))
        );
        registrar.play(
            PacketIds.GRID_UPDATE,
            GridUpdatePacket::decode,
            handler -> handler.client(GridUpdatePacket::handle)
        );
        registrar.play(
            PacketIds.NETWORK_TRANSMITTER_STATUS,
            NetworkTransmitterStatusPacket::decode,
            handler -> handler.client(NetworkTransmitterStatusPacket::handle)
        );
        registrar.play(
            PacketIds.RESOURCE_SLOT_UPDATE,
            ResourceSlotUpdatePacket::decode,
            handler -> handler.client(ResourceSlotUpdatePacket::handle)
        );
        registrar.play(
            PacketIds.STORAGE_INFO_RESPONSE,
            StorageInfoResponsePacket::decode,
            handler -> handler.client(StorageInfoResponsePacket::handle)
        );
        registrar.play(
            PacketIds.WIRELESS_TRANSMITTER_RANGE,
            WirelessTransmitterRangePacket::decode,
            handler -> handler.client(WirelessTransmitterRangePacket::handle)
        );
    }

    private static void registerClientToServerPackets(final IPayloadRegistrar registrar) {
        registrar.play(
            PacketIds.CRAFTING_GRID_CLEAR,
            CraftingGridClearPacket::decode,
            handler -> handler.server(CraftingGridClearPacket::handle)
        );
        registrar.play(
            PacketIds.CRAFTING_GRID_RECIPE_TRANSFER,
            CraftingGridRecipeTransferPacket::decode,
            handler -> handler.server(CraftingGridRecipeTransferPacket::handle)
        );
        registrar.play(
            PacketIds.GRID_EXTRACT,
            GridExtractPacket::decode,
            handler -> handler.server(GridExtractPacket::handle)
        );
        registrar.play(
            PacketIds.GRID_INSERT,
            GridInsertPacket::decode,
            handler -> handler.server(GridInsertPacket::handle)
        );
        registrar.play(
            PacketIds.GRID_SCROLL,
            GridScrollPacket::decode,
            handler -> handler.server(GridScrollPacket::handle)
        );
        registrar.play(
            PacketIds.PROPERTY_CHANGE,
            PropertyChangePacket::decode,
            handler -> handler.server(PropertyChangePacket::handle)
        );
        registrar.play(
            PacketIds.RESOURCE_FILTER_SLOT_CHANGE,
            ResourceFilterSlotChangePacket::decode,
            handler -> handler.server(ResourceFilterSlotChangePacket::handle)
        );
        registrar.play(
            PacketIds.RESOURCE_SLOT_AMOUNT_CHANGE,
            ResourceSlotAmountChangePacket::decode,
            handler -> handler.server(ResourceSlotAmountChangePacket::handle)
        );
        registrar.play(
            PacketIds.RESOURCE_SLOT_CHANGE,
            ResourceSlotChangePacket::decode,
            handler -> handler.server(ResourceSlotChangePacket::handle)
        );
        registrar.play(
            PacketIds.SINGLE_AMOUNT_CHANGE,
            SingleAmountChangePacket::decode,
            handler -> handler.server(SingleAmountChangePacket::handle)
        );
        registrar.play(
            PacketIds.STORAGE_INFO_REQUEST,
            StorageInfoRequestPacket::decode,
            handler -> handler.server(StorageInfoRequestPacket::handle)
        );
        registrar.play(
            PacketIds.USE_NETWORK_BOUND_ITEM,
            UseNetworkBoundItemPacket::decode,
            handler -> handler.server(UseNetworkBoundItemPacket::handle)
        );
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            ServerEventQueue.runQueuedActions();
        }
    }

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return registry.register(id.getPath(), value);
        }
    }
}

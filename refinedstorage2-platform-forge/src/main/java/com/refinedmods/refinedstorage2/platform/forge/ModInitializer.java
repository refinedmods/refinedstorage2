package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyItem;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.PlatformProxy;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.RegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.ServerEventQueue;
import com.refinedmods.refinedstorage2.platform.forge.exporter.FluidHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.exporter.ItemHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.externalstorage.FluidHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.externalstorage.ItemHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.forge.importer.FluidHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.importer.ItemHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;
import com.refinedmods.refinedstorage2.platform.forge.storage.diskdrive.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.forge.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage2.platform.forge.support.network.bounditem.CuriosSlotReferenceFactory;
import com.refinedmods.refinedstorage2.platform.forge.support.network.bounditem.CuriosSlotReferenceProvider;
import com.refinedmods.refinedstorage2.platform.forge.support.resource.ResourceContainerFluidHandlerAdapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

@Mod(IdentifierUtil.MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private final DeferredRegister<Block> blockRegistry =
        DeferredRegister.create(ForgeRegistries.BLOCKS, IdentifierUtil.MOD_ID);
    private final DeferredRegister<Item> itemRegistry =
        DeferredRegister.create(ForgeRegistries.ITEMS, IdentifierUtil.MOD_ID);
    private final DeferredRegister<BlockEntityType<?>> blockEntityTypeRegistry =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IdentifierUtil.MOD_ID);
    private final DeferredRegister<MenuType<?>> menuTypeRegistry =
        DeferredRegister.create(ForgeRegistries.MENU_TYPES, IdentifierUtil.MOD_ID);
    private final DeferredRegister<SoundEvent> soundEventRegistry =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IdentifierUtil.MOD_ID);
    private final DeferredRegister<RecipeSerializer<?>> recipeSerializerRegistry =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, IdentifierUtil.MOD_ID);

    public ModInitializer() {
        PlatformProxy.loadPlatform(new PlatformImpl(new NetworkManager()));
        initializePlatformApi();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerContent();
        registerSounds();
        registerRecipeSerializers();
        registerTickHandler();
        registerSlotReferenceProviders();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onClientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onRegisterModelGeometry);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onRegisterKeyMappings);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(
                ClientModInitializer::onRegisterTooltipFactories
            );
        });

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegister);
        MinecraftForge.EVENT_BUS.addListener(this::registerWrenchingEvent);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::registerBlockEntityCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, this::registerItemStackCapabilities);
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

    private void registerContent() {
        registerBlocks();
        registerItems();
        registerBlockEntities();
        registerMenus();
    }

    private void registerBlocks() {
        registerBlocks(new ForgeRegistryCallback<>(blockRegistry), ForgeDiskDriveBlockEntity::new);
        blockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerItems() {
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
            }
        );
        itemRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerBlockEntities() {
        registerBlockEntities(
            new ForgeRegistryCallback<>(blockEntityTypeRegistry),
            new BlockEntityTypeFactory() {
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntitySupplier<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            },
            ForgeDiskDriveBlockEntity::new
        );
        blockEntityTypeRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerMenus() {
        registerMenus(new ForgeRegistryCallback<>(menuTypeRegistry), new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return IForgeMenuType.create(supplier::create);
            }
        });
        menuTypeRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerSounds() {
        registerSounds(new ForgeRegistryCallback<>(soundEventRegistry));
        soundEventRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerRecipeSerializers() {
        registerRecipeSerializers(new ForgeRegistryCallback<>(recipeSerializerRegistry));
        recipeSerializerRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerTickHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
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
    public void registerBlockEntityCapabilities(final AttachCapabilitiesEvent<BlockEntity> e) {
        if (e.getObject() instanceof EnergyBlockEntity energyBlockEntity) {
            registerEnergyBlockEntity(e, energyBlockEntity);
        }
        if (e.getObject() instanceof AbstractDiskDriveBlockEntity diskDriveBlockEntity) {
            registerItemHandler(e, diskDriveBlockEntity, AbstractDiskDriveBlockEntity::getDiskInventory);
        }
        if (e.getObject() instanceof InterfaceBlockEntity interfaceBlockEntity) {
            registerItemHandler(e, interfaceBlockEntity, InterfaceBlockEntity::getExportedResourcesAsContainer);
            registerFluidHandler(
                e,
                interfaceBlockEntity,
                blockEntity -> new ResourceContainerFluidHandlerAdapter(blockEntity.getExportedResources())
            );
        }
    }

    private <T extends BlockEntity> void registerItemHandler(final AttachCapabilitiesEvent<BlockEntity> e,
                                                             final T blockEntity,
                                                             final Function<T, Container> containerSupplier) {
        final LazyOptional<IItemHandler> capability = LazyOptional.of(() -> new InvWrapper(
            containerSupplier.apply(blockEntity)
        ));
        e.addCapability(createIdentifier("items"), new ICapabilityProvider() {
            @Override
            @Nonnull
            public <C> LazyOptional<C> getCapability(final Capability<C> cap, @Nullable final Direction side) {
                if (cap == ForgeCapabilities.ITEM_HANDLER) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    private <T extends BlockEntity> void registerFluidHandler(final AttachCapabilitiesEvent<BlockEntity> e,
                                                              final T blockEntity,
                                                              final Function<T, IFluidHandler> handlerSupplier) {
        final LazyOptional<IFluidHandler> capability = LazyOptional.of(() -> handlerSupplier.apply(blockEntity));
        e.addCapability(createIdentifier("fluids"), new ICapabilityProvider() {
            @Override
            @Nonnull
            public <C> LazyOptional<C> getCapability(final Capability<C> cap, @Nullable final Direction side) {
                if (cap == ForgeCapabilities.FLUID_HANDLER) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    private void registerEnergyBlockEntity(final AttachCapabilitiesEvent<BlockEntity> e,
                                           final EnergyBlockEntity energyBlockEntity) {
        final LazyOptional<IEnergyStorage> capability = LazyOptional.of(
            () -> new EnergyStorageAdapter(energyBlockEntity.getEnergyStorage())
        );
        e.addCapability(createIdentifier("energy"), new ICapabilityProvider() {
            @Override
            @Nonnull
            public <T> LazyOptional<T> getCapability(final Capability<T> cap,
                                                     @Nullable final Direction side) {
                if (cap == ForgeCapabilities.ENERGY) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    @SubscribeEvent
    public void registerItemStackCapabilities(final AttachCapabilitiesEvent<ItemStack> e) {
        if (e.getObject().getItem() instanceof EnergyItem energyItem) {
            final Optional<EnergyStorage> energyStorage = energyItem.createEnergyStorage(e.getObject());
            final LazyOptional<IEnergyStorage> forgeEnergyStorage = energyStorage.map(
                storage -> LazyOptional.of(() -> (IEnergyStorage) new EnergyStorageAdapter(storage))
            ).orElse(LazyOptional.empty());
            e.addCapability(createIdentifier("energy"), new ICapabilityProvider() {
                @Override
                public <T> LazyOptional<T> getCapability(final Capability<T> cap,
                                                         @Nullable final Direction side) {
                    if (cap == ForgeCapabilities.ENERGY) {
                        return forgeEnergyStorage.cast();
                    }
                    return LazyOptional.empty();
                }
            });
        }
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

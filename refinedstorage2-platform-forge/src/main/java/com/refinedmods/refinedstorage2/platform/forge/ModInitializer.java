package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ConstructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.DestructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.RegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.recipe.UpgradeWithEnchantedBookRecipeSerializer;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.forge.block.entity.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.forge.internal.grid.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.forge.internal.network.node.exporter.FluidHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.network.node.exporter.ItemHandlerExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.network.node.externalstorage.FluidHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.network.node.externalstorage.ItemHandlerPlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer.FluidHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer.ItemHandlerImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
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
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONSTRUCTOR;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CRAFTING_GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DESTRUCTOR;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DETECTOR;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.EXPORTER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.EXTERNAL_STORAGE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.FLUID_STORAGE_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.IMPORTER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.INTERFACE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.ITEM_STORAGE_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.STORAGE_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.WRENCH;
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
        initializePlatform(new PlatformImpl(new NetworkManager()));
        initializePlatformApi();
        registerAdditionalStorageTypes();
        registerAdditionalStorageChannelTypes();
        registerAdditionalFilteredResourceFactories();
        registerDestructorStrategyFactories();
        registerConstructorStrategyFactories();
        registerAlternativeGridHints();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerNetworkComponents();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerContent();
        registerSounds();
        registerRecipeSerializers();
        registerTickHandler();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onClientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onRegisterModelGeometry);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onRegisterKeyMappings);
            FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(ClientModInitializer::onRegisterTooltipFactories);
        });

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegister);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::registerCapabilities);
    }

    private void registerAdditionalGridInsertionStrategyFactories() {
        PlatformApi.INSTANCE.addGridInsertionStrategyFactory(FluidGridInsertionStrategy::new);
    }

    private void registerGridExtractionStrategyFactories() {
        PlatformApi.INSTANCE.addGridExtractionStrategyFactory(
            (containerMenu, player, gridServiceFactory, itemStorage) ->
                new ItemGridExtractionStrategy(containerMenu, player, gridServiceFactory)
        );
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
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            StorageChannelTypes.ITEM,
            new ItemHandlerPlatformExternalStorageProviderFactory()
        );
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            StorageChannelTypes.FLUID,
            new FluidHandlerPlatformExternalStorageProviderFactory()
        );
    }

    private void registerContent() {
        registerBlocks();
        registerItems();
        registerBlockEntities();
        registerMenus();
    }

    private void registerBlocks() {
        registerBlocks(new RegistryCallback<>() {
            @Override
            public <R extends Block> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
                return blockRegistry.register(id.getPath(), value);
            }
        }, ForgeDiskDriveBlockEntity::new);
        blockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerItems() {
        registerItems(new RegistryCallback<>() {
            @Override
            public <R extends Item> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
                return itemRegistry.register(id.getPath(), value);
            }
        });
        itemRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerBlockEntities() {
        registerBlockEntities(
            new RegistryCallback<>() {
                @Override
                public <R extends BlockEntityType<?>> Supplier<R> register(final ResourceLocation id,
                                                                           final Supplier<R> value) {
                    return blockEntityTypeRegistry.register(id.getPath(), value);
                }
            },
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
        Menus.INSTANCE.setController(menuTypeRegistry.register(
            CONTROLLER.getPath(),
            () -> IForgeMenuType.create(ControllerContainerMenu::new)
        ));
        Menus.INSTANCE.setDiskDrive(menuTypeRegistry.register(
            DISK_DRIVE.getPath(),
            () -> IForgeMenuType.create(DiskDriveContainerMenu::new)
        ));
        Menus.INSTANCE.setGrid(menuTypeRegistry.register(
            GRID.getPath(),
            () -> IForgeMenuType.create(GridContainerMenu::new)
        ));
        Menus.INSTANCE.setCraftingGrid(menuTypeRegistry.register(
            CRAFTING_GRID.getPath(),
            () -> IForgeMenuType.create(CraftingGridContainerMenu::new)
        ));
        Menus.INSTANCE.setItemStorage(menuTypeRegistry.register(
            ITEM_STORAGE_BLOCK.getPath(),
            () -> IForgeMenuType.create(ItemStorageBlockContainerMenu::new)
        ));
        Menus.INSTANCE.setFluidStorage(menuTypeRegistry.register(
            FLUID_STORAGE_BLOCK.getPath(),
            () -> IForgeMenuType.create(FluidStorageBlockContainerMenu::new)
        ));
        Menus.INSTANCE.setImporter(menuTypeRegistry.register(
            IMPORTER.getPath(),
            () -> IForgeMenuType.create(ImporterContainerMenu::new)
        ));
        Menus.INSTANCE.setExporter(menuTypeRegistry.register(
            EXPORTER.getPath(),
            () -> IForgeMenuType.create(ExporterContainerMenu::new)
        ));
        Menus.INSTANCE.setInterface(menuTypeRegistry.register(
            INTERFACE.getPath(),
            () -> IForgeMenuType.create(InterfaceContainerMenu::new)
        ));
        Menus.INSTANCE.setExternalStorage(menuTypeRegistry.register(
            EXTERNAL_STORAGE.getPath(),
            () -> IForgeMenuType.create(ExternalStorageContainerMenu::new)
        ));
        Menus.INSTANCE.setDetector(menuTypeRegistry.register(
            DETECTOR.getPath(),
            () -> IForgeMenuType.create(DetectorContainerMenu::new)
        ));
        Menus.INSTANCE.setDestructor(menuTypeRegistry.register(
            DESTRUCTOR.getPath(),
            () -> IForgeMenuType.create(DestructorContainerMenu::new)
        ));
        Menus.INSTANCE.setConstructor(menuTypeRegistry.register(
            CONSTRUCTOR.getPath(),
            () -> IForgeMenuType.create(ConstructorContainerMenu::new)
        ));

        menuTypeRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerSounds() {
        Sounds.INSTANCE.setWrench(soundEventRegistry.register(
            WRENCH.getPath(),
            () -> SoundEvent.createVariableRangeEvent(WRENCH)
        ));

        soundEventRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerRecipeSerializers() {
        recipeSerializerRegistry.register("upgrade_with_enchanted_book", UpgradeWithEnchantedBookRecipeSerializer::new);
        recipeSerializerRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerTickHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @SubscribeEvent
    public void onCommonSetup(final FMLCommonSetupEvent e) {
        registerUpgradeMappings();
    }

    @SubscribeEvent
    public void onRegister(final RegisterEvent e) {
        e.register(Registries.LOOT_FUNCTION_TYPE, helper -> {
            // We don't use the helper here as we need the return value.
            final LootItemFunctionType storageBlockLootItemFunction = Registry.register(
                BuiltInRegistries.LOOT_FUNCTION_TYPE,
                STORAGE_BLOCK,
                new LootItemFunctionType(new AbstractStorageBlock.StorageBlockLootItemFunctionSerializer())
            );
            LootFunctions.INSTANCE.setStorageBlock(() -> storageBlockLootItemFunction);
        });

        e.register(Registries.CREATIVE_MODE_TAB, helper -> {
            helper.register(
                createIdentifier("general"),
                CreativeModeTab.builder()
                    .title(createTranslation("itemGroup", "general"))
                    .icon(() -> new ItemStack(Blocks.INSTANCE.getController().getDefault()))
                    .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
                    .build()
            );
        });
    }

    @SubscribeEvent
    public void onRightClickBlock(final PlayerInteractEvent.RightClickBlock e) {
        final BlockState state = e.getLevel().getBlockState(e.getHitVec().getBlockPos());

        AbstractBaseBlock.tryUseWrench(state, e.getLevel(), e.getHitVec(), e.getEntity(), e.getHand())
            .or(() -> AbstractBaseBlock.tryUpdateColor(
                state,
                e.getLevel(),
                e.getHitVec().getBlockPos(),
                e.getEntity(),
                e.getHand()
            ))
            .ifPresent(result -> {
                e.setCanceled(true);
                e.setCancellationResult(result);
            });
    }

    @SubscribeEvent
    public void registerCapabilities(final AttachCapabilitiesEvent<BlockEntity> e) {
        if (e.getObject() instanceof ControllerBlockEntity controllerBlockEntity) {
            registerControllerEnergy(e, controllerBlockEntity);
        }
        if (e.getObject() instanceof AbstractDiskDriveBlockEntity diskDriveBlockEntity) {
            registerItemHandler(e, diskDriveBlockEntity, AbstractDiskDriveBlockEntity::getDiskInventory);
        }
        if (e.getObject() instanceof InterfaceBlockEntity interfaceBlockEntity) {
            registerItemHandler(e, interfaceBlockEntity, InterfaceBlockEntity::getExportedItems);
        }
    }

    private <T extends BlockEntity> void registerItemHandler(final AttachCapabilitiesEvent<BlockEntity> e,
                                                             final T diskDriveBlockEntity,
                                                             final Function<T, Container> containerSupplier) {
        final LazyOptional<IItemHandler> capability = LazyOptional
            .of(() -> new InvWrapper(containerSupplier.apply(diskDriveBlockEntity)));
        e.addCapability(createIdentifier("items"), new ICapabilityProvider() {
            @Override
            @Nonnull
            public <C> LazyOptional<C> getCapability(final Capability<C> cap,
                                                     @Nullable final Direction side) {
                if (cap == ForgeCapabilities.ITEM_HANDLER) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    private void registerControllerEnergy(final AttachCapabilitiesEvent<BlockEntity> e,
                                          final ControllerBlockEntity controllerBlockEntity) {
        final LazyOptional<IEnergyStorage> capability = LazyOptional
            .of(() -> (IEnergyStorage) controllerBlockEntity.getEnergyStorage());
        e.addCapability(createIdentifier("energy"), new ICapabilityProvider() {
            @Override
            @Nonnull
            public <T> LazyOptional<T> getCapability(final Capability<T> cap,
                                                     @Nullable final Direction side) {
                if (cap == ForgeCapabilities.ENERGY
                    && controllerBlockEntity.getEnergyStorage() instanceof IEnergyStorage) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            TickHandler.runQueuedActions();
        }
    }
}

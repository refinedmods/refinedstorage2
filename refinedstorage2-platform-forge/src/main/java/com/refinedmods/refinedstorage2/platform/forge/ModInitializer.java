package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.RegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
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
        registerBlocks(new ForgeRegistryCallback<>(blockRegistry), ForgeDiskDriveBlockEntity::new);
        blockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerItems() {
        registerItems(new ForgeRegistryCallback<>(itemRegistry));
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

    private record ForgeRegistryCallback<T>(DeferredRegister<T> registry) implements RegistryCallback<T> {
        @Override
        public <R extends T> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
            return registry.register(id.getPath(), value);
        }
    }
}

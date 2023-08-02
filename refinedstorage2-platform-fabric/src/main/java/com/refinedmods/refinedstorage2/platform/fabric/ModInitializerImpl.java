package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractStorageBlock;
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
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.RegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.internal.network.node.iface.externalstorage.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.recipe.UpgradeWithEnchantedBookRecipeSerializer;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.exporter.StorageExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.externalstorage.StoragePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.importer.StorageImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.CraftingGridClearPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.CraftingGridRecipeTransferPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.DetectorAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceFilterSlotAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceFilterSlotChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

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

public class ModInitializerImpl extends AbstractModInitializer implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModInitializerImpl.class);

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);

        initializePlatform(new PlatformImpl());
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
        registerPackets();
        registerSounds();
        registerRecipeSerializers();
        registerSidedHandlers();
        registerTickHandler();
        registerEvents();

        LOGGER.info("Refined Storage 2 has loaded.");
    }

    private void registerEvents() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            final BlockState state = level.getBlockState(hitResult.getBlockPos());
            return AbstractBaseBlock.tryUseWrench(state, level, hitResult, player, hand)
                .or(() -> AbstractBaseBlock.tryUpdateColor(state, level, hitResult.getBlockPos(), player, hand))
                .orElse(InteractionResult.PASS);
        });
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
            new StorageImporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                StorageChannelTypes.ITEM,
                VariantUtil::ofItemVariant,
                VariantUtil::toItemVariant,
                1
            )
        );
        PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new StorageImporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                StorageChannelTypes.FLUID,
                VariantUtil::ofFluidVariant,
                VariantUtil::toFluidVariant,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExporterTransferStrategyFactories() {
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new StorageExporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                StorageChannelTypes.ITEM,
                resource -> resource instanceof ItemResource itemResource
                    ? Optional.of(itemResource)
                    : Optional.empty(),
                VariantUtil::toItemVariant,
                1
            )
        );
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new StorageExporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                StorageChannelTypes.FLUID,
                resource -> resource instanceof FluidResource fluidResource
                    ? Optional.of(fluidResource)
                    : Optional.empty(),
                VariantUtil::toFluidVariant,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExternalStorageProviderFactories() {
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            StorageChannelTypes.ITEM,
            new InterfacePlatformExternalStorageProviderFactory()
        );
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            StorageChannelTypes.ITEM,
            new StoragePlatformExternalStorageProviderFactory<>(
                ItemStorage.SIDED,
                VariantUtil::ofItemVariant,
                VariantUtil::toItemVariant
            )
        );
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(
            StorageChannelTypes.FLUID,
            new StoragePlatformExternalStorageProviderFactory<>(
                FluidStorage.SIDED,
                VariantUtil::ofFluidVariant,
                VariantUtil::toFluidVariant
            )
        );
    }

    private void registerContent() {
        registerBlocks(new RegistryCallback<>() {
            @Override
            public <R extends Block> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
                return ModInitializerImpl.register(BuiltInRegistries.BLOCK, id, value.get());
            }
        }, FabricDiskDriveBlockEntity::new);
        registerItems(new RegistryCallback<>() {
            @Override
            public <R extends Item> Supplier<R> register(final ResourceLocation id, final Supplier<R> value) {
                return ModInitializerImpl.register(BuiltInRegistries.ITEM, id, value.get());
            }
        });
        registerUpgradeMappings();
        registerCreativeModeTab();
        registerBlockEntities(
            new RegistryCallback<>() {
                @Override
                public <R extends BlockEntityType<?>> Supplier<R> register(final ResourceLocation id,
                                                                           final Supplier<R> value) {
                    return ModInitializerImpl.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, value.get());
                }
            },
            new BlockEntityTypeFactory() {
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntitySupplier<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            },
            FabricDiskDriveBlockEntity::new
        );
        registerMenus();
        registerLootFunctions();
    }

    private static <T, R extends T> Supplier<R> register(final Registry<T> registry,
                                                         final ResourceLocation id,
                                                         final R value) {
        final R result = Registry.register(registry, id, value);
        return () -> result;
    }

    private void registerMenus() {
        Menus.INSTANCE.setDiskDrive(register(
            BuiltInRegistries.MENU,
            DISK_DRIVE,
            new ExtendedScreenHandlerType<>(DiskDriveContainerMenu::new)
        ));
        Menus.INSTANCE.setGrid(register(
            BuiltInRegistries.MENU,
            GRID,
            new ExtendedScreenHandlerType<>(GridContainerMenu::new)
        ));
        Menus.INSTANCE.setCraftingGrid(register(
            BuiltInRegistries.MENU,
            CRAFTING_GRID,
            new ExtendedScreenHandlerType<>(CraftingGridContainerMenu::new)
        ));
        Menus.INSTANCE.setController(register(
            BuiltInRegistries.MENU,
            CONTROLLER,
            new ExtendedScreenHandlerType<>(ControllerContainerMenu::new)
        ));
        Menus.INSTANCE.setItemStorage(register(
            BuiltInRegistries.MENU,
            ITEM_STORAGE_BLOCK,
            new ExtendedScreenHandlerType<>(ItemStorageBlockContainerMenu::new)
        ));
        Menus.INSTANCE.setFluidStorage(register(
            BuiltInRegistries.MENU,
            FLUID_STORAGE_BLOCK,
            new ExtendedScreenHandlerType<>(FluidStorageBlockContainerMenu::new)
        ));
        Menus.INSTANCE.setImporter(register(
            BuiltInRegistries.MENU,
            IMPORTER,
            new ExtendedScreenHandlerType<>(ImporterContainerMenu::new)
        ));
        Menus.INSTANCE.setExporter(register(
            BuiltInRegistries.MENU,
            EXPORTER,
            new ExtendedScreenHandlerType<>(ExporterContainerMenu::new)
        ));
        Menus.INSTANCE.setInterface(register(
            BuiltInRegistries.MENU,
            INTERFACE,
            new ExtendedScreenHandlerType<>(InterfaceContainerMenu::new)
        ));
        Menus.INSTANCE.setExternalStorage(register(
            BuiltInRegistries.MENU,
            EXTERNAL_STORAGE,
            new ExtendedScreenHandlerType<>(ExternalStorageContainerMenu::new)
        ));
        Menus.INSTANCE.setDetector(register(
            BuiltInRegistries.MENU,
            DETECTOR,
            new ExtendedScreenHandlerType<>(DetectorContainerMenu::new)
        ));
        Menus.INSTANCE.setDestructor(register(
            BuiltInRegistries.MENU,
            DESTRUCTOR,
            new ExtendedScreenHandlerType<>(DestructorContainerMenu::new)
        ));
        Menus.INSTANCE.setConstructor(register(
            BuiltInRegistries.MENU,
            CONSTRUCTOR,
            new ExtendedScreenHandlerType<>(ConstructorContainerMenu::new)
        ));
    }

    private void registerLootFunctions() {
        LootFunctions.INSTANCE.setStorageBlock(register(
            BuiltInRegistries.LOOT_FUNCTION_TYPE,
            STORAGE_BLOCK,
            new LootItemFunctionType(new AbstractStorageBlock.StorageBlockLootItemFunctionSerializer())
        ));
    }

    private void registerCreativeModeTab() {
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            createIdentifier("general"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(createTranslation("itemGroup", "general"))
                .icon(() -> new ItemStack(Blocks.INSTANCE.getController().getDefault()))
                .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
                .build()
        );
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_REQUEST, new StorageInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_INSERT, new GridInsertPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_EXTRACT, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_SCROLL, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.CRAFTING_GRID_CLEAR, new CraftingGridClearPacket());
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.CRAFTING_GRID_RECIPE_TRANSFER,
            new CraftingGridRecipeTransferPacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.PROPERTY_CHANGE, new PropertyChangePacket());
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.RESOURCE_FILTER_SLOT_AMOUNT_CHANGE,
            new ResourceFilterSlotAmountChangePacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.RESOURCE_FILTER_SLOT_CHANGE,
            new ResourceFilterSlotChangePacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.DETECTOR_AMOUNT_CHANGE, new DetectorAmountChangePacket());
    }

    private void registerSounds() {
        Sounds.INSTANCE.setWrench(register(
            BuiltInRegistries.SOUND_EVENT,
            WRENCH,
            SoundEvent.createVariableRangeEvent(WRENCH)
        ));
    }

    private void registerRecipeSerializers() {
        register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            createIdentifier("upgrade_with_enchanted_book"),
            new UpgradeWithEnchantedBookRecipeSerializer()
        );
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
            InterfaceBlockEntity::getExportedItems,
            BlockEntities.INSTANCE.getInterface()
        );
        registerControllerEnergy();
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

    private void registerControllerEnergy() {
        EnergyStorage.SIDED.registerForBlockEntity(
            (be, direction) -> ((ControllerTeamRebornEnergy) be.getEnergyStorage()).getExposedStorage(),
            BlockEntities.INSTANCE.getController()
        );
    }

    private void registerTickHandler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> TickHandler.runQueuedActions());
    }
}

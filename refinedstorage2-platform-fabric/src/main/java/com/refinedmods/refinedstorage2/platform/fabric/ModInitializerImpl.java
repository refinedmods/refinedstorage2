package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.block.DestructorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DetectorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExternalStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ImporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.InterfaceBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.SimpleBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ImporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.destructor.DestructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.detector.DetectorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.grid.CraftingGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.grid.GridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.ControllerBlockEntityTicker;
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
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ContentIds;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.internal.network.node.iface.externalstorage.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.FortuneUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.item.SimpleItem;
import com.refinedmods.refinedstorage2.platform.common.item.SimpleUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.CreativeControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.FluidStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ItemStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.NamedBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.SimpleBlockItem;
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

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CABLE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONSTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CRAFTING_GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CREATIVE_CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DESTRUCTION_CORE;
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
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.MACHINE_CASING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.PROCESSOR_BINDING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.QUARTZ_ENRICHED_IRON;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.QUARTZ_ENRICHED_IRON_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.SILICON;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.STORAGE_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.STORAGE_HOUSING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.WRENCH;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStorageBlock;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStorageDisk;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStoragePart;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forItemStorageBlock;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forItemStoragePart;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forProcessor;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forStorageDisk;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ModInitializerImpl extends AbstractModInitializer implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModInitializerImpl.class);
    private static final String BLOCK_TRANSLATION_CATEGORY = "block";

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);

        initializePlatform(new PlatformImpl());
        initializePlatformApi();
        registerAdditionalStorageTypes();
        registerAdditionalStorageChannelTypes();
        registerAdditionalFilteredResourceFactories();
        registerDestructorStrategyFactories();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerNetworkComponents();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerContent();
        registerCreativeModeTab();
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
            (containerMenu, player, gridServiceFactory, containerExtractionSource) ->
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
        registerBlocks();
        registerItems();
        registerBlockEntities();
        registerMenus();
        registerLootFunctions();
    }

    private <T, R extends T> Supplier<R> register(final Registry<T> registry,
                                                  final ResourceLocation id,
                                                  final R value) {
        final R result = Registry.register(registry, id, value);
        return () -> result;
    }

    private void registerBlocks() {
        Blocks.INSTANCE.getCable().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getCable().getId(color, CABLE),
            new CableBlock(color, Blocks.INSTANCE.getCable().getName(
                color,
                createTranslation(
                    BLOCK_TRANSLATION_CATEGORY,
                    "cable"
                )
            ))
        ));
        Blocks.INSTANCE.setQuartzEnrichedIronBlock(register(
            BuiltInRegistries.BLOCK,
            QUARTZ_ENRICHED_IRON_BLOCK,
            new SimpleBlock()
        ));
        Blocks.INSTANCE.setDiskDrive(register(
            BuiltInRegistries.BLOCK,
            DISK_DRIVE,
            new DiskDriveBlock(FabricDiskDriveBlockEntity::new)
        ));
        Blocks.INSTANCE.setMachineCasing(register(
            BuiltInRegistries.BLOCK,
            MACHINE_CASING,
            new SimpleBlock()
        ));

        Blocks.INSTANCE.getGrid().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getGrid().getId(color, GRID),
            new GridBlock(
                Blocks.INSTANCE.getGrid().getName(color, createTranslation(
                    BLOCK_TRANSLATION_CATEGORY,
                    "grid"
                )),
                color
            )
        ));
        Blocks.INSTANCE.getCraftingGrid().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getCraftingGrid().getId(color, CRAFTING_GRID),
            new CraftingGridBlock(
                Blocks.INSTANCE.getCraftingGrid().getName(color, createTranslation(
                    BLOCK_TRANSLATION_CATEGORY,
                    "crafting_grid"
                )),
                color
            )
        ));
        Blocks.INSTANCE.getController().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getController().getId(color, CONTROLLER),
            new ControllerBlock(
                ControllerType.NORMAL,
                Blocks.INSTANCE.getController().getName(
                    color,
                    createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")
                ),
                new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getController),
                color
            )
        ));
        Blocks.INSTANCE.getCreativeController().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER),
            new ControllerBlock(
                ControllerType.CREATIVE,
                Blocks.INSTANCE.getCreativeController().getName(
                    color,
                    createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")
                ),
                new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getCreativeController),
                color
            )
        ));

        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            Blocks.INSTANCE.setItemStorageBlock(variant, register(
                BuiltInRegistries.BLOCK,
                forItemStorageBlock(variant),
                new ItemStorageBlock(variant)
            ));
        }

        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            Blocks.INSTANCE.setFluidStorageBlock(variant, register(
                BuiltInRegistries.BLOCK,
                forFluidStorageBlock(variant),
                new FluidStorageBlock(variant)
            ));
        }

        Blocks.INSTANCE.getImporter().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getImporter().getId(color, IMPORTER),
            new ImporterBlock(color, Blocks.INSTANCE.getImporter().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "importer")
            ))
        ));
        Blocks.INSTANCE.getExporter().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getExporter().getId(color, EXPORTER),
            new ExporterBlock(color, Blocks.INSTANCE.getExporter().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "exporter")
            ))
        ));
        Blocks.INSTANCE.setInterface(register(
            BuiltInRegistries.BLOCK,
            INTERFACE,
            new InterfaceBlock()
        ));
        Blocks.INSTANCE.getExternalStorage().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getExternalStorage().getId(color, EXTERNAL_STORAGE),
            new ExternalStorageBlock(color, Blocks.INSTANCE.getExternalStorage().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "external_storage")
            ))
        ));
        Blocks.INSTANCE.getDetector().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getDetector().getId(color, DETECTOR),
            new DetectorBlock(color, Blocks.INSTANCE.getDetector().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "detector")
            ))
        ));
        Blocks.INSTANCE.getDestructor().putAll(color -> register(
            BuiltInRegistries.BLOCK,
            Blocks.INSTANCE.getDestructor().getId(color, DESTRUCTOR),
            new DestructorBlock(color, Blocks.INSTANCE.getDestructor().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "destructor")
            ))
        ));
    }

    private void registerItems() {
        registerSimpleItems();
        registerGridItems();
        registerCableItems();
        registerControllerItems();
        registerDetectorItems();
        registerImporterItems();
        registerExporterItems();
        registerExternalStorageItems();
        registerDestructorItems();
        registerStorageItems();
        registerUpgrades();
    }

    private void registerSimpleItems() {
        Items.INSTANCE.setQuartzEnrichedIron(register(
            BuiltInRegistries.ITEM,
            QUARTZ_ENRICHED_IRON,
            new SimpleItem()
        ));
        register(
            BuiltInRegistries.ITEM,
            QUARTZ_ENRICHED_IRON_BLOCK,
            new SimpleBlockItem(Blocks.INSTANCE.getQuartzEnrichedIronBlock())
        );
        Items.INSTANCE.setSilicon(register(
            BuiltInRegistries.ITEM,
            SILICON,
            new SimpleItem()
        ));
        Items.INSTANCE.setProcessorBinding(register(
            BuiltInRegistries.ITEM,
            PROCESSOR_BINDING,
            new SimpleItem()
        ));
        register(
            BuiltInRegistries.ITEM,
            DISK_DRIVE,
            new SimpleBlockItem(Blocks.INSTANCE.getDiskDrive())
        );
        Items.INSTANCE.setWrench(register(
            BuiltInRegistries.ITEM,
            WRENCH,
            new WrenchItem()
        ));
        Items.INSTANCE.setStorageHousing(register(
            BuiltInRegistries.ITEM,
            STORAGE_HOUSING,
            new SimpleItem())
        );
        register(
            BuiltInRegistries.ITEM,
            MACHINE_CASING,
            new SimpleBlockItem(Blocks.INSTANCE.getMachineCasing())
        );

        register(BuiltInRegistries.ITEM, INTERFACE,
            new SimpleBlockItem(Blocks.INSTANCE.getInterface()));

        Items.INSTANCE.setConstructionCore(register(BuiltInRegistries.ITEM, CONSTRUCTION_CORE, new SimpleItem()));
        Items.INSTANCE.setDestructionCore(register(BuiltInRegistries.ITEM, DESTRUCTION_CORE, new SimpleItem()));

        for (final ProcessorItem.Type type : ProcessorItem.Type.values()) {
            Items.INSTANCE.setProcessor(
                type,
                register(
                    BuiltInRegistries.ITEM,
                    forProcessor(type),
                    new ProcessorItem()
                )
            );
        }
    }

    private void registerGridItems() {
        Blocks.INSTANCE.getGrid().forEach((color, block) -> register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getGrid().getId(color, GRID),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getGrid().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")
            ))
        ));
        Blocks.INSTANCE.getCraftingGrid().forEach((color, block) -> register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getCraftingGrid().getId(color, CRAFTING_GRID),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getCraftingGrid().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "crafting_grid")
            ))
        ));
    }

    private void registerCableItems() {
        Blocks.INSTANCE.getCable().forEach((color, block) -> Items.INSTANCE.addCable(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getCable().getId(color, CABLE),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getCable().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "cable")
            ))
        )));
    }

    private void registerControllerItems() {
        Blocks.INSTANCE.getController().forEach((color, block) -> Items.INSTANCE.addRegularController(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getController().getId(color, CONTROLLER),
            new ControllerBlockItem(block.get(), Blocks.INSTANCE.getController().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")
            ))
        )));
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> Items.INSTANCE.addController(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER),
            new CreativeControllerBlockItem(
                block.get(),
                Blocks.INSTANCE.getCreativeController().getName(
                    color,
                    createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")
                )
            )
        )));
    }

    private void registerDetectorItems() {
        Blocks.INSTANCE.getDetector().forEach((color, block) -> Items.INSTANCE.addDetector(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getDetector().getId(color, DETECTOR),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getDetector().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "detector")
            ))
        )));
    }

    private void registerImporterItems() {
        Blocks.INSTANCE.getImporter().forEach((color, block) -> Items.INSTANCE.addImporter(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getImporter().getId(color, IMPORTER),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getImporter().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "importer")
            ))
        )));
    }

    private void registerExporterItems() {
        Blocks.INSTANCE.getExporter().forEach((color, block) -> Items.INSTANCE.addExporter(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getExporter().getId(color, EXPORTER),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getExporter().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "exporter")
            ))
        )));
    }

    private void registerExternalStorageItems() {
        Blocks.INSTANCE.getExternalStorage().forEach((color, block) -> Items.INSTANCE.addExternalStorage(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getExternalStorage().getId(color, EXTERNAL_STORAGE),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getExternalStorage().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "external_storage")
            ))
        )));
    }

    private void registerDestructorItems() {
        Blocks.INSTANCE.getDestructor().forEach((color, block) -> Items.INSTANCE.addDestructor(register(
            BuiltInRegistries.ITEM,
            Blocks.INSTANCE.getDestructor().getId(color, DESTRUCTOR),
            new NamedBlockItem(block.get(), new Item.Properties(), Blocks.INSTANCE.getDestructor().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "destructor")
            ))
        )));
    }

    private void registerStorageItems() {
        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            registerItemStorageItems(variant);
        }
        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            registerFluidStorageItems(variant);
        }
    }

    private void registerItemStorageItems(final ItemStorageType.Variant variant) {
        if (variant != ItemStorageType.Variant.CREATIVE) {
            Items.INSTANCE.setItemStoragePart(variant, register(
                BuiltInRegistries.ITEM,
                forItemStoragePart(variant),
                new SimpleItem())
            );
        }
        Items.INSTANCE.setItemStorageDisk(variant, register(
            BuiltInRegistries.ITEM,
            forStorageDisk(variant),
            new ItemStorageDiskItem(variant)
        ));
        register(
            BuiltInRegistries.ITEM,
            forItemStorageBlock(variant),
            new ItemStorageBlockBlockItem(Blocks.INSTANCE.getItemStorageBlock(variant), variant)
        );
    }

    private void registerFluidStorageItems(final FluidStorageType.Variant variant) {
        if (variant != FluidStorageType.Variant.CREATIVE) {
            Items.INSTANCE.setFluidStoragePart(variant, register(
                BuiltInRegistries.ITEM,
                forFluidStoragePart(variant),
                new SimpleItem())
            );
        }
        Items.INSTANCE.setFluidStorageDisk(variant, register(
            BuiltInRegistries.ITEM,
            forFluidStorageDisk(variant),
            new FluidStorageDiskItem(variant)
        ));
        register(
            BuiltInRegistries.ITEM,
            forFluidStorageBlock(variant),
            new FluidStorageBlockBlockItem(Blocks.INSTANCE.getFluidStorageBlock(variant), variant)
        );
    }

    private void registerUpgrades() {
        Items.INSTANCE.setUpgrade(register(
            BuiltInRegistries.ITEM,
            ContentIds.UPGRADE,
            new SimpleItem()
        ));
        final Supplier<SimpleUpgradeItem> speedUpgrade = register(
            BuiltInRegistries.ITEM,
            ContentIds.SPEED_UPGRADE,
            new SimpleUpgradeItem(
                PlatformApi.INSTANCE.getUpgradeRegistry(),
                Platform.INSTANCE.getConfig().getUpgrade()::getSpeedUpgradeEnergyUsage,
                false
            )
        );
        Items.INSTANCE.setSpeedUpgrade(speedUpgrade);
        final Supplier<SimpleUpgradeItem> stackUpgrade = register(
            BuiltInRegistries.ITEM,
            ContentIds.STACK_UPGRADE,
            new SimpleUpgradeItem(
                PlatformApi.INSTANCE.getUpgradeRegistry(),
                Platform.INSTANCE.getConfig().getUpgrade()::getStackUpgradeEnergyUsage,
                false
            )
        );
        Items.INSTANCE.setStackUpgrade(stackUpgrade);
        final Supplier<FortuneUpgradeItem> fortune1Upgrade = register(
            BuiltInRegistries.ITEM,
            ContentIds.FORTUNE_1_UPGRADE,
            new FortuneUpgradeItem(PlatformApi.INSTANCE.getUpgradeRegistry(), 1)
        );
        Items.INSTANCE.setFortune1Upgrade(fortune1Upgrade);
        final Supplier<FortuneUpgradeItem> fortune2Upgrade = register(
            BuiltInRegistries.ITEM,
            ContentIds.FORTUNE_2_UPGRADE,
            new FortuneUpgradeItem(PlatformApi.INSTANCE.getUpgradeRegistry(), 2)
        );
        Items.INSTANCE.setFortune2Upgrade(fortune2Upgrade);
        final Supplier<FortuneUpgradeItem> fortune3Upgrade = register(
            BuiltInRegistries.ITEM,
            ContentIds.FORTUNE_3_UPGRADE,
            new FortuneUpgradeItem(PlatformApi.INSTANCE.getUpgradeRegistry(), 3)
        );
        Items.INSTANCE.setFortune3Upgrade(fortune3Upgrade);
        final Supplier<SimpleUpgradeItem> silkTouchUpgrade = register(
            BuiltInRegistries.ITEM,
            ContentIds.SILK_TOUCH_UPGRADE,
            new SimpleUpgradeItem(
                PlatformApi.INSTANCE.getUpgradeRegistry(),
                Platform.INSTANCE.getConfig().getUpgrade()::getSilkTouchUpgradeEnergyUsage,
                true
            )
        );
        Items.INSTANCE.setSilkTouchUpgrade(silkTouchUpgrade);
        addApplicableUpgrades();
    }

    private void registerBlockEntities() {
        BlockEntities.INSTANCE.setCable(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            CABLE,
            FabricBlockEntityTypeBuilder.create(
                CableBlockEntity::new,
                Blocks.INSTANCE.getCable().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setDiskDrive(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            DISK_DRIVE,
            FabricBlockEntityTypeBuilder.create(
                FabricDiskDriveBlockEntity::new,
                Blocks.INSTANCE.getDiskDrive()
            ).build()
        ));
        BlockEntities.INSTANCE.setGrid(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            GRID,
            FabricBlockEntityTypeBuilder.create(
                GridBlockEntity::new,
                Blocks.INSTANCE.getGrid().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setCraftingGrid(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            CRAFTING_GRID,
            FabricBlockEntityTypeBuilder.create(
                CraftingGridBlockEntity::new,
                Blocks.INSTANCE.getCraftingGrid().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setController(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            CONTROLLER,
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state),
                Blocks.INSTANCE.getController().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setCreativeController(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            CREATIVE_CONTROLLER,
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new ControllerBlockEntity(ControllerType.CREATIVE, pos, state),
                Blocks.INSTANCE.getCreativeController().toArray()
            ).build()
        ));

        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            final BlockEntityType<ItemStorageBlockBlockEntity> blockEntityType = FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new ItemStorageBlockBlockEntity(pos, state, variant),
                Blocks.INSTANCE.getItemStorageBlock(variant)
            ).build();
            BlockEntities.INSTANCE.setItemStorageBlock(
                variant,
                register(BuiltInRegistries.BLOCK_ENTITY_TYPE, forItemStorageBlock(variant), blockEntityType)
            );
        }

        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            final BlockEntityType<FluidStorageBlockBlockEntity> blockEntityType = FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new FluidStorageBlockBlockEntity(pos, state, variant),
                Blocks.INSTANCE.getFluidStorageBlock(variant)
            ).build();
            BlockEntities.INSTANCE.setFluidStorageBlock(
                variant,
                register(BuiltInRegistries.BLOCK_ENTITY_TYPE, forFluidStorageBlock(variant), blockEntityType)
            );
        }

        BlockEntities.INSTANCE.setImporter(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            IMPORTER,
            FabricBlockEntityTypeBuilder.create(
                ImporterBlockEntity::new,
                Blocks.INSTANCE.getImporter().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setExporter(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            EXPORTER,
            FabricBlockEntityTypeBuilder.create(
                ExporterBlockEntity::new,
                Blocks.INSTANCE.getExporter().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setInterface(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            INTERFACE,
            FabricBlockEntityTypeBuilder.create(
                InterfaceBlockEntity::new,
                Blocks.INSTANCE.getInterface()
            ).build()
        ));
        BlockEntities.INSTANCE.setExternalStorage(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            EXTERNAL_STORAGE,
            FabricBlockEntityTypeBuilder.create(
                ExternalStorageBlockEntity::new,
                Blocks.INSTANCE.getExternalStorage().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setDetector(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            DETECTOR,
            FabricBlockEntityTypeBuilder.create(
                DetectorBlockEntity::new,
                Blocks.INSTANCE.getDetector().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setDestructor(register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            DESTRUCTOR,
            FabricBlockEntityTypeBuilder.create(
                DestructorBlockEntity::new,
                Blocks.INSTANCE.getDestructor().toArray()
            ).build()
        ));
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
    }

    private void registerLootFunctions() {
        LootFunctions.INSTANCE.setStorageBlock(register(
            BuiltInRegistries.LOOT_FUNCTION_TYPE,
            STORAGE_BLOCK,
            new LootItemFunctionType(new AbstractStorageBlock.StorageBlockLootItemFunctionSerializer())
        ));
    }

    private void registerCreativeModeTab() {
        FabricItemGroup.builder(createIdentifier("general"))
            .title(createTranslation("itemGroup", "general"))
            .icon(() -> new ItemStack(Blocks.INSTANCE.getController().getDefault()))
            .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
            .build();
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

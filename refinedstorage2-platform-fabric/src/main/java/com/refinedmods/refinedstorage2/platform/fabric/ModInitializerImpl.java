package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.AbstractStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ExternalStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ImporterBlock;
import com.refinedmods.refinedstorage2.platform.common.block.InterfaceBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.SimpleBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ImporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.ControllerBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExternalStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.ContentIds;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.item.SimpleItem;
import com.refinedmods.refinedstorage2.platform.common.item.SimpleUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.CreativeControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.FluidStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.GridBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ItemStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.SimpleBlockItem;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.exporter.StorageExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.fabric.internal.network.node.importer.StorageImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceFilterSlotAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceTypeChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.reborn.energy.api.EnergyStorage;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CABLE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONSTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CREATIVE_CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DESTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.EXPORTER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.EXTERNAL_STORAGE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.FLUID_GRID;
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
    private static final Logger LOGGER = LogManager.getLogger(ModInitializerImpl.class);
    private static final String BLOCK_TRANSLATION_CATEGORY = "block";
    private static final CreativeModeTab CREATIVE_MODE_TAB = FabricItemGroupBuilder.build(
        createIdentifier("general"),
        () -> new ItemStack(Blocks.INSTANCE.getController().getNormal())
    );

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);

        initializePlatform(new PlatformImpl());
        initializePlatformApi();
        registerAdditionalStorageTypes();
        registerAdditionalStorageChannelTypes();
        registerNetworkComponents();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerContent();
        registerPackets();
        registerSounds();
        registerSidedHandlers();
        registerAdditionalResourceTypes();
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
        Blocks.INSTANCE.setCable(register(
            Registry.BLOCK,
            CABLE,
            new CableBlock()
        ));
        Blocks.INSTANCE.setQuartzEnrichedIronBlock(register(
            Registry.BLOCK,
            QUARTZ_ENRICHED_IRON_BLOCK,
            new SimpleBlock()
        ));
        Blocks.INSTANCE.setDiskDrive(register(
            Registry.BLOCK,
            DISK_DRIVE,
            new DiskDriveBlock(FabricDiskDriveBlockEntity::new)
        ));
        Blocks.INSTANCE.setMachineCasing(register(
            Registry.BLOCK,
            MACHINE_CASING,
            new SimpleBlock()
        ));

        Blocks.INSTANCE.getGrid().putAll(color -> register(
            Registry.BLOCK,
            Blocks.INSTANCE.getGrid().getId(color, GRID),
            new ItemGridBlock(Blocks.INSTANCE.getGrid().getName(color, createTranslation(
                BLOCK_TRANSLATION_CATEGORY,
                "grid"
            )))
        ));
        Blocks.INSTANCE.getFluidGrid().putAll(color -> register(
            Registry.BLOCK,
            Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID),
            new FluidGridBlock(Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(
                BLOCK_TRANSLATION_CATEGORY,
                "fluid_grid"
            )))
        ));
        Blocks.INSTANCE.getController().putAll(color -> register(
            Registry.BLOCK,
            Blocks.INSTANCE.getController().getId(color, CONTROLLER),
            new ControllerBlock(
                ControllerType.NORMAL,
                Blocks.INSTANCE.getController().getName(
                    color,
                    createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")
                ),
                new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getController)
            )
        ));
        Blocks.INSTANCE.getCreativeController().putAll(color -> register(
            Registry.BLOCK,
            Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER),
            new ControllerBlock(
                ControllerType.CREATIVE,
                Blocks.INSTANCE.getCreativeController().getName(
                    color,
                    createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")
                ),
                new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getCreativeController)
            )
        ));

        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            Blocks.INSTANCE.setItemStorageBlock(variant, register(
                Registry.BLOCK,
                forItemStorageBlock(variant),
                new ItemStorageBlock(variant)
            ));
        }

        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            Blocks.INSTANCE.setFluidStorageBlock(variant, register(
                Registry.BLOCK,
                forFluidStorageBlock(variant),
                new FluidStorageBlock(variant)
            ));
        }

        Blocks.INSTANCE.setImporter(register(
            Registry.BLOCK,
            IMPORTER,
            new ImporterBlock()
        ));
        Blocks.INSTANCE.setExporter(register(
            Registry.BLOCK,
            EXPORTER,
            new ExporterBlock()
        ));
        Blocks.INSTANCE.setInterface(register(
            Registry.BLOCK,
            INTERFACE,
            new InterfaceBlock()
        ));
        Blocks.INSTANCE.setExternalStorage(register(
            Registry.BLOCK,
            EXTERNAL_STORAGE,
            new ExternalStorageBlock()
        ));
    }

    private void registerItems() {
        registerSimpleItems();
        registerGridItems();
        registerControllerItems();
        registerStorageItems();
        registerUpgrades();
    }

    private void registerSimpleItems() {
        register(
            Registry.ITEM,
            CABLE,
            new SimpleBlockItem(Blocks.INSTANCE.getCable(), CREATIVE_MODE_TAB)
        );
        register(
            Registry.ITEM,
            QUARTZ_ENRICHED_IRON,
            new SimpleItem(CREATIVE_MODE_TAB)
        );
        register(
            Registry.ITEM,
            QUARTZ_ENRICHED_IRON_BLOCK,
            new SimpleBlockItem(Blocks.INSTANCE.getQuartzEnrichedIronBlock(), CREATIVE_MODE_TAB)
        );
        register(
            Registry.ITEM,
            SILICON,
            new SimpleItem(CREATIVE_MODE_TAB)
        );
        register(
            Registry.ITEM,
            PROCESSOR_BINDING,
            new SimpleItem(CREATIVE_MODE_TAB)
        );
        register(
            Registry.ITEM,
            DISK_DRIVE,
            new SimpleBlockItem(Blocks.INSTANCE.getDiskDrive(), CREATIVE_MODE_TAB)
        );
        register(
            Registry.ITEM,
            WRENCH,
            new WrenchItem(CREATIVE_MODE_TAB)
        );
        Items.INSTANCE.setStorageHousing(register(
            Registry.ITEM,
            STORAGE_HOUSING,
            new SimpleItem(CREATIVE_MODE_TAB))
        );
        register(
            Registry.ITEM,
            MACHINE_CASING,
            new SimpleBlockItem(Blocks.INSTANCE.getMachineCasing(), CREATIVE_MODE_TAB)
        );

        register(Registry.ITEM, IMPORTER, new SimpleBlockItem(Blocks.INSTANCE.getImporter(), CREATIVE_MODE_TAB));
        register(Registry.ITEM, EXPORTER, new SimpleBlockItem(Blocks.INSTANCE.getExporter(), CREATIVE_MODE_TAB));
        register(Registry.ITEM, INTERFACE, new SimpleBlockItem(Blocks.INSTANCE.getInterface(), CREATIVE_MODE_TAB));
        register(Registry.ITEM, EXTERNAL_STORAGE, new SimpleBlockItem(
            Blocks.INSTANCE.getExternalStorage(),
            CREATIVE_MODE_TAB
        ));

        register(Registry.ITEM, CONSTRUCTION_CORE, new SimpleItem(CREATIVE_MODE_TAB));
        register(Registry.ITEM, DESTRUCTION_CORE, new SimpleItem(CREATIVE_MODE_TAB));

        for (final ProcessorItem.Type type : ProcessorItem.Type.values()) {
            register(
                Registry.ITEM,
                forProcessor(type),
                new ProcessorItem(CREATIVE_MODE_TAB)
            );
        }
    }

    private void registerGridItems() {
        Blocks.INSTANCE.getGrid().forEach((color, block) -> register(
            Registry.ITEM,
            Blocks.INSTANCE.getGrid().getId(color, GRID),
            new GridBlockItem(block.get(), CREATIVE_MODE_TAB, Blocks.INSTANCE.getGrid().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")
            ))
        ));
        Blocks.INSTANCE.getFluidGrid().forEach((color, block) -> register(
            Registry.ITEM,
            Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID),
            new GridBlockItem(block.get(), CREATIVE_MODE_TAB, Blocks.INSTANCE.getFluidGrid().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")
            ))
        ));
    }

    private void registerControllerItems() {
        Blocks.INSTANCE.getController().forEach((color, block) -> Items.INSTANCE.getControllers().add(register(
            Registry.ITEM,
            Blocks.INSTANCE.getController().getId(color, CONTROLLER),
            new ControllerBlockItem(block.get(), CREATIVE_MODE_TAB, Blocks.INSTANCE.getController().getName(
                color,
                createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")
            ))
        )));
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> register(
            Registry.ITEM,
            Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER),
            new CreativeControllerBlockItem(
                block.get(),
                CREATIVE_MODE_TAB,
                Blocks.INSTANCE.getCreativeController().getName(
                    color,
                    createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")
                )
            )
        ));
    }

    private void registerStorageItems() {
        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            if (variant != ItemStorageType.Variant.CREATIVE) {
                Items.INSTANCE.setItemStoragePart(variant, register(
                    Registry.ITEM,
                    forItemStoragePart(variant),
                    new SimpleItem(CREATIVE_MODE_TAB))
                );
            }
        }

        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            if (variant != FluidStorageType.Variant.CREATIVE) {
                Items.INSTANCE.setFluidStoragePart(variant, register(
                    Registry.ITEM,
                    forFluidStoragePart(variant),
                    new SimpleItem(CREATIVE_MODE_TAB))
                );
            }
        }

        for (final ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            register(
                Registry.ITEM,
                forStorageDisk(variant),
                new ItemStorageDiskItem(CREATIVE_MODE_TAB, variant)
            );
        }

        for (final ItemStorageType.Variant v : ItemStorageType.Variant.values()) {
            register(
                Registry.ITEM,
                forItemStorageBlock(v),
                new ItemStorageBlockBlockItem(Blocks.INSTANCE.getItemStorageBlock(v), CREATIVE_MODE_TAB, v)
            );
        }

        for (final FluidStorageType.Variant v : FluidStorageType.Variant.values()) {
            register(
                Registry.ITEM,
                forFluidStorageDisk(v),
                new FluidStorageDiskItem(CREATIVE_MODE_TAB, v)
            );
        }

        for (final FluidStorageType.Variant v : FluidStorageType.Variant.values()) {
            register(
                Registry.ITEM,
                forFluidStorageBlock(v),
                new FluidStorageBlockBlockItem(Blocks.INSTANCE.getFluidStorageBlock(v), CREATIVE_MODE_TAB, v)
            );
        }
    }

    private void registerUpgrades() {
        register(
            Registry.ITEM,
            ContentIds.UPGRADE,
            new SimpleUpgradeItem(CREATIVE_MODE_TAB, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        final Supplier<Item> speedUpgrade = register(
            Registry.ITEM,
            ContentIds.SPEED_UPGRADE,
            new SimpleUpgradeItem(CREATIVE_MODE_TAB, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        Items.INSTANCE.setSpeedUpgrade(speedUpgrade);
        final Supplier<Item> stackUpgrade = register(
            Registry.ITEM,
            ContentIds.STACK_UPGRADE,
            new SimpleUpgradeItem(CREATIVE_MODE_TAB, PlatformApi.INSTANCE.getUpgradeRegistry())
        );
        Items.INSTANCE.setStackUpgrade(stackUpgrade);
        addApplicableUpgrades(speedUpgrade, stackUpgrade);
    }

    private void registerBlockEntities() {
        BlockEntities.INSTANCE.setCable(register(
            Registry.BLOCK_ENTITY_TYPE,
            CABLE,
            FabricBlockEntityTypeBuilder.create(
                CableBlockEntity::new,
                Blocks.INSTANCE.getCable()
            ).build())
        );
        BlockEntities.INSTANCE.setDiskDrive(register(
            Registry.BLOCK_ENTITY_TYPE,
            DISK_DRIVE,
            FabricBlockEntityTypeBuilder.create(
                FabricDiskDriveBlockEntity::new,
                Blocks.INSTANCE.getDiskDrive()
            ).build()
        ));
        BlockEntities.INSTANCE.setGrid(register(
            Registry.BLOCK_ENTITY_TYPE,
            GRID,
            FabricBlockEntityTypeBuilder.create(
                ItemGridBlockEntity::new,
                Blocks.INSTANCE.getGrid().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setFluidGrid(register(
            Registry.BLOCK_ENTITY_TYPE,
            FLUID_GRID,
            FabricBlockEntityTypeBuilder.create(
                FluidGridBlockEntity::new,
                Blocks.INSTANCE.getFluidGrid().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setController(register(
            Registry.BLOCK_ENTITY_TYPE,
            CONTROLLER,
            FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state),
                Blocks.INSTANCE.getController().toArray()
            ).build()
        ));
        BlockEntities.INSTANCE.setCreativeController(register(
            Registry.BLOCK_ENTITY_TYPE,
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
                register(Registry.BLOCK_ENTITY_TYPE, forItemStorageBlock(variant), blockEntityType)
            );
        }

        for (final FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            final BlockEntityType<FluidStorageBlockBlockEntity> blockEntityType = FabricBlockEntityTypeBuilder.create(
                (pos, state) -> new FluidStorageBlockBlockEntity(pos, state, variant),
                Blocks.INSTANCE.getFluidStorageBlock(variant)
            ).build();
            BlockEntities.INSTANCE.setFluidStorageBlock(
                variant,
                register(Registry.BLOCK_ENTITY_TYPE, forFluidStorageBlock(variant), blockEntityType)
            );
        }

        BlockEntities.INSTANCE.setImporter(register(
            Registry.BLOCK_ENTITY_TYPE,
            IMPORTER,
            FabricBlockEntityTypeBuilder.create(
                ImporterBlockEntity::new,
                Blocks.INSTANCE.getImporter()
            ).build()
        ));
        BlockEntities.INSTANCE.setExporter(register(
            Registry.BLOCK_ENTITY_TYPE,
            EXPORTER,
            FabricBlockEntityTypeBuilder.create(
                ExporterBlockEntity::new,
                Blocks.INSTANCE.getExporter()
            ).build()
        ));
        BlockEntities.INSTANCE.setInterface(register(
            Registry.BLOCK_ENTITY_TYPE,
            INTERFACE,
            FabricBlockEntityTypeBuilder.create(
                InterfaceBlockEntity::new,
                Blocks.INSTANCE.getInterface()
            ).build()
        ));
        BlockEntities.INSTANCE.setExternalStorage(register(
            Registry.BLOCK_ENTITY_TYPE,
            EXTERNAL_STORAGE,
            FabricBlockEntityTypeBuilder.create(
                ExternalStorageBlockEntity::new,
                Blocks.INSTANCE.getExternalStorage()
            ).build()
        ));
    }

    private void registerMenus() {
        Menus.INSTANCE.setDiskDrive(register(
            Registry.MENU,
            DISK_DRIVE,
            new ExtendedScreenHandlerType<>(DiskDriveContainerMenu::new)
        ));
        Menus.INSTANCE.setGrid(register(
            Registry.MENU,
            GRID,
            new ExtendedScreenHandlerType<>(ItemGridContainerMenu::new)
        ));
        Menus.INSTANCE.setFluidGrid(register(
            Registry.MENU,
            FLUID_GRID,
            new ExtendedScreenHandlerType<>(FluidGridContainerMenu::new)
        ));
        Menus.INSTANCE.setController(register(
            Registry.MENU,
            CONTROLLER,
            new ExtendedScreenHandlerType<>(ControllerContainerMenu::new)
        ));
        Menus.INSTANCE.setItemStorage(register(
            Registry.MENU,
            ITEM_STORAGE_BLOCK,
            new ExtendedScreenHandlerType<>(ItemStorageBlockContainerMenu::new)
        ));
        Menus.INSTANCE.setFluidStorage(register(
            Registry.MENU,
            FLUID_STORAGE_BLOCK,
            new ExtendedScreenHandlerType<>(FluidStorageBlockContainerMenu::new)
        ));
        Menus.INSTANCE.setImporter(register(
            Registry.MENU,
            IMPORTER,
            new ExtendedScreenHandlerType<>(ImporterContainerMenu::new)
        ));
        Menus.INSTANCE.setExporter(register(
            Registry.MENU,
            EXPORTER,
            new ExtendedScreenHandlerType<>(ExporterContainerMenu::new)
        ));
        Menus.INSTANCE.setInterface(register(
            Registry.MENU,
            INTERFACE,
            new ExtendedScreenHandlerType<>(InterfaceContainerMenu::new)
        ));
        Menus.INSTANCE.setExternalStorage(register(
            Registry.MENU,
            EXTERNAL_STORAGE,
            new ExtendedScreenHandlerType<>(ExternalStorageContainerMenu::new)
        ));
    }

    private void registerLootFunctions() {
        LootFunctions.INSTANCE.setStorageBlock(register(
            Registry.LOOT_FUNCTION_TYPE,
            STORAGE_BLOCK,
            new LootItemFunctionType(new AbstractStorageBlock.StorageBlockLootItemFunctionSerializer())
        ));
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_REQUEST, new StorageInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_INSERT, new GridInsertPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_EXTRACT, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_SCROLL, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.PROPERTY_CHANGE, new PropertyChangePacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.RESOURCE_TYPE_CHANGE, new ResourceTypeChangePacket());
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.RESOURCE_FILTER_SLOT_AMOUNT_CHANGE,
            new ResourceFilterSlotAmountChangePacket()
        );
    }

    private void registerSounds() {
        Sounds.INSTANCE.setWrench(register(Registry.SOUND_EVENT, WRENCH, new SoundEvent(WRENCH)));
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

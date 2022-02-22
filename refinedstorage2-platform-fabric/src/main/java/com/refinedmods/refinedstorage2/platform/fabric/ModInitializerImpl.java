package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.platform.common.block.QuartzEnrichedIronBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.search.PlatformSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.common.item.CoreItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorBindingItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.item.QuartzEnrichedIronItem;
import com.refinedmods.refinedstorage2.platform.common.item.SiliconItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.NameableBlockItem;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.integration.energy.ControllerTeamRebornEnergy;
import com.refinedmods.refinedstorage2.platform.fabric.integration.rei.ReiIntegration;
import com.refinedmods.refinedstorage2.platform.fabric.internal.ConfigImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.PlatformImpl;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceTypeChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.reborn.energy.api.EnergyStorage;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CABLE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONSTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CREATIVE_CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DESTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.FLUID_GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.MACHINE_CASING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.PROCESSOR_BINDING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.QUARTZ_ENRICHED_IRON;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.QUARTZ_ENRICHED_IRON_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.SILICON;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.STORAGE_HOUSING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.WRENCH;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStorageDisk;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStoragePart;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forItemStoragePart;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forProcessor;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forStorageDisk;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ModInitializerImpl extends AbstractModInitializer implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(ModInitializerImpl.class);
    private static final String BLOCK_TRANSLATION_CATEGORY = "block";
    private static final CreativeModeTab CREATIVE_MODE_TAB = FabricItemGroupBuilder.build(createIdentifier("general"), () -> new ItemStack(Blocks.INSTANCE.getController().getNormal()));

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);

        initializePlatform(new PlatformImpl());
        initializePlatformApiFacade();
        registerDiskTypes();
        registerStorageChannelTypes();
        registerNetworkComponents();
        registerContent();
        registerGridSearchBoxModes();
        registerPackets();
        registerSounds();
        registerSidedHandlers();
        registerResourceTypes();
        registerTickHandler();
        registerWrench();

        LOGGER.info("Refined Storage 2 has loaded.");
    }

    private void registerWrench() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> BaseBlock.useWrench(level.getBlockState(hitResult.getBlockPos()), level, hitResult, player, hand));
    }

    private void registerContent() {
        registerBlocks();
        registerItems();
        registerBlockEntities();
        registerMenus();
    }

    private void registerBlocks() {
        Blocks.INSTANCE.setCable(Registry.register(Registry.BLOCK, CABLE, new CableBlock()));
        Blocks.INSTANCE.setQuartzEnrichedIron(Registry.register(Registry.BLOCK, QUARTZ_ENRICHED_IRON_BLOCK, new QuartzEnrichedIronBlock()));
        Blocks.INSTANCE.setDiskDrive(Registry.register(Registry.BLOCK, DISK_DRIVE, new DiskDriveBlock(FabricDiskDriveBlockEntity::new)));
        Blocks.INSTANCE.setMachineCasing(Registry.register(Registry.BLOCK, MACHINE_CASING, new MachineCasingBlock()));

        Blocks.INSTANCE.getGrid().putAll(color -> Registry.register(Registry.BLOCK, Blocks.INSTANCE.getGrid().getId(color, GRID), new ItemGridBlock(Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")))));
        Blocks.INSTANCE.getFluidGrid().putAll(color -> Registry.register(Registry.BLOCK, Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID), new FluidGridBlock(Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")))));
        Blocks.INSTANCE.getController().putAll(color -> Registry.register(Registry.BLOCK, Blocks.INSTANCE.getController().getId(color, CONTROLLER), new ControllerBlock(ControllerType.NORMAL, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")))));
        Blocks.INSTANCE.getCreativeController().putAll(color -> Registry.register(Registry.BLOCK, Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER), new ControllerBlock(ControllerType.CREATIVE, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")))));
    }

    private void registerItems() {
        Registry.register(Registry.ITEM, CABLE, new BlockItem(Blocks.INSTANCE.getCable(), createProperties()));
        Registry.register(Registry.ITEM, QUARTZ_ENRICHED_IRON, new QuartzEnrichedIronItem(createProperties()));
        Registry.register(Registry.ITEM, QUARTZ_ENRICHED_IRON_BLOCK, new BlockItem(Blocks.INSTANCE.getQuartzEnrichedIron(), createProperties()));
        Registry.register(Registry.ITEM, SILICON, new SiliconItem(createProperties()));
        Registry.register(Registry.ITEM, PROCESSOR_BINDING, new ProcessorBindingItem(createProperties()));
        Registry.register(Registry.ITEM, DISK_DRIVE, new BlockItem(Blocks.INSTANCE.getDiskDrive(), createProperties()));
        Registry.register(Registry.ITEM, WRENCH, new WrenchItem(createProperties().stacksTo(1)));

        Items.INSTANCE.setStorageHousing(Registry.register(Registry.ITEM, STORAGE_HOUSING, new StorageHousingItem(createProperties())));
        Registry.register(Registry.ITEM, MACHINE_CASING, new BlockItem(Blocks.INSTANCE.getMachineCasing(), createProperties()));
        Blocks.INSTANCE.getGrid().forEach((color, block) -> Registry.register(Registry.ITEM, Blocks.INSTANCE.getGrid().getId(color, GRID), new NameableBlockItem(block, createProperties(), color, Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")))));
        Blocks.INSTANCE.getFluidGrid().forEach((color, block) -> Registry.register(Registry.ITEM, Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID), new NameableBlockItem(block, createProperties(), color, Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")))));
        Blocks.INSTANCE.getController().forEach((color, block) -> Items.INSTANCE.getControllers().add(Registry.register(Registry.ITEM, Blocks.INSTANCE.getController().getId(color, CONTROLLER), new ControllerBlockItem(block, createProperties().stacksTo(1), color, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller"))))));
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> Registry.register(Registry.ITEM, Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER), new NameableBlockItem(block, createProperties().stacksTo(1), color, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")))));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            Registry.register(Registry.ITEM, forProcessor(type), new ProcessorItem(createProperties()));
        }

        for (ItemStorageDiskItem.ItemStorageType type : ItemStorageDiskItem.ItemStorageType.values()) {
            if (type != ItemStorageDiskItem.ItemStorageType.CREATIVE) {
                Items.INSTANCE.getStorageParts().put(type, Registry.register(Registry.ITEM, forItemStoragePart(type), new StoragePartItem(createProperties())));
            }
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            if (type != FluidStorageDiskItem.FluidStorageType.CREATIVE) {
                Items.INSTANCE.getFluidStorageParts().put(type, Registry.register(Registry.ITEM, forFluidStoragePart(type), new FluidStoragePartItem(createProperties())));
            }
        }

        for (ItemStorageDiskItem.ItemStorageType type : ItemStorageDiskItem.ItemStorageType.values()) {
            Registry.register(Registry.ITEM, forStorageDisk(type), new ItemStorageDiskItem(createProperties().stacksTo(1).fireResistant(), type));
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            Registry.register(Registry.ITEM, forFluidStorageDisk(type), new FluidStorageDiskItem(createProperties().stacksTo(1).fireResistant(), type));
        }

        Registry.register(Registry.ITEM, CONSTRUCTION_CORE, new CoreItem(createProperties()));
        Registry.register(Registry.ITEM, DESTRUCTION_CORE, new CoreItem(createProperties()));
    }

    private Item.Properties createProperties() {
        return new Item.Properties().tab(CREATIVE_MODE_TAB);
    }

    private void registerBlockEntities() {
        BlockEntities.INSTANCE.setCable(Registry.register(Registry.BLOCK_ENTITY_TYPE, CABLE, FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, Blocks.INSTANCE.getCable()).build(null)));
        BlockEntities.INSTANCE.setDiskDrive(Registry.register(Registry.BLOCK_ENTITY_TYPE, DISK_DRIVE, FabricBlockEntityTypeBuilder.create(FabricDiskDriveBlockEntity::new, Blocks.INSTANCE.getDiskDrive()).build(null)));
        BlockEntities.INSTANCE.setGrid(Registry.register(Registry.BLOCK_ENTITY_TYPE, GRID, FabricBlockEntityTypeBuilder.create(ItemGridBlockEntity::new, Blocks.INSTANCE.getGrid().toArray()).build(null)));
        BlockEntities.INSTANCE.setFluidGrid(Registry.register(Registry.BLOCK_ENTITY_TYPE, FLUID_GRID, FabricBlockEntityTypeBuilder.create(FluidGridBlockEntity::new, Blocks.INSTANCE.getFluidGrid().toArray()).build(null)));
        BlockEntities.INSTANCE.setController(Registry.register(Registry.BLOCK_ENTITY_TYPE, CONTROLLER, FabricBlockEntityTypeBuilder.create((pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state), Blocks.INSTANCE.getController().toArray()).build(null)));
        BlockEntities.INSTANCE.setCreativeController(Registry.register(Registry.BLOCK_ENTITY_TYPE, CREATIVE_CONTROLLER, FabricBlockEntityTypeBuilder.create((pos, state) -> new ControllerBlockEntity(ControllerType.CREATIVE, pos, state), Blocks.INSTANCE.getCreativeController().toArray()).build(null)));
    }

    private void registerMenus() {
        Menus.INSTANCE.setDiskDrive(ScreenHandlerRegistry.registerExtended(DISK_DRIVE, DiskDriveContainerMenu::new));
        Menus.INSTANCE.setGrid(ScreenHandlerRegistry.registerExtended(GRID, ItemGridContainerMenu::new));
        Menus.INSTANCE.setFluidGrid(ScreenHandlerRegistry.registerExtended(FLUID_GRID, FluidGridContainerMenu::new));
        Menus.INSTANCE.setController(ScreenHandlerRegistry.registerExtended(CONTROLLER, ControllerContainerMenu::new));
    }

    private void registerGridSearchBoxModes() {
        GridQueryParser queryParser = new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS, GridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING);

        for (boolean autoSelected : new boolean[]{false, true}) {
            GridSearchBoxModeRegistry.INSTANCE.add(new PlatformSearchBoxModeImpl(queryParser, createIdentifier("textures/icons.png"), autoSelected ? 16 : 0, 96, createTranslation("gui", String.format("grid.search_box_mode.normal%s", autoSelected ? "_autoselected" : "")), autoSelected));
        }

        if (ReiIntegration.isLoaded()) {
            ReiIntegration.registerGridSearchBoxModes(queryParser);
        }
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_REQUEST, new StorageInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_INSERT, new GridInsertPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_EXTRACT, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_SCROLL, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.PROPERTY_CHANGE, new PropertyChangePacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.RESOURCE_TYPE_CHANGE, new ResourceTypeChangePacket());
    }

    private void registerSounds() {
        Sounds.INSTANCE.setWrench(Registry.register(Registry.SOUND_EVENT, WRENCH, new SoundEvent(WRENCH)));
    }

    private void registerSidedHandlers() {
        registerDiskDriveInventory();
        registerControllerEnergy();
    }

    private void registerDiskDriveInventory() {
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof DiskDriveBlockEntity diskDrive) {
                return InventoryStorage.of(diskDrive.getDiskInventory(), context);
            }
            return null;
        }, BlockEntities.INSTANCE.getDiskDrive());
    }

    private void registerControllerEnergy() {
        EnergyStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> ((ControllerTeamRebornEnergy) blockEntity.getEnergyStorage()).getExposedStorage(), BlockEntities.INSTANCE.getController());
    }

    private void registerTickHandler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> TickHandler.runQueuedActions());
    }
}

package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterBlock;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerBlock;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorBlock;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlock;
import com.refinedmods.refinedstorage.common.constructordestructor.AbstractConstructorBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.AbstractDestructorBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.ConstructorBlock;
import com.refinedmods.refinedstorage.common.constructordestructor.DestructorBlock;
import com.refinedmods.refinedstorage.common.controller.AbstractControllerBlock;
import com.refinedmods.refinedstorage.common.controller.ControllerBlock;
import com.refinedmods.refinedstorage.common.controller.ControllerBlockEntityTicker;
import com.refinedmods.refinedstorage.common.controller.ControllerBlockItem;
import com.refinedmods.refinedstorage.common.controller.CreativeControllerBlock;
import com.refinedmods.refinedstorage.common.controller.CreativeControllerBlockItem;
import com.refinedmods.refinedstorage.common.detector.DetectorBlock;
import com.refinedmods.refinedstorage.common.exporter.AbstractExporterBlockEntity;
import com.refinedmods.refinedstorage.common.exporter.ExporterBlock;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlock;
import com.refinedmods.refinedstorage.common.grid.GridBlock;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlock;
import com.refinedmods.refinedstorage.common.importer.AbstractImporterBlockEntity;
import com.refinedmods.refinedstorage.common.importer.ImporterBlock;
import com.refinedmods.refinedstorage.common.networking.AbstractCableBlockEntity;
import com.refinedmods.refinedstorage.common.networking.CableBlock;
import com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlock;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterBlock;
import com.refinedmods.refinedstorage.common.networking.RelayBlock;
import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterBlock;
import com.refinedmods.refinedstorage.common.security.SecurityManagerBlock;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskinterface.DiskInterfaceBlock;
import com.refinedmods.refinedstorage.common.storage.externalstorage.AbstractExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageBlock;
import com.refinedmods.refinedstorage.common.storage.portablegrid.PortableGridBlock;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.SimpleStoneBlock;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class Blocks {
    public static final DyeColor COLOR = DyeColor.LIGHT_BLUE;
    public static final DyeColor CABLE_LIKE_COLOR = DyeColor.GRAY;
    public static final Blocks INSTANCE = new Blocks();

    @Nullable
    private BlockColorMap<CableBlock, BaseBlockItem> cable;
    private final BlockColorMap<GridBlock, BaseBlockItem> grid = new BlockColorMap<>(
        GridBlock::new,
        ContentIds.GRID,
        ContentNames.GRID,
        COLOR
    );
    private final BlockColorMap<CraftingGridBlock, BaseBlockItem> craftingGrid = new BlockColorMap<>(
        CraftingGridBlock::new,
        ContentIds.CRAFTING_GRID,
        ContentNames.CRAFTING_GRID,
        COLOR
    );
    private final BlockColorMap<PatternGridBlock, BaseBlockItem> patternGrid = new BlockColorMap<>(
        PatternGridBlock::new,
        ContentIds.PATTERN_GRID,
        ContentNames.PATTERN_GRID,
        COLOR
    );
    private final BlockColorMap<DetectorBlock, BaseBlockItem> detector = new BlockColorMap<>(
        DetectorBlock::new,
        ContentIds.DETECTOR,
        ContentNames.DETECTOR,
        COLOR
    );
    private final BlockColorMap<AbstractControllerBlock<ControllerBlockItem>, ControllerBlockItem> controller =
        new BlockColorMap<>(
            (id, color, name) -> new ControllerBlock(
                id,
                name,
                new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getController),
                color
            ),
            ContentIds.CONTROLLER,
            ContentNames.CONTROLLER,
            COLOR
        );
    private final BlockColorMap
        <AbstractControllerBlock<CreativeControllerBlockItem>, CreativeControllerBlockItem> creativeController =
        new BlockColorMap<>((id, color, name) -> new CreativeControllerBlock(
            id,
            name,
            new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getCreativeController),
            color
        ),
            ContentIds.CREATIVE_CONTROLLER,
            ContentNames.CREATIVE_CONTROLLER,
            COLOR
        );
    @Nullable
    private BlockColorMap<ExporterBlock, BaseBlockItem> exporter;
    @Nullable
    private BlockColorMap<ImporterBlock, BaseBlockItem> importer;
    @Nullable
    private BlockColorMap<ExternalStorageBlock, BaseBlockItem> externalStorage;
    @Nullable
    private BlockColorMap<DestructorBlock, BaseBlockItem> destructor;
    @Nullable
    private BlockColorMap<ConstructorBlock, BaseBlockItem> constructor;
    private final BlockColorMap<WirelessTransmitterBlock, BaseBlockItem> wirelessTransmitter = new BlockColorMap<>(
        WirelessTransmitterBlock::new,
        ContentIds.WIRELESS_TRANSMITTER,
        ContentNames.WIRELESS_TRANSMITTER,
        COLOR
    );
    private final BlockColorMap<NetworkReceiverBlock, BaseBlockItem> networkReceiver = new BlockColorMap<>(
        NetworkReceiverBlock::new,
        ContentIds.NETWORK_RECEIVER,
        ContentNames.NETWORK_RECEIVER,
        COLOR
    );
    private final BlockColorMap<NetworkTransmitterBlock, BaseBlockItem> networkTransmitter = new BlockColorMap<>(
        NetworkTransmitterBlock::new,
        ContentIds.NETWORK_TRANSMITTER,
        ContentNames.NETWORK_TRANSMITTER,
        COLOR
    );
    private final BlockColorMap<SecurityManagerBlock, BaseBlockItem> securityManager = new BlockColorMap<>(
        SecurityManagerBlock::new,
        ContentIds.SECURITY_MANAGER,
        ContentNames.SECURITY_MANAGER,
        COLOR
    );
    private final BlockColorMap<RelayBlock, BaseBlockItem> relay = new BlockColorMap<>(
        RelayBlock::new,
        ContentIds.RELAY,
        ContentNames.RELAY,
        COLOR
    );
    @Nullable
    private BlockColorMap<DiskInterfaceBlock, BaseBlockItem> diskInterface;
    private final BlockColorMap<AutocrafterBlock, BaseBlockItem> autocrafter = new BlockColorMap<>(
        AutocrafterBlock::new,
        ContentIds.AUTOCRAFTER,
        ContentNames.AUTOCRAFTER,
        COLOR
    );
    private final BlockColorMap<AutocrafterManagerBlock, BaseBlockItem> autocrafterManager = new BlockColorMap<>(
        AutocrafterManagerBlock::new,
        ContentIds.AUTOCRAFTER_MANAGER,
        ContentNames.AUTOCRAFTER_MANAGER,
        COLOR
    );
    private final BlockColorMap<AutocraftingMonitorBlock, BaseBlockItem> autocraftingMonitor = new BlockColorMap<>(
        AutocraftingMonitorBlock::new,
        ContentIds.AUTOCRAFTING_MONITOR,
        ContentNames.AUTOCRAFTING_MONITOR,
        COLOR
    );

    @Nullable
    private Supplier<DiskDriveBlock> diskDrive;
    @Nullable
    private Supplier<SimpleStoneBlock> machineCasing;
    private final Map<ItemStorageVariant, Supplier<Block>> itemStorageBlocks = new EnumMap<>(ItemStorageVariant.class);
    private final Map<FluidStorageVariant, Supplier<Block>> fluidStorageBlocks =
        new EnumMap<>(FluidStorageVariant.class);
    @Nullable
    private Supplier<InterfaceBlock> iface;
    @Nullable
    private Supplier<StorageMonitorBlock> storageMonitor;
    @Nullable
    private Supplier<PortableGridBlock> portableGrid;
    @Nullable
    private Supplier<PortableGridBlock> creativePortableGrid;

    private Blocks() {
    }

    public BlockColorMap<CableBlock, BaseBlockItem> setCable(
        final BlockEntityProvider<AbstractCableBlockEntity> provider
    ) {
        cable = new BlockColorMap<>(
            (id, color, name) -> new CableBlock(id, color, name, provider),
            ContentIds.CABLE,
            ContentNames.CABLE,
            CABLE_LIKE_COLOR
        );
        return cable;
    }

    public BlockColorMap<CableBlock, BaseBlockItem> getCable() {
        return requireNonNull(cable);
    }

    public DiskDriveBlock getDiskDrive() {
        return requireNonNull(diskDrive).get();
    }

    public SimpleStoneBlock getMachineCasing() {
        return requireNonNull(machineCasing).get();
    }

    public BlockColorMap<GridBlock, BaseBlockItem> getGrid() {
        return grid;
    }

    public BlockColorMap<CraftingGridBlock, BaseBlockItem> getCraftingGrid() {
        return craftingGrid;
    }

    public BlockColorMap<PatternGridBlock, BaseBlockItem> getPatternGrid() {
        return patternGrid;
    }

    public BlockColorMap<AbstractControllerBlock<ControllerBlockItem>, ControllerBlockItem> getController() {
        return controller;
    }

    public BlockColorMap<
        AbstractControllerBlock<CreativeControllerBlockItem>,
        CreativeControllerBlockItem> getCreativeController() {
        return creativeController;
    }

    public void setDiskDrive(final Supplier<DiskDriveBlock> diskDriveSupplier) {
        this.diskDrive = diskDriveSupplier;
    }

    public void setMachineCasing(final Supplier<SimpleStoneBlock> machineCasingSupplier) {
        this.machineCasing = machineCasingSupplier;
    }

    public void setItemStorageBlock(final ItemStorageVariant variant, final Supplier<Block> supplier) {
        itemStorageBlocks.put(variant, supplier);
    }

    public Block getItemStorageBlock(final ItemStorageVariant variant) {
        return itemStorageBlocks.get(variant).get();
    }

    public void setFluidStorageBlock(final FluidStorageVariant variant, final Supplier<Block> supplier) {
        fluidStorageBlocks.put(variant, supplier);
    }

    public Block getFluidStorageBlock(final FluidStorageVariant variant) {
        return fluidStorageBlocks.get(variant).get();
    }

    public BlockColorMap<ImporterBlock, BaseBlockItem> setImporter(
        final BlockEntityProvider<AbstractImporterBlockEntity> provider
    ) {
        importer = new BlockColorMap<>(
            (id, pos, state) -> new ImporterBlock(id, pos, state, provider),
            ContentIds.IMPORTER,
            ContentNames.IMPORTER,
            CABLE_LIKE_COLOR
        );
        return importer;
    }

    public BlockColorMap<ImporterBlock, BaseBlockItem> getImporter() {
        return requireNonNull(importer);
    }

    public BlockColorMap<ExporterBlock, BaseBlockItem> setExporter(
        final BlockEntityProvider<AbstractExporterBlockEntity> provider
    ) {
        exporter = new BlockColorMap<>(
            (id, color, name) -> new ExporterBlock(id, color, name, provider),
            ContentIds.EXPORTER,
            ContentNames.EXPORTER,
            CABLE_LIKE_COLOR
        );
        return exporter;
    }

    public BlockColorMap<ExporterBlock, BaseBlockItem> getExporter() {
        return requireNonNull(exporter);
    }

    public void setInterface(final Supplier<InterfaceBlock> interfaceSupplier) {
        this.iface = interfaceSupplier;
    }

    public InterfaceBlock getInterface() {
        return requireNonNull(iface).get();
    }

    // generate setter for ext storage with block entity provider
    public BlockColorMap<ExternalStorageBlock, BaseBlockItem> setExternalStorage(
        final BlockEntityProvider<AbstractExternalStorageBlockEntity> provider
    ) {
        externalStorage = new BlockColorMap<>(
            (id, color, name) -> new ExternalStorageBlock(id, color, name, provider),
            ContentIds.EXTERNAL_STORAGE,
            ContentNames.EXTERNAL_STORAGE,
            CABLE_LIKE_COLOR
        );
        return externalStorage;
    }

    public BlockColorMap<ExternalStorageBlock, BaseBlockItem> getExternalStorage() {
        return requireNonNull(externalStorage);
    }

    public BlockColorMap<DetectorBlock, BaseBlockItem> getDetector() {
        return detector;
    }

    public BlockColorMap<DestructorBlock, BaseBlockItem> setDestructor(
        final BlockEntityProvider<AbstractDestructorBlockEntity> provider
    ) {
        destructor = new BlockColorMap<>(
            (id, color, name) -> new DestructorBlock(id, color, name, provider),
            ContentIds.DESTRUCTOR,
            ContentNames.DESTRUCTOR,
            CABLE_LIKE_COLOR
        );
        return destructor;
    }

    public BlockColorMap<DestructorBlock, BaseBlockItem> getDestructor() {
        return requireNonNull(destructor);
    }

    public BlockColorMap<ConstructorBlock, BaseBlockItem> setConstructor(
        final BlockEntityProvider<AbstractConstructorBlockEntity> provider
    ) {
        constructor = new BlockColorMap<>(
            (id, color, name) -> new ConstructorBlock(id, color, name, provider),
            ContentIds.CONSTRUCTOR,
            ContentNames.CONSTRUCTOR,
            CABLE_LIKE_COLOR
        );
        return constructor;
    }

    public BlockColorMap<ConstructorBlock, BaseBlockItem> getConstructor() {
        return requireNonNull(constructor);
    }

    public BlockColorMap<WirelessTransmitterBlock, BaseBlockItem> getWirelessTransmitter() {
        return wirelessTransmitter;
    }

    public void setStorageMonitor(final Supplier<StorageMonitorBlock> supplier) {
        this.storageMonitor = supplier;
    }

    public StorageMonitorBlock getStorageMonitor() {
        return requireNonNull(storageMonitor).get();
    }

    public BlockColorMap<NetworkReceiverBlock, BaseBlockItem> getNetworkReceiver() {
        return networkReceiver;
    }

    public BlockColorMap<NetworkTransmitterBlock, BaseBlockItem> getNetworkTransmitter() {
        return networkTransmitter;
    }

    public PortableGridBlock getPortableGrid() {
        return requireNonNull(portableGrid).get();
    }

    public void setPortableGrid(final Supplier<PortableGridBlock> supplier) {
        this.portableGrid = supplier;
    }

    public PortableGridBlock getCreativePortableGrid() {
        return requireNonNull(creativePortableGrid).get();
    }

    public void setCreativePortableGrid(final Supplier<PortableGridBlock> supplier) {
        this.creativePortableGrid = supplier;
    }

    public BlockColorMap<SecurityManagerBlock, BaseBlockItem> getSecurityManager() {
        return securityManager;
    }

    public BlockColorMap<RelayBlock, BaseBlockItem> getRelay() {
        return relay;
    }

    public BlockColorMap<DiskInterfaceBlock, BaseBlockItem> setDiskInterface(
        final BlockEntityProvider<AbstractDiskInterfaceBlockEntity> provider
    ) {
        this.diskInterface = new BlockColorMap<>(
            (id, color, name) -> new DiskInterfaceBlock(
                id,
                color,
                name,
                provider
            ),
            ContentIds.DISK_INTERFACE,
            ContentNames.DISK_INTERFACE,
            COLOR
        );
        return diskInterface;
    }

    public BlockColorMap<DiskInterfaceBlock, BaseBlockItem> getDiskInterface() {
        return requireNonNull(diskInterface);
    }

    public BlockColorMap<AutocrafterBlock, BaseBlockItem> getAutocrafter() {
        return autocrafter;
    }

    public BlockColorMap<AutocrafterManagerBlock, BaseBlockItem> getAutocrafterManager() {
        return autocrafterManager;
    }

    public BlockColorMap<AutocraftingMonitorBlock, BaseBlockItem> getAutocraftingMonitor() {
        return autocraftingMonitor;
    }
}

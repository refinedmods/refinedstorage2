package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.constructordestructor.ConstructorBlock;
import com.refinedmods.refinedstorage2.platform.common.constructordestructor.DestructorBlock;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.detector.DetectorBlock;
import com.refinedmods.refinedstorage2.platform.common.exporter.ExporterBlock;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridBlock;
import com.refinedmods.refinedstorage2.platform.common.grid.GridBlock;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfaceBlock;
import com.refinedmods.refinedstorage2.platform.common.importer.ImporterBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkReceiverBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.externalstorage.ExternalStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.storageblock.FluidStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.storage.storageblock.ItemStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.storagemonitor.StorageMonitorBlock;
import com.refinedmods.refinedstorage2.platform.common.support.SimpleBlock;
import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterBlock;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.item.DyeColor;

public final class Blocks {
    public static final DyeColor COLOR = DyeColor.LIGHT_BLUE;
    public static final DyeColor CABLE_LIKE_COLOR = DyeColor.GRAY;
    public static final Blocks INSTANCE = new Blocks();

    private final BlockColorMap<CableBlock> cable = new BlockColorMap<>(
        CableBlock::new,
        ContentIds.CABLE,
        ContentNames.CABLE,
        CABLE_LIKE_COLOR
    );
    private final BlockColorMap<GridBlock> grid = new BlockColorMap<>(
        GridBlock::new,
        ContentIds.GRID,
        ContentNames.GRID,
        COLOR
    );
    private final BlockColorMap<CraftingGridBlock> craftingGrid = new BlockColorMap<>(
        CraftingGridBlock::new,
        ContentIds.CRAFTING_GRID,
        ContentNames.CRAFTING_GRID,
        COLOR
    );
    private final BlockColorMap<DetectorBlock> detector = new BlockColorMap<>(
        DetectorBlock::new,
        ContentIds.DETECTOR,
        ContentNames.DETECTOR,
        COLOR
    );
    private final BlockColorMap<ControllerBlock> controller = new BlockColorMap<>(
        (color, name) -> new ControllerBlock(
            ControllerType.NORMAL,
            name,
            new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getController),
            color
        ),
        ContentIds.CONTROLLER,
        ContentNames.CONTROLLER,
        COLOR
    );
    private final BlockColorMap<ControllerBlock> creativeController = new BlockColorMap<>(
        (color, name) -> new ControllerBlock(
            ControllerType.CREATIVE,
            name,
            new ControllerBlockEntityTicker(BlockEntities.INSTANCE::getCreativeController),
            color
        ),
        ContentIds.CREATIVE_CONTROLLER,
        ContentNames.CREATIVE_CONTROLLER,
        COLOR
    );
    private final BlockColorMap<ExporterBlock> exporter = new BlockColorMap<>(
        ExporterBlock::new,
        ContentIds.EXPORTER,
        ContentNames.EXPORTER,
        CABLE_LIKE_COLOR
    );
    private final BlockColorMap<ImporterBlock> importer = new BlockColorMap<>(
        ImporterBlock::new,
        ContentIds.IMPORTER,
        ContentNames.IMPORTER,
        CABLE_LIKE_COLOR
    );
    private final BlockColorMap<ExternalStorageBlock> externalStorage = new BlockColorMap<>(
        ExternalStorageBlock::new,
        ContentIds.EXTERNAL_STORAGE,
        ContentNames.EXTERNAL_STORAGE,
        CABLE_LIKE_COLOR
    );
    private final BlockColorMap<DestructorBlock> destructor = new BlockColorMap<>(
        DestructorBlock::new,
        ContentIds.DESTRUCTOR,
        ContentNames.DESTRUCTOR,
        CABLE_LIKE_COLOR
    );
    private final BlockColorMap<ConstructorBlock> constructor = new BlockColorMap<>(
        ConstructorBlock::new,
        ContentIds.CONSTRUCTOR,
        ContentNames.CONSTRUCTOR,
        CABLE_LIKE_COLOR
    );
    private final BlockColorMap<WirelessTransmitterBlock> wirelessTransmitter = new BlockColorMap<>(
        WirelessTransmitterBlock::new,
        ContentIds.WIRELESS_TRANSMITTER,
        ContentNames.WIRELESS_TRANSMITTER,
        COLOR
    );
    private final BlockColorMap<NetworkReceiverBlock> networkReceiver = new BlockColorMap<>(
        NetworkReceiverBlock::new,
        ContentIds.NETWORK_RECEIVER,
        ContentNames.NETWORK_RECEIVER,
        COLOR
    );

    @Nullable
    private Supplier<SimpleBlock> quartzEnrichedIronBlock;
    @Nullable
    private Supplier<DiskDriveBlock> diskDrive;
    @Nullable
    private Supplier<SimpleBlock> machineCasing;
    private final Map<ItemStorageType.Variant, Supplier<ItemStorageBlock>> itemStorageBlocks =
        new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<FluidStorageBlock>> fluidStorageBlocks =
        new EnumMap<>(FluidStorageType.Variant.class);
    @Nullable
    private Supplier<InterfaceBlock> iface;
    @Nullable
    private Supplier<StorageMonitorBlock> storageMonitor;

    private Blocks() {
    }

    public BlockColorMap<CableBlock> getCable() {
        return cable;
    }

    public SimpleBlock getQuartzEnrichedIronBlock() {
        return Objects.requireNonNull(quartzEnrichedIronBlock).get();
    }

    public DiskDriveBlock getDiskDrive() {
        return Objects.requireNonNull(diskDrive).get();
    }

    public SimpleBlock getMachineCasing() {
        return Objects.requireNonNull(machineCasing).get();
    }

    public BlockColorMap<GridBlock> getGrid() {
        return grid;
    }

    public BlockColorMap<CraftingGridBlock> getCraftingGrid() {
        return craftingGrid;
    }

    public BlockColorMap<ControllerBlock> getController() {
        return controller;
    }

    public BlockColorMap<ControllerBlock> getCreativeController() {
        return creativeController;
    }

    public void setQuartzEnrichedIronBlock(final Supplier<SimpleBlock> quartzEnrichedIronBlockSupplier) {
        this.quartzEnrichedIronBlock = quartzEnrichedIronBlockSupplier;
    }

    public void setDiskDrive(final Supplier<DiskDriveBlock> diskDriveSupplier) {
        this.diskDrive = diskDriveSupplier;
    }

    public void setMachineCasing(final Supplier<SimpleBlock> machineCasingSupplier) {
        this.machineCasing = machineCasingSupplier;
    }

    public void setItemStorageBlock(final ItemStorageType.Variant variant, final Supplier<ItemStorageBlock> supplier) {
        itemStorageBlocks.put(variant, supplier);
    }

    public ItemStorageBlock getItemStorageBlock(final ItemStorageType.Variant variant) {
        return itemStorageBlocks.get(variant).get();
    }

    public void setFluidStorageBlock(final FluidStorageType.Variant variant,
                                     final Supplier<FluidStorageBlock> supplier) {
        fluidStorageBlocks.put(variant, supplier);
    }

    public FluidStorageBlock getFluidStorageBlock(final FluidStorageType.Variant variant) {
        return fluidStorageBlocks.get(variant).get();
    }

    public BlockColorMap<ImporterBlock> getImporter() {
        return importer;
    }

    public BlockColorMap<ExporterBlock> getExporter() {
        return exporter;
    }

    public void setInterface(final Supplier<InterfaceBlock> interfaceSupplier) {
        this.iface = interfaceSupplier;
    }

    public InterfaceBlock getInterface() {
        return Objects.requireNonNull(iface).get();
    }

    public BlockColorMap<ExternalStorageBlock> getExternalStorage() {
        return externalStorage;
    }

    public BlockColorMap<DetectorBlock> getDetector() {
        return detector;
    }

    public BlockColorMap<DestructorBlock> getDestructor() {
        return destructor;
    }

    public BlockColorMap<ConstructorBlock> getConstructor() {
        return constructor;
    }

    public BlockColorMap<WirelessTransmitterBlock> getWirelessTransmitter() {
        return wirelessTransmitter;
    }

    public void setStorageMonitor(final Supplier<StorageMonitorBlock> supplier) {
        this.storageMonitor = supplier;
    }

    public StorageMonitorBlock getStorageMonitor() {
        return Objects.requireNonNull(storageMonitor).get();
    }

    public BlockColorMap<NetworkReceiverBlock> getNetworkReceiver() {
        return networkReceiver;
    }
}

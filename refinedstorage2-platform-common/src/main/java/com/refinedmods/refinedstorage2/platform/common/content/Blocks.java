package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
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
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    private final BlockColorMap<ItemGridBlock> grid = new BlockColorMap<>();
    private final BlockColorMap<FluidGridBlock> fluidGrid = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> controller = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> creativeController = new BlockColorMap<>();
    @Nullable
    private Supplier<CableBlock> cable;
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
    private Supplier<ImporterBlock> importer;
    @Nullable
    private Supplier<ExporterBlock> exporter;
    @Nullable
    private Supplier<InterfaceBlock> iface;
    @Nullable
    private Supplier<ExternalStorageBlock> externalStorage;

    private Blocks() {
    }

    public CableBlock getCable() {
        return Objects.requireNonNull(cable).get();
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

    public BlockColorMap<ItemGridBlock> getGrid() {
        return grid;
    }

    public BlockColorMap<FluidGridBlock> getFluidGrid() {
        return fluidGrid;
    }

    public BlockColorMap<ControllerBlock> getController() {
        return controller;
    }

    public BlockColorMap<ControllerBlock> getCreativeController() {
        return creativeController;
    }

    public void setCable(final Supplier<CableBlock> cableSupplier) {
        this.cable = cableSupplier;
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

    public void setImporter(final Supplier<ImporterBlock> importerSupplier) {
        this.importer = importerSupplier;
    }

    public ImporterBlock getImporter() {
        return Objects.requireNonNull(importer).get();
    }

    public void setExporter(final Supplier<ExporterBlock> exporterSupplier) {
        this.exporter = exporterSupplier;
    }

    public ExporterBlock getExporter() {
        return Objects.requireNonNull(exporter).get();
    }

    public void setInterface(final Supplier<InterfaceBlock> interfaceSupplier) {
        this.iface = interfaceSupplier;
    }

    public InterfaceBlock getInterface() {
        return Objects.requireNonNull(iface).get();
    }

    public void setExternalStorage(final Supplier<ExternalStorageBlock> externalStorageSupplier) {
        this.externalStorage = externalStorageSupplier;
    }

    public ExternalStorageBlock getExternalStorage() {
        return Objects.requireNonNull(externalStorage).get();
    }
}

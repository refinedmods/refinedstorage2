package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.block.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    private final BlockColorMap<ItemGridBlock> grid = new BlockColorMap<>();
    private final BlockColorMap<FluidGridBlock> fluidGrid = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> controller = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> creativeController = new BlockColorMap<>();
    private Supplier<CableBlock> cable;
    private Supplier<QuartzEnrichedIronBlock> quartzEnrichedIronBlock;
    private Supplier<DiskDriveBlock> diskDrive;
    private Supplier<MachineCasingBlock> machineCasing;
    private final Map<ItemStorageType.Variant, Supplier<ItemStorageBlock>> itemStorageBlocks = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<FluidStorageBlock>> fluidStorageBlocks = new EnumMap<>(FluidStorageType.Variant.class);

    private Blocks() {
    }

    public CableBlock getCable() {
        return cable.get();
    }

    public QuartzEnrichedIronBlock getQuartzEnrichedIronBlock() {
        return quartzEnrichedIronBlock.get();
    }

    public DiskDriveBlock getDiskDrive() {
        return diskDrive.get();
    }

    public MachineCasingBlock getMachineCasing() {
        return machineCasing.get();
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

    public void setCable(Supplier<CableBlock> cableSupplier) {
        this.cable = cableSupplier;
    }

    public void setQuartzEnrichedIronBlock(Supplier<QuartzEnrichedIronBlock> quartzEnrichedIronBlockSupplier) {
        this.quartzEnrichedIronBlock = quartzEnrichedIronBlockSupplier;
    }

    public void setDiskDrive(Supplier<DiskDriveBlock> diskDriveSupplier) {
        this.diskDrive = diskDriveSupplier;
    }

    public void setMachineCasing(Supplier<MachineCasingBlock> machineCasingSupplier) {
        this.machineCasing = machineCasingSupplier;
    }

    // TODO: Cleanup these getters, always return supplied value.
    public Map<ItemStorageType.Variant, Supplier<ItemStorageBlock>> getItemStorageBlocks() {
        return itemStorageBlocks;
    }

    public Map<FluidStorageType.Variant, Supplier<FluidStorageBlock>> getFluidStorageBlocks() {
        return fluidStorageBlocks;
    }
}

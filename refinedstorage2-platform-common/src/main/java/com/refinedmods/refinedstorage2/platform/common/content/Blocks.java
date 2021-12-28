package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.platform.common.block.QuartzEnrichedIronBlock;

public final class Blocks {
    public static final Blocks INSTANCE = new Blocks();

    private final BlockColorMap<ItemGridBlock> grid = new BlockColorMap<>();
    private final BlockColorMap<FluidGridBlock> fluidGrid = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> controller = new BlockColorMap<>();
    private final BlockColorMap<ControllerBlock> creativeController = new BlockColorMap<>();
    private CableBlock cable;
    private QuartzEnrichedIronBlock quartzEnrichedIron;
    private DiskDriveBlock diskDrive;
    private MachineCasingBlock machineCasing;

    private Blocks() {
    }

    public CableBlock getCable() {
        return cable;
    }

    public QuartzEnrichedIronBlock getQuartzEnrichedIron() {
        return quartzEnrichedIron;
    }

    public DiskDriveBlock getDiskDrive() {
        return diskDrive;
    }

    public MachineCasingBlock getMachineCasing() {
        return machineCasing;
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

    public void setCable(CableBlock cable) {
        this.cable = cable;
    }

    public void setQuartzEnrichedIron(QuartzEnrichedIronBlock quartzEnrichedIron) {
        this.quartzEnrichedIron = quartzEnrichedIron;
    }

    public void setDiskDrive(DiskDriveBlock diskDrive) {
        this.diskDrive = diskDrive;
    }

    public void setMachineCasing(MachineCasingBlock machineCasing) {
        this.machineCasing = machineCasing;
    }
}

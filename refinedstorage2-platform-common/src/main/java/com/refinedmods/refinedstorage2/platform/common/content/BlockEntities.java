package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    private BlockEntityType<CableBlockEntity> cable;
    private BlockEntityType<? extends DiskDriveBlockEntity> diskDrive;
    private BlockEntityType<ItemGridBlockEntity> grid;
    private BlockEntityType<FluidGridBlockEntity> fluidGrid;
    private BlockEntityType<ControllerBlockEntity> controller;
    private BlockEntityType<ControllerBlockEntity> creativeController;
    private final Map<ItemStorageType.Variant, BlockEntityType<ItemStorageBlockEntity>> itemStorageBlocks = new EnumMap<>(ItemStorageType.Variant.class);

    private BlockEntities() {
    }

    public BlockEntityType<CableBlockEntity> getCable() {
        return cable;
    }

    public void setCable(BlockEntityType<CableBlockEntity> cable) {
        this.cable = cable;
    }

    public BlockEntityType<? extends DiskDriveBlockEntity> getDiskDrive() {
        return diskDrive;
    }

    public void setDiskDrive(BlockEntityType<? extends DiskDriveBlockEntity> diskDrive) {
        this.diskDrive = diskDrive;
    }

    public BlockEntityType<ItemGridBlockEntity> getGrid() {
        return grid;
    }

    public void setGrid(BlockEntityType<ItemGridBlockEntity> grid) {
        this.grid = grid;
    }

    public BlockEntityType<FluidGridBlockEntity> getFluidGrid() {
        return fluidGrid;
    }

    public void setFluidGrid(BlockEntityType<FluidGridBlockEntity> fluidGrid) {
        this.fluidGrid = fluidGrid;
    }

    public BlockEntityType<ControllerBlockEntity> getController() {
        return controller;
    }

    public void setController(BlockEntityType<ControllerBlockEntity> controller) {
        this.controller = controller;
    }

    public BlockEntityType<ControllerBlockEntity> getCreativeController() {
        return creativeController;
    }

    public void setCreativeController(BlockEntityType<ControllerBlockEntity> creativeController) {
        this.creativeController = creativeController;
    }

    public Map<ItemStorageType.Variant, BlockEntityType<ItemStorageBlockEntity>> getItemStorageBlocks() {
        return itemStorageBlocks;
    }
}

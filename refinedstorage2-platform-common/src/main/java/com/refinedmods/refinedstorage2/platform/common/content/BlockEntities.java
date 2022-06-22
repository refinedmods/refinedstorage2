package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockBlockEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    private Supplier<BlockEntityType<CableBlockEntity>> cable;
    private Supplier<BlockEntityType<? extends DiskDriveBlockEntity>> diskDrive;
    private Supplier<BlockEntityType<ItemGridBlockEntity>> grid;
    private Supplier<BlockEntityType<FluidGridBlockEntity>> fluidGrid;
    private Supplier<BlockEntityType<ControllerBlockEntity>> controller;
    private Supplier<BlockEntityType<ControllerBlockEntity>> creativeController;
    private final Map<ItemStorageType.Variant, Supplier<BlockEntityType<ItemStorageBlockBlockEntity>>> itemStorageBlocks = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<BlockEntityType<FluidStorageBlockBlockEntity>>> fluidStorageBlocks = new EnumMap<>(FluidStorageType.Variant.class);

    private BlockEntities() {
    }

    public BlockEntityType<CableBlockEntity> getCable() {
        return cable.get();
    }

    public void setCable(Supplier<BlockEntityType<CableBlockEntity>> cableSupplier) {
        this.cable = cableSupplier;
    }

    public BlockEntityType<? extends DiskDriveBlockEntity> getDiskDrive() {
        return diskDrive.get();
    }

    public void setDiskDrive(Supplier<BlockEntityType<? extends DiskDriveBlockEntity>> diskDriveSupplier) {
        this.diskDrive = diskDriveSupplier;
    }

    public BlockEntityType<ItemGridBlockEntity> getGrid() {
        return grid.get();
    }

    public void setGrid(Supplier<BlockEntityType<ItemGridBlockEntity>> gridSupplier) {
        this.grid = gridSupplier;
    }

    public BlockEntityType<FluidGridBlockEntity> getFluidGrid() {
        return fluidGrid.get();
    }

    public void setFluidGrid(Supplier<BlockEntityType<FluidGridBlockEntity>> fluidGridSupplier) {
        this.fluidGrid = fluidGridSupplier;
    }

    public BlockEntityType<ControllerBlockEntity> getController() {
        return controller.get();
    }

    public void setController(Supplier<BlockEntityType<ControllerBlockEntity>> controllerSupplier) {
        this.controller = controllerSupplier;
    }

    public BlockEntityType<ControllerBlockEntity> getCreativeController() {
        return creativeController.get();
    }

    public void setCreativeController(Supplier<BlockEntityType<ControllerBlockEntity>> creativeControllerSupplier) {
        this.creativeController = creativeControllerSupplier;
    }

    public void setItemStorageBlock(ItemStorageType.Variant variant, Supplier<BlockEntityType<ItemStorageBlockBlockEntity>> storageBlockBlockEntitySupplier) {
        itemStorageBlocks.put(variant, storageBlockBlockEntitySupplier);
    }

    public BlockEntityType<ItemStorageBlockBlockEntity> getItemStorageBlock(ItemStorageType.Variant variant) {
        return itemStorageBlocks.get(variant).get();
    }

    public void setFluidStorageBlock(FluidStorageType.Variant variant, Supplier<BlockEntityType<FluidStorageBlockBlockEntity>> storageBlockBlockEntitySupplier) {
        fluidStorageBlocks.put(variant, storageBlockBlockEntitySupplier);
    }

    public BlockEntityType<FluidStorageBlockBlockEntity> getFluidStorageBlock(FluidStorageType.Variant variant) {
        return fluidStorageBlocks.get(variant).get();
    }
}

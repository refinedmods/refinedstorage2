package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockBlockEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    @Nullable
    private Supplier<BlockEntityType<CableBlockEntity>> cable;
    @Nullable
    private Supplier<BlockEntityType<? extends AbstractDiskDriveBlockEntity>> diskDrive;
    @Nullable
    private Supplier<BlockEntityType<ItemGridBlockEntity>> grid;
    @Nullable
    private Supplier<BlockEntityType<FluidGridBlockEntity>> fluidGrid;
    @Nullable
    private Supplier<BlockEntityType<ControllerBlockEntity>> controller;
    @Nullable
    private Supplier<BlockEntityType<ControllerBlockEntity>> creativeController;
    private final Map<ItemStorageType.Variant, Supplier<BlockEntityType<ItemStorageBlockBlockEntity>>>
            itemStorageBlocks = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<BlockEntityType<FluidStorageBlockBlockEntity>>>
            fluidStorageBlocks = new EnumMap<>(FluidStorageType.Variant.class);

    private BlockEntities() {
    }

    public BlockEntityType<CableBlockEntity> getCable() {
        return Objects.requireNonNull(cable).get();
    }

    public void setCable(final Supplier<BlockEntityType<CableBlockEntity>> supplier) {
        this.cable = supplier;
    }

    public BlockEntityType<? extends AbstractDiskDriveBlockEntity> getDiskDrive() {
        return Objects.requireNonNull(diskDrive).get();
    }

    public void setDiskDrive(final Supplier<BlockEntityType<? extends AbstractDiskDriveBlockEntity>> supplier) {
        this.diskDrive = supplier;
    }

    public BlockEntityType<ItemGridBlockEntity> getGrid() {
        return Objects.requireNonNull(grid).get();
    }

    public void setGrid(final Supplier<BlockEntityType<ItemGridBlockEntity>> supplier) {
        this.grid = supplier;
    }

    public BlockEntityType<FluidGridBlockEntity> getFluidGrid() {
        return Objects.requireNonNull(fluidGrid).get();
    }

    public void setFluidGrid(final Supplier<BlockEntityType<FluidGridBlockEntity>> supplier) {
        this.fluidGrid = supplier;
    }

    public BlockEntityType<ControllerBlockEntity> getController() {
        return Objects.requireNonNull(controller).get();
    }

    public void setController(final Supplier<BlockEntityType<ControllerBlockEntity>> supplier) {
        this.controller = supplier;
    }

    public BlockEntityType<ControllerBlockEntity> getCreativeController() {
        return Objects.requireNonNull(creativeController).get();
    }

    public void setCreativeController(final Supplier<BlockEntityType<ControllerBlockEntity>> supplier) {
        this.creativeController = supplier;
    }

    public void setItemStorageBlock(final ItemStorageType.Variant variant,
                                    final Supplier<BlockEntityType<ItemStorageBlockBlockEntity>> supplier) {
        itemStorageBlocks.put(variant, supplier);
    }

    public BlockEntityType<ItemStorageBlockBlockEntity> getItemStorageBlock(final ItemStorageType.Variant variant) {
        return itemStorageBlocks.get(variant).get();
    }

    public void setFluidStorageBlock(final FluidStorageType.Variant variant,
                                     final Supplier<BlockEntityType<FluidStorageBlockBlockEntity>> supplier) {
        fluidStorageBlocks.put(variant, supplier);
    }

    public BlockEntityType<FluidStorageBlockBlockEntity> getFluidStorageBlock(final FluidStorageType.Variant variant) {
        return fluidStorageBlocks.get(variant).get();
    }
}

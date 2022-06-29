package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.inventory.MenuType;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    @Nullable
    private Supplier<MenuType<DiskDriveContainerMenu>> diskDrive;
    @Nullable
    private Supplier<MenuType<ItemGridContainerMenu>> grid;
    @Nullable
    private Supplier<MenuType<FluidGridContainerMenu>> fluidGrid;
    @Nullable
    private Supplier<MenuType<ControllerContainerMenu>> controller;
    @Nullable
    private Supplier<MenuType<ItemStorageBlockContainerMenu>> itemStorage;
    @Nullable
    private Supplier<MenuType<FluidStorageBlockContainerMenu>> fluidStorage;

    private Menus() {
    }

    public MenuType<DiskDriveContainerMenu> getDiskDrive() {
        return Objects.requireNonNull(diskDrive).get();
    }

    public MenuType<ItemGridContainerMenu> getGrid() {
        return Objects.requireNonNull(grid).get();
    }

    public MenuType<FluidGridContainerMenu> getFluidGrid() {
        return Objects.requireNonNull(fluidGrid).get();
    }

    public MenuType<ControllerContainerMenu> getController() {
        return Objects.requireNonNull(controller).get();
    }

    public void setDiskDrive(final Supplier<MenuType<DiskDriveContainerMenu>> diskDriveSupplier) {
        this.diskDrive = diskDriveSupplier;
    }

    public void setGrid(final Supplier<MenuType<ItemGridContainerMenu>> gridSupplier) {
        this.grid = gridSupplier;
    }

    public void setFluidGrid(final Supplier<MenuType<FluidGridContainerMenu>> fluidGridSupplier) {
        this.fluidGrid = fluidGridSupplier;
    }

    public void setController(final Supplier<MenuType<ControllerContainerMenu>> controllerSupplier) {
        this.controller = controllerSupplier;
    }

    public MenuType<ItemStorageBlockContainerMenu> getItemStorage() {
        return Objects.requireNonNull(itemStorage).get();
    }

    public void setItemStorage(final Supplier<MenuType<ItemStorageBlockContainerMenu>> itemStorageSupplier) {
        this.itemStorage = itemStorageSupplier;
    }

    public MenuType<FluidStorageBlockContainerMenu> getFluidStorage() {
        return Objects.requireNonNull(fluidStorage).get();
    }

    public void setFluidStorage(final Supplier<MenuType<FluidStorageBlockContainerMenu>> fluidStorageSupplier) {
        this.fluidStorage = fluidStorageSupplier;
    }
}

package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;

import java.util.function.Supplier;

import net.minecraft.world.inventory.MenuType;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    private Supplier<MenuType<DiskDriveContainerMenu>> diskDrive;
    private Supplier<MenuType<ItemGridContainerMenu>> grid;
    private Supplier<MenuType<FluidGridContainerMenu>> fluidGrid;
    private Supplier<MenuType<ControllerContainerMenu>> controller;
    private Supplier<MenuType<ItemStorageBlockContainerMenu>> itemStorage;
    private Supplier<MenuType<FluidStorageBlockContainerMenu>> fluidStorage;

    private Menus() {
    }

    public MenuType<DiskDriveContainerMenu> getDiskDrive() {
        return diskDrive.get();
    }

    public MenuType<ItemGridContainerMenu> getGrid() {
        return grid.get();
    }

    public MenuType<FluidGridContainerMenu> getFluidGrid() {
        return fluidGrid.get();
    }

    public MenuType<ControllerContainerMenu> getController() {
        return controller.get();
    }

    public void setDiskDrive(Supplier<MenuType<DiskDriveContainerMenu>> diskDriveSupplier) {
        this.diskDrive = diskDriveSupplier;
    }

    public void setGrid(Supplier<MenuType<ItemGridContainerMenu>> gridSupplier) {
        this.grid = gridSupplier;
    }

    public void setFluidGrid(Supplier<MenuType<FluidGridContainerMenu>> fluidGridSupplier) {
        this.fluidGrid = fluidGridSupplier;
    }

    public void setController(Supplier<MenuType<ControllerContainerMenu>> controllerSupplier) {
        this.controller = controllerSupplier;
    }

    public MenuType<ItemStorageBlockContainerMenu> getItemStorage() {
        return itemStorage.get();
    }

    public void setItemStorage(Supplier<MenuType<ItemStorageBlockContainerMenu>> itemStorageSupplier) {
        this.itemStorage = itemStorageSupplier;
    }

    public MenuType<FluidStorageBlockContainerMenu> getFluidStorage() {
        return fluidStorage.get();
    }

    public void setFluidStorage(Supplier<MenuType<FluidStorageBlockContainerMenu>> fluidStorageSupplier) {
        this.fluidStorage = fluidStorageSupplier;
    }
}

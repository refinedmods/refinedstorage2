package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;

import net.minecraft.world.inventory.MenuType;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    private MenuType<DiskDriveContainerMenu> diskDrive;
    private MenuType<ItemGridContainerMenu> grid;
    private MenuType<FluidGridContainerMenu> fluidGrid;
    private MenuType<ControllerContainerMenu> controller;
    private MenuType<ItemStorageBlockContainerMenu> itemStorage;
    private MenuType<FluidStorageBlockContainerMenu> fluidStorage;

    private Menus() {
    }

    public MenuType<DiskDriveContainerMenu> getDiskDrive() {
        return diskDrive;
    }

    public MenuType<ItemGridContainerMenu> getGrid() {
        return grid;
    }

    public MenuType<FluidGridContainerMenu> getFluidGrid() {
        return fluidGrid;
    }

    public MenuType<ControllerContainerMenu> getController() {
        return controller;
    }

    public void setDiskDrive(MenuType<DiskDriveContainerMenu> diskDrive) {
        this.diskDrive = diskDrive;
    }

    public void setGrid(MenuType<ItemGridContainerMenu> grid) {
        this.grid = grid;
    }

    public void setFluidGrid(MenuType<FluidGridContainerMenu> fluidGrid) {
        this.fluidGrid = fluidGrid;
    }

    public void setController(MenuType<ControllerContainerMenu> controller) {
        this.controller = controller;
    }

    public MenuType<ItemStorageBlockContainerMenu> getItemStorage() {
        return itemStorage;
    }

    public void setItemStorage(MenuType<ItemStorageBlockContainerMenu> itemStorage) {
        this.itemStorage = itemStorage;
    }

    public MenuType<FluidStorageBlockContainerMenu> getFluidStorage() {
        return fluidStorage;
    }

    public void setFluidStorage(MenuType<FluidStorageBlockContainerMenu> fluidStorage) {
        this.fluidStorage = fluidStorage;
    }
}

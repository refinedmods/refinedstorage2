package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.ItemGridContainerMenu;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.inventory.MenuType;

public class Rs2Menus {
    private MenuType<DiskDriveContainerMenu> diskDrive;
    private MenuType<ItemGridContainerMenu> grid;
    private MenuType<FluidGridContainerMenu> fluidGrid;
    private MenuType<ControllerContainerMenu> controller;

    public void register() {
        diskDrive = ScreenHandlerRegistry.registerSimple(Rs2Mod.createIdentifier("disk_drive"), DiskDriveContainerMenu::new);
        grid = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("grid"), ItemGridContainerMenu::new);
        fluidGrid = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("fluid_grid"), FluidGridContainerMenu::new);
        controller = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("controller"), ControllerContainerMenu::new);
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
}

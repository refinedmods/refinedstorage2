package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.ControllerScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.FluidGridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.ItemGridScreenHandler;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.world.inventory.MenuType;

public class Rs2ScreenHandlers {
    private MenuType<DiskDriveScreenHandler> diskDrive;
    private MenuType<ItemGridScreenHandler> grid;
    private MenuType<FluidGridScreenHandler> fluidGrid;
    private MenuType<ControllerScreenHandler> controller;

    public void register() {
        diskDrive = ScreenHandlerRegistry.registerSimple(Rs2Mod.createIdentifier("disk_drive"), DiskDriveScreenHandler::new);
        grid = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("grid"), ItemGridScreenHandler::new);
        fluidGrid = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("fluid_grid"), FluidGridScreenHandler::new);
        controller = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("controller"), ControllerScreenHandler::new);
    }

    public MenuType<DiskDriveScreenHandler> getDiskDrive() {
        return diskDrive;
    }

    public MenuType<ItemGridScreenHandler> getGrid() {
        return grid;
    }

    public MenuType<FluidGridScreenHandler> getFluidGrid() {
        return fluidGrid;
    }

    public MenuType<ControllerScreenHandler> getController() {
        return controller;
    }
}
